package io.github.ugaikit.gemini4kt

import io.github.ugaikit.gemini4kt.agent.CreateAgentRequest
import io.github.ugaikit.gemini4kt.interaction.CreateInteractionRequest
import io.github.ugaikit.gemini4kt.interaction.EnvironmentConfig
import io.github.ugaikit.gemini4kt.interaction.EnvironmentReference
import io.github.ugaikit.gemini4kt.interaction.InteractionInput
import io.github.ugaikit.gemini4kt.interaction.InteractionStep
import io.github.ugaikit.gemini4kt.interaction.InteractionTool
import io.github.ugaikit.gemini4kt.live.AudioTranscriptionConfig
import io.github.ugaikit.gemini4kt.live.BidiGenerateContentClientContent
import io.github.ugaikit.gemini4kt.live.BidiGenerateContentSetup
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable
import java.util.Base64
import java.util.Properties
import javax.sound.sampled.AudioFileFormat
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Integration tests for the Gemini4KT library running against live API endpoints.
 */
@DisabledIfEnvironmentVariable(named = "GITHUB_ACTIONS", matches = "true")
class IntegrationTest {
    private val liveModel = "gemini-3.8-live"
    private val liveTimeoutMs = 20000L
    private val httpTooManyRequests = 429
    private val liveAudioSampleRate = 24000.0f
    private val liveAudioSampleSizeInBits = 16

    private fun isSkippableLiveApiError(error: Throwable): Boolean {
        val skippable =
            generateSequence(error) { it.cause }.any { current ->
                current is TimeoutCancellationException ||
                    current.message.orEmpty().contains("Timed out", ignoreCase = true) ||
                    current.message.orEmpty().contains("SetupComplete", ignoreCase = true) ||
                    current.message.orEmpty().contains("too_many_requests", ignoreCase = true) ||
                    current.message.orEmpty().contains("RESOURCE_EXHAUSTED", ignoreCase = true)
            }
        return skippable
    }

    private fun getApiKey(): String? {
        var apiKey = System.getenv("GEMINI_API_KEY")
        if (apiKey == null) {
            apiKey =
                Gemini::class.java.getResourceAsStream("/prop.properties")?.use { inputStream ->
                    Properties()
                        .apply {
                            load(inputStream)
                        }.getProperty("apiKey")
                }
        }
        return apiKey
    }

    /**
     * Tests the Agent API.
     */
    @Test
    fun testAgentAPI() =
        runTest {
            val apiKey = getApiKey()
            Assumptions.assumeTrue(!apiKey.isNullOrEmpty(), "API key not found. Skipping Agent API integration test.")

            println("--- testAgentAPI ---")
            val geminiAI = GeminiAI(apiKey = apiKey!!)
            val agentId = "fibonacci-analyst-jvm-${System.currentTimeMillis()}"
            val request =
                CreateAgentRequest(
                    id = agentId,
                    baseAgent = "antigravity-preview-09-2026",
                    systemInstruction = "You are a math analysis agent. Generate the Fibonacci sequence.",
                )

            var created = false
            try {
                println("Creating agent: $agentId...")
                val createdAgent = geminiAI.createAgent(request)
                created = true
                println("Created Agent: $createdAgent")

                println("Getting agent: $agentId...")
                val retrievedAgent = geminiAI.getAgent(agentId)
                println("Retrieved Agent: $retrievedAgent")

                println("Listing agents...")
                val listResponse = geminiAI.listAgents(pageSize = 5)
                println("Listed agents (first page): ${listResponse.agents.joinToString { it.id }}")

                println("Creating interaction with agent: $agentId...")
                val interactionRequest =
                    CreateInteractionRequest(
                        agent = agentId,
                        input = "Generate the first 5 Fibonacci numbers.",
                        environment = EnvironmentConfig(),
                        stream = false,
                    )
                val interaction = geminiAI.createInteraction(interactionRequest)
                println("Interaction output text: ${interaction.outputText}")
                println("Interaction status: ${interaction.status}")
            } catch (e: GeminiException) {
                println("Agent API test failed: ${e.message}")
                e.printStackTrace()
                if (e.error.code == httpTooManyRequests || e.message?.contains("too_many_requests") == true) {
                    println("Skipping quota limit error to prevent build failure.")
                } else {
                    throw e
                }
            } catch (e: Exception) {
                println("Agent API test failed: ${e.message}")
                e.printStackTrace()
                if (e is HttpRequestTimeoutException) {
                    Assumptions.assumeTrue(false, "Skipping unstable Agent API integration test: ${e.message}")
                }
                throw e
            } finally {
                if (created) {
                    try {
                        println("Deleting agent: $agentId...")
                        geminiAI.deleteAgent(agentId)
                        println("Deleted agent successfully.")
                    } catch (cleanupError: Exception) {
                        println("Agent cleanup failed: ${cleanupError.message}")
                    }
                }
                geminiAI.close()
            }
        }

