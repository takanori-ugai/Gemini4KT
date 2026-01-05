package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.Content
import io.github.ugaikit.gemini4kt.FunctionResponse
import io.github.ugaikit.gemini4kt.Gemini
import io.github.ugaikit.gemini4kt.GeminiFunction
import io.github.ugaikit.gemini4kt.GeminiParameter
import io.github.ugaikit.gemini4kt.GenerateContentRequest
import io.github.ugaikit.gemini4kt.Part
import io.github.ugaikit.gemini4kt.Tool
import io.github.ugaikit.gemini4kt.buildFunctionDeclaration
import io.github.ugaikit.gemini4kt.getApiKey
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/**
 * Represents the function example3.
 */
object FunctionExample3 {
    /**
     * Handles add.
     *
     * @param a The a.
     * @param b The b.
     */
    @GeminiFunction(description = "add two numbers")
    fun add(
        @GeminiParameter(description = "first number") a: Int,
        @GeminiParameter(description = "second number") b: Int,
    ): Int {
        println("Add is called")
        return a + b
    }

    /**
     * Handles run.
     *
     * @param gemini The gemini.
     */
    suspend fun run(gemini: Gemini? = null) {
        val client = gemini ?: Gemini(getApiKey())

        val addFunction = buildFunctionDeclaration(::add)

        val tools = arrayOf(Tool(functionDeclarations = arrayOf(addFunction)))

        // Step 1: Send the user's prompt and function declarations to the model.
        val userPrompt = "What is 123 plus 456?"
        val initialContent = Content(role = "user", parts = arrayOf(Part(text = userPrompt)))
        val firstRequest = GenerateContentRequest(contents = arrayOf(initialContent), tools = tools)
        val firstResponse = client.generateContent(firstRequest, "gemini-2.5-flash-lite")

        val modelResponsePart =
            firstResponse.candidates[0]
                .content.parts!!
                .get(0)
        val functionCall = modelResponsePart.functionCall
        println("Model requested function call: $functionCall")

        // Step 2: "Execute" the function and send the response back to the model.
        if (functionCall != null && functionCall.name == "add") {
            val a = functionCall.args["a"]?.jsonPrimitive?.int ?: 0
            val b = functionCall.args["b"]?.jsonPrimitive?.int ?: 0

            val result = add(a, b)

            val functionResponseContent =
                Content(
                    role = "function",
                    parts =
                        arrayOf(
                            Part(
                                functionResponse =
                                    FunctionResponse(
                                        name = "add",
                                        response = buildJsonObject { put("result", result) },
                                    ),
                            ),
                        ),
                )

            // Add the history (user prompt, model's function call) and the new function response to the next request.
            val conversationHistory =
                arrayOf(
                    initialContent,
                    Content(role = "model", parts = arrayOf(modelResponsePart)),
                    functionResponseContent,
                )

            val secondRequest = GenerateContentRequest(contents = conversationHistory, tools = tools)
            val secondResponse = client.generateContent(secondRequest, "gemini-2.5-flash-lite")
            val firstCandidate = secondResponse.candidates.firstOrNull()
            val finalText =
                firstCandidate
                    ?.content
                    ?.parts
                    ?.firstOrNull()
                    ?.text
            if (finalText != null) {
                println("Final response: $finalText")
            } else {
                // Show the full candidate content when the model returns non-text parts.
                println("Final candidate content: ${firstCandidate?.content}")
            }
        }
    }
}
