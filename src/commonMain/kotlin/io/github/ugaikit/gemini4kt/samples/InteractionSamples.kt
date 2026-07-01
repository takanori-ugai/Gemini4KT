package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.GeminiAI
import io.github.ugaikit.gemini4kt.getApiKey
import io.github.ugaikit.gemini4kt.getImage
import io.github.ugaikit.gemini4kt.interaction.CreateInteractionRequest
import io.github.ugaikit.gemini4kt.interaction.Interaction
import io.github.ugaikit.gemini4kt.interaction.InteractionContent
import io.github.ugaikit.gemini4kt.interaction.InteractionTool
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

val model = "gemma-4-26b-a4b-it"

object InteractionSamples {
    private val json =
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = false
        }

    suspend fun runSimple(client: GeminiAI? = null) {
        val ai = client ?: GeminiAI(apiKey = getApiKey())
        try {
            println("--- Simple Request ---")
            val request =
                CreateInteractionRequest(
                    model = model,
                    input = JsonPrimitive("Hello, how are you?"),
                    background = false,
                )
            val interaction = ai.createInteraction(request)
            printInteractionResult(interaction)
        } finally {
            if (client == null) {
                ai.close()
            }
        }
    }

    suspend fun runMultiTurn(client: GeminiAI? = null) {
        val ai = client ?: GeminiAI(apiKey = getApiKey())
        try {
            println("--- Multi-turn ---")
            val inputJson =
                buildJsonArray {
                    add(
                        buildJsonObject {
                            put("type", JsonPrimitive("user_input"))
                            putJsonArray("content") {
                                add(
                                    buildJsonObject {
                                        put("type", JsonPrimitive("text"))
                                        put("text", JsonPrimitive("Hello!"))
                                    },
                                )
                            }
                        },
                    )
                    add(
                        buildJsonObject {
                            put("type", JsonPrimitive("model_output"))
                            putJsonArray("content") {
                                add(
                                    buildJsonObject {
                                        put("type", JsonPrimitive("text"))
                                        put("text", JsonPrimitive("Hi there! How can I help you today?"))
                                    },
                                )
                            }
                        },
                    )
                    add(
                        buildJsonObject {
                            put("type", JsonPrimitive("user_input"))
                            putJsonArray("content") {
                                add(
                                    buildJsonObject {
                                        put("type", JsonPrimitive("text"))
                                        put("text", JsonPrimitive("What is the capital of France?"))
                                    },
                                )
                            }
                        },
                    )
                }

            val request =
                CreateInteractionRequest(
                    model = model,
                    input = inputJson,
                )
            val interaction = ai.createInteraction(request)
            printInteractionResult(interaction)
        } finally {
            if (client == null) {
                ai.close()
            }
        }
    }

    suspend fun runImageInput(
        client: GeminiAI? = null,
        imageProvider: () -> String = { getImage() },
    ) {
        val ai = client ?: GeminiAI(apiKey = getApiKey())
        try {
            println("--- Image Input ---")
            val base64Image = imageProvider()

            val textContent = InteractionContent(type = "text", text = "What is in this picture?")
            val imageContent = InteractionContent(type = "image", data = base64Image, mimeType = "image/jpeg")

            val inputList = listOf(textContent, imageContent)
            val inputJson = json.encodeToJsonElement(inputList)

            val request =
                CreateInteractionRequest(
                    model = model,
                    input = inputJson,
                )
            val interaction = ai.createInteraction(request)
            printInteractionResult(interaction)
        } finally {
            if (client == null) {
                ai.close()
            }
        }
    }

    suspend fun runFunctionCalling(client: GeminiAI? = null) {
        val ai = client ?: GeminiAI(apiKey = getApiKey())
        try {
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
                    model = model,
                    tools = arrayOf(tool),
                    input = JsonPrimitive("What is the weather like in Boston, MA?"),
                )
            val interaction = ai.createInteraction(request)
            printInteractionResult(interaction)
        } finally {
            if (client == null) {
                ai.close()
            }
        }
    }

    private fun printInteractionResult(interaction: Interaction) {
        println("Interaction ID: ${interaction.id}")
        println("Status: ${interaction.status}")
        println("Model: ${interaction.model ?: "N/A"}")
        println("Role: ${interaction.role ?: "N/A"}")
        println("Usage: ${interaction.usage ?: "N/A"}")
        println("Steps: ${interaction.steps?.size ?: 0}")

        printGeneratedContents(interaction)
    }

    private fun printGeneratedContents(interaction: Interaction) {
        val generatedText = interaction.outputText?.takeIf { it.isNotBlank() }
            if (generatedText != null) {
            println("Generated text:")
            println(generatedText)
        } else {
            val extractedStepText = extractTextFromSteps(interaction.steps)
            if (extractedStepText.isNotEmpty()) {
                println("Generated text from steps:")
                extractedStepText.forEachIndexed { index, text ->
                    println("[$index] $text")
                }
            } else {
                println("Generated text: N/A")
            }
        }

        val outputs = interaction.outputs
        if (outputs.isNullOrEmpty()) {
            println("No generated content items returned.")
            return
        }

        println("Generated content items:")
        outputs.forEach { content ->
            println("Type: ${content.type}")
            when (content.type) {
                "text" -> println("Text: ${content.text}")
                "image" -> println("Image: [Image Data]")
                "function_call" -> println("Function Call: ${content.name}(${content.arguments})")
                "thought" -> println("Thought: ${content.summary?.content?.text ?: "No summary"}")
                "code_execution_result" -> println("Code result: ${content.result}")
                else -> println("Content: $content")
            }
        }
    }

    private fun extractTextFromSteps(steps: Array<JsonElement>?): List<String> {
        if (steps.isNullOrEmpty()) {
            return emptyList()
        }

        val texts = mutableListOf<String>()
        steps.forEach { step ->
            collectText(step, texts)
        }
        return texts
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
    }

    private fun collectText(element: JsonElement, texts: MutableList<String>) {
        when (element) {
            is JsonPrimitive -> return
            is JsonArray -> element.forEach { collectText(it, texts) }
            is JsonObject -> {
                val stepType = (element["type"] as? JsonPrimitive)?.content
                if (stepType == "user_input") {
                    return
                }

                element.forEach { (key, value) ->
                    if (key == "text" || key == "output_text" || key == "content") {
                        if (value is JsonPrimitive && value.isString) {
                            texts.add(value.content)
                        }
                    }
                    collectText(value, texts)
                }
            }
        }
    }

    suspend fun runDeepResearch(client: GeminiAI? = null) {
        val ai = client ?: GeminiAI(apiKey = getApiKey())
        try {
            println("--- Deep Research ---")
            val request =
                CreateInteractionRequest(
                    agent = "deep-research-pro-preview-12-2025",
                    input = JsonPrimitive("Find a cure to cancer"),
                    background = true,
                )
            try {
                val interaction = ai.createInteraction(request)
                printInteractionResult(interaction)
            } catch (e: Exception) {
                println("Error: ${e.message}")
            }
        } finally {
            if (client == null) {
                ai.close()
            }
        }
    }
}