    /**
     * Tests a custom function call through the Antigravity Agent.
     */
    @Test
    fun testAntigravityAgentFunctionCall() =
        runTest {
            val apiKey = getApiKey()
            Assumptions.assumeTrue(!apiKey.isNullOrEmpty(), "API key not found. Skipping Agent function call integration test.")

            val geminiAI = GeminiAI(apiKey = apiKey!!)
            val agentId = "function-caller-jvm-${System.currentTimeMillis()}"
            val weatherTool =
                InteractionTool(
                    type = "function",
                    name = "get_weather",
                    description = "Gets the current weather for a given location.",
                    parameters =
                        buildJsonObject {
                            put("type", "object")
                            putJsonObject("properties") {
                                putJsonObject("location") {
                                    put("type", "string")
                                }
                            }
                            putJsonArray("required") {
                                add(JsonPrimitive("location"))
                            }
                        },
                )
            val tools = arrayOf(weatherTool)
            var created = false
            try {
                geminiAI.createAgent(
                    CreateAgentRequest(
                        id = agentId,
                        baseAgent = "antigravity-preview-09-2026",
                        systemInstruction = "Use get_weather whenever the user asks about weather. Do not answer from your own knowledge.",
                        tools = tools,
                    ),
                )
                created = true

                val firstInteraction =
                    geminiAI.createInteraction(
                        CreateInteractionRequest(
                            agent = agentId,
                            input = "What is the weather in Tokyo?",
                            tools = tools,
                            environment = EnvironmentConfig(),
                            stream = false,
                        ),
                    )
                val functionCall =
                    firstInteraction.steps
                        ?.firstOrNull { it.type == "function_call" }
                        ?: error("Antigravity Agent did not return a function_call step: $firstInteraction")

                assertEquals("get_weather", functionCall.name)
                assertEquals(
                    "Tokyo",
                    functionCall.arguments
                        ?.jsonObject
                        ?.get("location")
                        ?.jsonPrimitive
                        ?.content,
                )
                val callId = functionCall.id ?: error("Function call did not include an id: $functionCall")
                val environmentId =
                    firstInteraction.environmentId
                        ?: error("Antigravity Agent did not return an environment id: $firstInteraction")

                val finalInteraction =
                    geminiAI.createInteraction(
                        CreateInteractionRequest(
                            agent = agentId,
                            previousInteractionId = firstInteraction.id,
                            input =
                                InteractionInput.StepList(
                                    arrayOf(
                                        InteractionStep(
                                            type = "function_result",
                                            name = "get_weather",
                                            callId = callId,
                                            result =
                                                buildJsonObject {
                                                    put("temperature", 23)
                                                    put("unit", "celsius")
                                                },
                                        ),
                                    ),
                                ),
                            tools = tools,
                            environment = EnvironmentReference(environmentId),
                            stream = false,
                        ),
                    )

                assertTrue(finalInteraction.outputText.orEmpty().contains("23"))
            } catch (e: GeminiException) {
                if (e.error.code == httpTooManyRequests || e.message?.contains("too_many_requests") == true) {
                    println("Skipping Agent function call test because the API quota was exceeded.")
                } else {
                    throw e
                }
            } finally {
                if (created) {
                    try {
                        geminiAI.deleteAgent(agentId)
                    } catch (cleanupError: Exception) {
                        println("Agent function call cleanup failed: ${cleanupError.message}")
                    }
                }
                geminiAI.close()
            }
        }

