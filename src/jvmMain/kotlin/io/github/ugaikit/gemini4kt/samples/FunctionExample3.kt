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
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.util.Properties

@GeminiFunction(description = "add two numbers")
fun add(
    @GeminiParameter(description = "first number") a: Int,
    @GeminiParameter(description = "second number") b: Int,
): Int = a + b

object FunctionExample3Sample {
    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        val apiKey =
            Gemini::class.java.getResourceAsStream("/prop.properties").use { inputStream ->
            Properties()
                .apply {
                    load(inputStream)
                }.getProperty("apiKey")
        }
    val gemini = Gemini(apiKey)

    val addFunction = buildFunctionDeclaration(::add)

    val tools = listOf(Tool(functionDeclarations = listOf(addFunction)))

    // Step 1: Send the user's prompt and function declarations to the model.
    val userPrompt = "What is 123 plus 456?"
    val initialContent = Content(role = "user", parts = listOf(Part(text = userPrompt)))
    val firstRequest = GenerateContentRequest(contents = listOf(initialContent), tools = tools)
    val firstResponse = gemini.generateContent(firstRequest, "gemini-2.5-flash-lite")

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
                    listOf(
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
            listOf(
                initialContent,
                Content(role = "model", parts = listOf(modelResponsePart)),
                functionResponseContent,
            )

        val secondRequest = GenerateContentRequest(contents = conversationHistory, tools = tools)
        val secondResponse = gemini.generateContent(secondRequest, "gemini-2.5-flash-lite")
        println("Final response: ${secondResponse.candidates[0].content.parts!!.get(0).text}")
    }
    }
}
