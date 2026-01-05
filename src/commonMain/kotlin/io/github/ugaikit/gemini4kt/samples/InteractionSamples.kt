package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.GeminiAI
import io.github.ugaikit.gemini4kt.getApiKey
import io.github.ugaikit.gemini4kt.getImage
import io.github.ugaikit.gemini4kt.interaction.CreateInteractionRequest
import io.github.ugaikit.gemini4kt.interaction.Interaction
import io.github.ugaikit.gemini4kt.interaction.InteractionContent
import io.github.ugaikit.gemini4kt.interaction.InteractionTool
import io.github.ugaikit.gemini4kt.interaction.InteractionTurn
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

object InteractionSamples {
    private val json =
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = false
        }

    suspend fun runSimple(client: GeminiAI? = null) {
        val ai = client ?: GeminiAI(apiKey = getApiKey())
        println("--- Simple Request ---")
        val request =
            CreateInteractionRequest(
                model = "gemini-2.5-flash",
                input = JsonPrimitive("Hello, how are you?"),
            )
        val interaction = ai.createInteraction(request)
        printOutputs(interaction)
    }

    suspend fun runMultiTurn(client: GeminiAI? = null) {
        val ai = client ?: GeminiAI(apiKey = getApiKey())
        println("--- Multi-turn ---")
        val turns =
            listOf(
                InteractionTurn(role = "user", content = JsonPrimitive("Hello!")),
                InteractionTurn(role = "model", content = JsonPrimitive("Hi there! How can I help you today?")),
                InteractionTurn(role = "user", content = JsonPrimitive("What is the capital of France?")),
            )
        val inputJson = json.encodeToJsonElement(turns)

        val request =
            CreateInteractionRequest(
                model = "gemini-2.5-flash",
                input = inputJson,
            )
        val interaction = ai.createInteraction(request)
        printOutputs(interaction)
    }

    suspend fun runImageInput(
        client: GeminiAI? = null,
        imageProvider: () -> String = { getImage() },
    ) {
        val ai = client ?: GeminiAI(apiKey = getApiKey())
        println("--- Image Input ---")
        val base64Image = imageProvider()

        val textContent = InteractionContent(type = "text", text = "What is in this picture?")
        val imageContent = InteractionContent(type = "image", data = base64Image, mimeType = "image/jpeg")

        val inputList = listOf(textContent, imageContent)
        val inputJson = json.encodeToJsonElement(inputList)

        val request =
            CreateInteractionRequest(
                model = "gemini-2.5-flash",
                input = inputJson,
            )
        val interaction = ai.createInteraction(request)
        printOutputs(interaction)
    }

    suspend fun runFunctionCalling(client: GeminiAI? = null) {
        val ai = client ?: GeminiAI(apiKey = getApiKey())
        println("--- Function Calling ---")

        val tool =
            InteractionTool(
                type = "function",
                name = "get_weather",
                description = "Get the current weather in a given location",
                parameters =
                    buildJsonObject {
                        put("type", "object")
                        put(
                            "properties",
                            buildJsonObject {
                                put(
                                    "location",
                                    buildJsonObject {
                                        put("type", "string")
                                        put("description", "The city and state, e.g. San Francisco, CA")
                                    },
                                )
                            },
                        )
                        putJsonArray("required") { add(JsonPrimitive("location")) }
                    },
            )

        val request =
            CreateInteractionRequest(
                model = "gemini-2.5-flash",
                tools = arrayOf(tool),
                input = JsonPrimitive("What is the weather like in Boston, MA?"),
            )
        val interaction = ai.createInteraction(request)
        printOutputs(interaction)
    }

    private fun printOutputs(interaction: Interaction) {
        val outputs = interaction.outputs
        if (outputs.isNullOrEmpty()) {
            println("No outputs. Status: ${interaction.status}")
            return
        }
        outputs.forEach { content ->
            println("Type: ${content.type}")
            when (content.type) {
                "text" -> println("Text: ${content.text}")
                "image" -> println("Image: [Image Data]")
                "function_call" -> println("Function Call: ${content.name}(${content.arguments})")
                "thought" -> println("Thought: ${content.summary?.content?.text ?: "No summary"}")
                else -> println("Content: $content")
            }
        }
    }

    suspend fun runDeepResearch(client: GeminiAI? = null) {
        val ai = client ?: GeminiAI(apiKey = getApiKey())
        println("--- Deep Research ---")
        val request =
            CreateInteractionRequest(
                agent = "deep-research-pro-preview-12-2025",
                input = JsonPrimitive("Find a cure to cancer"),
                background = true,
            )
        try {
            val interaction = ai.createInteraction(request)
            println("Status: ${interaction.status}")
        } catch (e: Exception) {
            println("Error: ${e.message}")
        }
    }
}
