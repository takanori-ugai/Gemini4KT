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
import io.github.ugaikit.gemini4kt.getApiKey
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

object FunctionExample2 {
    /**
     * A sample function that finds the weather in a given location.
     *
     * @param location The city and state, e.g. San Francisco, CA.
     * @return A description of the weather.
     */
    fun findWeather(location: String): String = "The weather in $location is super sunny"

    suspend fun run(gemini: Gemini? = null) {
        val client = gemini ?: Gemini(getApiKey())

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
                        required = arrayOf("location"),
                    ),
            )

        val tools = arrayOf(Tool(functionDeclarations = arrayOf(findWeatherFunction)))

        // Step 1: Send the user's prompt and function declarations to the model.
        val userPrompt = "What's the weather like in Boston?"
        val firstResponse = getFunctionCall(client, tools, userPrompt)

        val modelResponsePart =
            firstResponse.candidates[0]
                .content.parts!!
                .get(0)
        val functionCall = modelResponsePart.functionCall
        println("Model requested function call: $functionCall")

        // Step 2: "Execute" the function and send the response back to the model.
        val initialContent = Content(role = "user", parts = arrayOf(Part(text = userPrompt)))
        sendFunctionResult(client, tools, initialContent, modelResponsePart)
    }

    private suspend fun getFunctionCall(
        gemini: Gemini,
        tools: Array<Tool>,
        userPrompt: String,
    ): GenerateContentResponse {
        val initialContent = Content(role = "user", parts = arrayOf(Part(text = userPrompt)))
        val firstRequest = GenerateContentRequest(contents = arrayOf(initialContent), tools = tools)
        return gemini.generateContent(firstRequest, "gemini-2.5-flash-lite")
    }

    private suspend fun sendFunctionResult(
        gemini: Gemini,
        tools: Array<Tool>,
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
                        arrayOf(
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
                arrayOf(
                    initialContent,
                    Content(role = "model", parts = arrayOf(modelResponsePart)),
                    functionResponseContent,
                )

            val secondRequest = GenerateContentRequest(contents = conversationHistory, tools = tools)
            val secondResponse = gemini.generateContent(secondRequest, "gemini-2.5-flash-lite")
            println("Final response: ${secondResponse.candidates[0].content.parts!!.get(0).text}")
        }
    }
}
