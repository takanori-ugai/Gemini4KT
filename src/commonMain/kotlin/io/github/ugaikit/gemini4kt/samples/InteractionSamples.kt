package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.GeminiAI
import io.github.ugaikit.gemini4kt.getApiKey
import io.github.ugaikit.gemini4kt.getImage
import io.github.ugaikit.gemini4kt.interaction.CreateInteractionRequest
import io.github.ugaikit.gemini4kt.interaction.Interaction
import io.github.ugaikit.gemini4kt.interaction.InteractionContent
import io.github.ugaikit.gemini4kt.interaction.InteractionInput
import io.github.ugaikit.gemini4kt.interaction.InteractionTool
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

private const val MODEL = "gemma-4-26b-a4b-it"

object InteractionSamples {
    suspend fun runSimple(client: GeminiAI? = null) {
        val ai = client ?: GeminiAI(apiKey = getApiKey())
        try {
            println("--- Simple Request ---")
            val request =
                CreateInteractionRequest(
                    model = MODEL,
                    input = "Reply with exactly one short sentence that says hello from the sample runner.",
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
            val request =
                CreateInteractionRequest(
                    model = MODEL,
                    input =
                        InteractionInput.StepList(
                            arrayOf(
                                io.github.ugaikit.gemini4kt.interaction.InteractionStep(
                                    type = "user_input",
                                    content =
                                        arrayOf(
                                            InteractionContent(type = "text", text = "Hello! Reply with a very short friendly greeting."),
                                        ),
                                ),
                                io.github.ugaikit.gemini4kt.interaction.InteractionStep(
                                    type = "model_output",
                                    content =
                                        arrayOf(
                                            InteractionContent(type = "text", text = "Hi there!"),
                                        ),
                                ),
                                io.github.ugaikit.gemini4kt.interaction.InteractionStep(
                                    type = "user_input",
                                    content =
                                        arrayOf(
                                            InteractionContent(type = "text", text = "What is the capital of France? Answer in one short sentence."),
                                        ),
                                ),
                            ),
                        ),
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

            val request =
                CreateInteractionRequest(
                    model = MODEL,
                    input =
                        InteractionInput.StepList(
                            arrayOf(
                                io.github.ugaikit.gemini4kt.interaction.InteractionStep(
                                    type = "user_input",
                                    content =
                                        arrayOf(
                                            InteractionContent(type = "text", text = "What is in this picture?"),
                                            InteractionContent(
                                                type = "image",
                                                data = base64Image,
                                                mimeType = "image/jpeg",
                                            ),
                                        ),
                                ),
                            ),
                        ),
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
                    model = MODEL,
                    tools = arrayOf(tool),
                    input = "What is the weather like in Boston, MA?",
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
        printGeneratedSteps(interaction)
    }

    private fun printGeneratedContents(interaction: Interaction) {
        println("Output text: ${interaction.outputText ?: "N/A"}")
        println("Output text length: ${interaction.outputText?.length ?: 0}")
        val outputs = interaction.outputs
        if (outputs.isNullOrEmpty()) {
            println("No generated content items returned.")
            return
        }

        println("Generated content items:")
        outputs.forEach { content ->
            println("Type: ${content.type}")
            when (content.type) {
                "text" -> println("Text length: ${content.text?.length ?: 0}")
                "image" -> println("Image payload present: ${content.data != null}")
                "function_call" -> println("Function call name: ${content.name ?: "N/A"}")
                "thought" -> println("Thought summary present: ${content.summary != null}")
                "code_execution_result" -> println("Code result present: ${content.result != null}")
                else -> println("Content type logged without payload details.")
            }
        }
    }

    private fun printGeneratedSteps(interaction: Interaction) {
        val steps = interaction.steps
        if (steps.isNullOrEmpty()) {
            println("No interaction steps returned.")
            return
        }

        println("Interaction steps:")
        steps.forEachIndexed { index, step ->
            println("Step[$index] type: ${step.type}")
            val content = step.content
            if (content.isNullOrEmpty()) {
                println("  No step content returned.")
            } else {
                content.forEachIndexed { contentIndex, item ->
                    val summary =
                        when {
                            !item.text.isNullOrBlank() -> "text=${item.text}"
                            !item.data.isNullOrBlank() -> "data=<${item.type ?: "content"} payload>"
                            !item.name.isNullOrBlank() -> "name=${item.name}"
                            else -> item.type ?: "unknown"
                        }
                    println("  Content[$contentIndex]: $summary")
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
                    input = "Find a cure to cancer",
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
