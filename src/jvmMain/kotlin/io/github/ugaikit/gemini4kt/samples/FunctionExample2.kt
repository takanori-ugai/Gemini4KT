package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.Content
import io.github.ugaikit.gemini4kt.FunctionDeclaration
import io.github.ugaikit.gemini4kt.FunctionResponse
import io.github.ugaikit.gemini4kt.Gemini
import io.github.ugaikit.gemini4kt.GenerateContentRequest
import io.github.ugaikit.gemini4kt.GenerateContentResponse
import io.github.ugaikit.gemini4kt.Part
import io.github.ugaikit.gemini4kt.Schema
import io.github.ugaikit.gemini4kt.Tool
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.util.Properties

object FunctionExample2 {
    /**
     * A sample function that finds the weather in a given location.
     *
     * @param location The city and state, e.g. San Francisco, CA.
     * @return A description of the weather.
     */
    fun findWeather(location: String): String = "The weather in $location is super sunny"

    private suspend fun getFunctionCall(
        gemini: Gemini,
        tools: List<Tool>,
        userPrompt: String,
    ): GenerateContentResponse {
        val initialContent = Content(role = "user", parts = listOf(Part(text = userPrompt)))
        val firstRequest = GenerateContentRequest(contents = listOf(initialContent), tools = tools)
        return gemini.generateContent(firstRequest, "gemini-2.5-flash-lite")
    }

    private suspend fun sendFunctionResult(
        gemini: Gemini,
        tools: List<Tool>,
        initialContent: Content,
        modelResponsePart: Part,
    ) {
        val functionCall = modelResponsePart.functionCall
        if (functionCall != null && functionCall.name == "find_weather") {
            val location = functionCall.args["location"]?.jsonPrimitive?.content ?: ""
            val weather = findWeather(location)

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

    @JvmStatic
    fun main(args: Array<String>) =
        runBlocking {
            val apiKey =
                Gemini::class.java.getResourceAsStream("/prop.properties").use { inputStream ->
                    Properties()
                        .apply {
                            load(inputStream)
                        }.getProperty("apiKey")
                }
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
                                ),
                            required = listOf("location"),
                        ),
                )

            val tools = listOf(Tool(functionDeclarations = listOf(findWeatherFunction)))

            // Step 1: Send the user's prompt and function declarations to the model.
            val userPrompt = "What's the weather like in Boston?"
            val firstResponse = getFunctionCall(gemini, tools, userPrompt)

            val modelResponsePart =
                firstResponse.candidates[0]
                    .content.parts!!
                    .get(0)
            val functionCall = modelResponsePart.functionCall
            println("Model requested function call: $functionCall")

            // Step 2: "Execute" the function and send the response back to the model.
            val initialContent = Content(role = "user", parts = listOf(Part(text = userPrompt)))
            sendFunctionResult(gemini, tools, initialContent, modelResponsePart)
        }
}
