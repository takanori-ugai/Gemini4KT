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
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/**
 * A sample function that finds the weather in a given location.
 *
 * @param location The city and state, e.g. San Francisco, CA.
 * @param unit The temperature unit, e.g. celsius or fahrenheit.
 * @return A description of the weather.
 */
fun findWeather(
    location: String,
    unit: String?,
): String {
    return "The weather in $location is super sunny"
}

fun main() {
    val apiKey = System.getenv("GEMINI_API_KEY")
    val gemini = Gemini(apiKey)

    val findWeatherFunction =
        FunctionDeclaration(
            name = "find_weather",
            description = "find weather in a given location",
            parameters =
                Schema(
                    type = "object",
                    properties =
                        mapOf(
                            "location" to
                                Schema(
                                    type = "string",
                                    description = "The city and state, e.g. San Francisco, CA",
                                ),
                            "unit" to
                                Schema(
                                    type = "string",
                                    description = "The temperature unit, e.g. celsius or fahrenheit",
                                    enum = listOf("celsius", "fahrenheit"),
                                ),
                        ),
                    required = listOf("location"),
                ),
        )

    val tools = listOf(Tool(functionDeclarations = listOf(findWeatherFunction)))

    // Step 1: Send the user's prompt and function declarations to the model.
    val userPrompt = "What's the weather like in Boston?"
    val initialContent = Content(role = "user", parts = listOf(Part(text = userPrompt)))
    val firstRequest = GenerateContentRequest(contents = listOf(initialContent), tools = tools)
    val firstResponse = gemini.generateContent(firstRequest, "gemini-1.5-flash")

    val modelResponsePart = firstResponse.candidates[0].content.parts[0]
    val functionCall = modelResponsePart.functionCall
    println("Model requested function call: $functionCall")

    // Step 2: "Execute" the function and send the response back to the model.
    if (functionCall != null && functionCall.name == "find_weather") {
        val location = functionCall.args["location"]?.jsonPrimitive?.content ?: ""
        val unit = functionCall.args["unit"]?.jsonPrimitive?.content

        val weather = findWeather(location, unit)

        val functionResponseContent =
            Content(
                role = "function",
                parts =
                    listOf(
                        Part(
                            functionResponse =
                                FunctionResponse(
                                    name = "find_weather",
                                    response = buildJsonObject { put("weather", weather) },
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