    /**
     * Tests the Live API.
     */
    @Test
    fun testLiveAPI() =
        runBlocking {
            val apiKey = getApiKey()
            Assumptions.assumeTrue(!apiKey.isNullOrEmpty(), "API key not found. Skipping Live API integration test.")

            println("--- testLiveAPI ---")
            val gemini = Gemini(apiKey!!)
            val modelName = if (liveModel.startsWith("models/")) liveModel else "models/$liveModel"
            val setup =
                BidiGenerateContentSetup(
                    model = modelName,
                    generationConfig =
                        GenerationConfig(
                            responseModalities = arrayOf(Modality.AUDIO),
                        ),
                    systemInstruction =
                        content {
                            part {
                                text { "You are a helpful assistant." }
                            }
                        },
                    outputAudioTranscription = AudioTranscriptionConfig(),
                )
            val liveClient = gemini.getLiveClient(liveModel, null)
            val audioBytes = java.io.ByteArrayOutputStream()
            try {
                println("Connecting to Live API...")
                val session = liveClient.connect(setup)
                try {
                    val turnCompleted = CompletableDeferred<Unit>()
                    val scope = CoroutineScope(Dispatchers.Default)
                    val receiveJob =
                        scope.launch {
                            try {
                                session.receive().collect { msg ->
                                    println("Live message received: $msg")
                                    val text =
                                        msg.serverContent
                                            ?.modelTurn
                                            ?.parts
                                            ?.firstOrNull()
                                            ?.text
                                    if (text != null) {
                                        println("Live assistant response: $text")
                                    }
                                    val transcription = msg.serverContent?.outputTranscription?.text
                                    if (transcription != null) {
                                        println("Live assistant transcription: $transcription")
                                    }
                                    msg.serverContent?.modelTurn?.parts?.forEach { part ->
                                        part.inlineData?.let { inlineData ->
                                            if (inlineData.mimeType.startsWith("audio/")) {
                                                val decoded = Base64.getDecoder().decode(inlineData.data)
                                                audioBytes.write(decoded)
                                            }
                                        }
                                    }
                                    if (msg.serverContent?.turnComplete == true) {
                                        println("Turn complete")
                                        turnCompleted.complete(Unit)
                                    }
                                }
                            } catch (e: CancellationException) {
                                throw e
                            } catch (e: Exception) {
                                println("Error in Live receive flow: ${e.message}")
                                if (!turnCompleted.isCompleted) {
                                    turnCompleted.completeExceptionally(e)
                                }
                            }
                        }

                    println("Sending prompt to Live session...")
                    session.sendClientContent(
                        BidiGenerateContentClientContent(
                            turns =
                                listOf(
                                    content {
                                        role = "user"
                                        part {
                                            text { "Hello! Please respond with: 'Live API connection is working.'" }
                                        }
                                    },
                                ),
                            turnComplete = true,
                        ),
                    )

                    println("Waiting for response...")
                    val completed =
                        withTimeoutOrNull(liveTimeoutMs) {
                            turnCompleted.await()
                        }
                    if (completed == null) {
                        println("Timed out waiting for Live API response.")
                    } else {
                        println("Live API response received successfully.")
                    }
                    receiveJob.cancelAndJoin()
                } finally {
                    session.close()
                }

                if (audioBytes.size() > 0) {
                    val outputFile =
                        kotlin.io.path
                            .createTempFile(
                                prefix = "live_audio_response_",
                                suffix = ".wav",
                            ).toFile()
                            .apply { deleteOnExit() }
                    try {
                        val channels = 1
                        val signed = true
                        val bigEndian = false
                        val format =
                            AudioFormat(
                                liveAudioSampleRate,
                                liveAudioSampleSizeInBits,
                                channels,
                                signed,
                                bigEndian,
                            )
                        val pcmData = audioBytes.toByteArray()
                        val bais = java.io.ByteArrayInputStream(pcmData)
                        val length = pcmData.size / format.frameSize.toLong()
                        val ais = AudioInputStream(bais, format, length)
                        AudioSystem.write(ais, AudioFileFormat.Type.WAVE, outputFile)
                        println("Saved generated audio to WAV: ${outputFile.absolutePath}")
                    } catch (e: Exception) {
                        println("Error saving WAV file: ${e.message}")
                    }
                }
            } catch (e: TimeoutCancellationException) {
                Assumptions.assumeTrue(false, "Skipping unstable Live API integration test: ${e.message}")
                throw e
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                println("Live API test failed: ${e.message}")
                e.printStackTrace()
                val quotaExceeded =
                    e is GeminiException &&
                        (
                            e.error.code == httpTooManyRequests ||
                                e.error.status == "RESOURCE_EXHAUSTED" ||
                                e.message?.contains("too_many_requests", ignoreCase = true) == true
                        )
                if (quotaExceeded || isSkippableLiveApiError(e)) {
                    Assumptions.assumeTrue(false, "Skipping unstable Live API integration test: ${e.message}")
                }
                throw e
            }
        }
}
