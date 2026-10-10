@file:Suppress("TooManyFunctions")

package io.github.ugaikit.gemini4kt

import io.github.ugaikit.gemini4kt.interaction.CreateInteractionRequest
import io.github.ugaikit.gemini4kt.interaction.InteractionContent
import io.github.ugaikit.gemini4kt.interaction.InteractionInput
import io.github.ugaikit.gemini4kt.interaction.InteractionStep
import io.github.ugaikit.gemini4kt.interaction.InteractionTool
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import java.io.File
import java.util.Base64

private const val IT_REQUEST_TIMEOUT_MS = 180_000L
private const val HTTP_TOO_MANY_REQUESTS = 429
private val INTERACTION_MODELS = listOf("gemini-3.5-flash-lite", "gemma-4-31b-it")

@GeminiFunction(description = "Look up the current weather for a city")
private fun lookupWeather(
    @GeminiParameter(description = "City name")
    location: String,
): String = "Sunny in $location"

private fun describeInteractionFailure(error: Exception): String {
    val timeout = generateSequence(error as Throwable?) { it.cause }
        .filterIsInstance<HttpRequestTimeoutException>()
        .firstOrNull()
    if (timeout != null) return "HTTP request timed out: ${timeout.message}"

    val apiError = generateSequence(error as Throwable?) { it.cause }
        .filterIsInstance<GeminiException>()
        .firstOrNull()
        ?.error
    return if (apiError != null) {
        "Gemini API error (HTTP ${apiError.code}, ${apiError.status}): ${apiError.message}"
    } else {
        "${error::class.simpleName}: ${error.message}"
    }
}

private suspend fun testInteractionText(ai: GeminiAI, model: String) {
    println("--- testInteractionText ---")
    val interaction = ai.createInteraction(
        CreateInteractionRequest(
            model = model,
            input = "Write a short story about a magic backpack.",
            stream = false,
        ),
    )
    println("Interaction ID: ${interaction.id}")
    println("Status: ${interaction.status}")
    println("Output: ${interaction.outputText}")
    println("Usage: ${interaction.usage}")
}

private suspend fun testInteractionMultiTurn(ai: GeminiAI, model: String) {
    println("--- testInteractionMultiTurn ---")
    val interaction = ai.createInteraction(
        CreateInteractionRequest(
            model = model,
            input = InteractionInput.StepList(
                arrayOf(
                    InteractionStep(
                        type = "user_input",
                        content = arrayOf(InteractionContent(type = "text", text = "My name is Ada.")),
                    ),
                    InteractionStep(
                        type = "model_output",
                        content = arrayOf(InteractionContent(type = "text", text = "Hello, Ada!")),
                    ),
                    InteractionStep(
                        type = "user_input",
                        content = arrayOf(InteractionContent(type = "text", text = "What is my name?")),
                    ),
                ),
            ),
        ),
    )
    println("Output: ${interaction.outputText}")
}

private suspend fun testInteractionWithImage(ai: GeminiAI, model: String) {
    println("--- testInteractionWithImage ---")
    val imageUrl = Gemini::class.java.getResource("/scones.jpg")
        ?: error("Missing test resource: scones.jpg")
    val base64Image = Base64.getEncoder().encodeToString(File(imageUrl.toURI()).readBytes())
    val interaction = ai.createInteraction(
        CreateInteractionRequest(
            model = model,
            input = InteractionInput.StepList(
                arrayOf(
                    InteractionStep(
                        type = "user_input",
                        content = arrayOf(
                            InteractionContent(type = "text", text = "What is in this picture?"),
                            InteractionContent(type = "image", data = base64Image, mimeType = "image/jpeg"),
                        ),
                    ),
                ),
            ),
        ),
    )
    println("Output: ${interaction.outputText}")
}

private suspend fun testInteractionFunctionCalling(ai: GeminiAI, model: String) {
    println("--- testInteractionFunctionCalling ---")
    val weatherTool = InteractionTool(
        type = "function",
        name = "get_weather",
        description = "Get the current weather in a city",
        parameters = buildJsonObject {
            put("type", "object")
            put("properties", buildJsonObject {
                put("location", buildJsonObject {
                    put("type", "string")
                    put("description", "City name")
                })
            })
            putJsonArray("required") { add(JsonPrimitive("location")) }
        },
    )
    val interaction = ai.createInteraction(
        CreateInteractionRequest(
            model = model,
            input = "What is the weather like in Boston? Use the get_weather function.",
            tools = arrayOf(weatherTool),
        ),
    )
    println("Output: ${interaction.outputText}")
    println("Steps: ${interaction.steps?.contentToString()}")
}

private suspend fun testInteractionGeminiFunction(ai: GeminiAI, model: String) {
    println("--- testInteractionGeminiFunction ---")
    val interaction = ai.createInteraction(
        CreateInteractionRequest(
            model = model,
            input = "Use the lookupWeather function to answer: what is the weather in Tokyo?",
        ),
        ::lookupWeather,
        maxIterations = 4,
    )
    println("Function interaction output: ${interaction.outputText}")
    println("Function interaction steps: ${interaction.steps?.contentToString()}")
}

fun main() = runBlocking {
    val apiKey = getApiKey()
    val client = createHttpClient(
        Json { ignoreUnknownKeys = true },
        GeminiHttpClientConfig(requestTimeoutMillis = IT_REQUEST_TIMEOUT_MS),
    )
    val ai = GeminiAI(apiKey = apiKey, client = client)
    try {
        for (model in INTERACTION_MODELS) {
            println("\n========================================")
            println("Testing Interactions API with model: $model")
            println("========================================")
            val tests = listOf(
                "testInteractionText" to suspend { testInteractionText(ai, model) },
                "testInteractionMultiTurn" to suspend { testInteractionMultiTurn(ai, model) },
                "testInteractionWithImage" to suspend { testInteractionWithImage(ai, model) },
                "testInteractionFunctionCalling" to suspend { testInteractionFunctionCalling(ai, model) },
                "testInteractionGeminiFunction" to suspend { testInteractionGeminiFunction(ai, model) },
            )
            for ((name, test) in tests) {
                try {
                    test()
                } catch (e: Exception) {
                    println("$name failed: ${describeInteractionFailure(e)}")
                    if (e is GeminiException && e.error.code == HTTP_TOO_MANY_REQUESTS) continue
                }
            }
        }
    } finally {
        ai.close()
    }
}
