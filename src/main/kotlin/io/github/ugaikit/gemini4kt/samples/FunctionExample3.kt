package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.Content
import io.github.ugaikit.gemini4kt.FunctionDeclaration
import io.github.ugaikit.gemini4kt.FunctionResponse
import io.github.ugaikit.gemini4kt.Gemini
import io.github.ugaikit.gemini4kt.GenerateContentRequest
import io.github.ugaikit.gemini4kt.Part
import io.github.ugaikit.gemini4kt.Schema
import io.github.ugaikit.gemini4kt.Tool
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

fun add(
    a: Int,
    b: Int,
): Int = a + b

fun main() {
    val apiKey = System.getenv("GEMINI_API_KEY")
    val gemini = Gemini(apiKey)

    val addFunction =
        FunctionDeclaration(
            name = "add",
            description = "add two numbers",
            parameters =
                Schema(
                    type = "object",
                    properties =
                        mapOf(
                            "a" to
                                Schema(
                                    type = "integer",
                                    description = "first number",
                                ),
                            "b" to
                                Schema(
                                    type = "integer",
                                    description = "second number",
                                ),
                        ),
                    required = listOf("a", "b"),
                ),
        )

    val tools = listOf(Tool(functionDeclarations = listOf(addFunction)))

    // Step 1: Send the user's prompt and function declarations to the model.
    val userPrompt = "What is 123 plus 456?"
    val initialContent = Content(role = "user", parts = listOf(Part(text = userPrompt)))
    val firstRequest = GenerateContentRequest(contents = listOf(initialContent), tools = tools)
    val firstResponse = gemini.generateContent(firstRequest, "gemini-1.5-flash")

    val modelResponsePart = firstResponse.candidates[0].content.parts[0]
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
        val secondResponse = gemini.generateContent(secondRequest, "gemini-1.5-flash")
        println("Final response: ${secondResponse.candidates[0].content.parts[0].text}")
    }
}
