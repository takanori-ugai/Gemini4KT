@file:Suppress("TooManyFunctions")

package io.github.ugaikit.gemini4kt

import io.github.ugaikit.gemini4kt.agent.CreateAgentRequest
import io.github.ugaikit.gemini4kt.interaction.CreateInteractionRequest
import io.github.ugaikit.gemini4kt.live.AudioTranscriptionConfig
import io.github.ugaikit.gemini4kt.live.BidiGenerateContentClientContent
import io.github.ugaikit.gemini4kt.live.BidiGenerateContentSetup
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import java.io.File
import java.util.Base64
import java.util.Properties
import javax.sound.sampled.AudioFileFormat
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem

/**
 * Holds the embed model.
 */
private const val EMBED_MODEL = "gemini-embedding-2"

/**
 * Holds the flash model.
 */
private const val FLASH_MODEL = "gemini-3.1-flash-lite"

/**
 * Holds the pro model.
 */
private const val PRO_MODEL = "gemini-3.1-flash-lite"

/**
 * Holds the live model.
 */
private const val LIVE_MODEL = "gemini-3.1-flash-live-preview"

/**
 * Holds the live session timeout.
 */
private const val LIVE_TIMEOUT_MS = 20000L

/**
 * Holds the HTTP status code for Too Many Requests.
 */
private const val HTTP_TOO_MANY_REQUESTS = 429

/**
 * Holds the default sample rate for live audio.
 */
private const val LIVE_AUDIO_SAMPLE_RATE = 24000.0f

/**
 * Holds the default sample size in bits for live audio.
 */
private const val LIVE_AUDIO_SAMPLE_SIZE_IN_BITS = 16

/**
 * Tests test content generation.
 *
 * @param gemini The gemini.
 */
private suspend fun testContentGeneration(gemini: Gemini) {
    println("--- testGenerateContent ---")
    /**
     * Holds the text.
     */
    val text = "Write a story about a magic backpack."

    /**
     * Holds the input json.
     */
    val inputJson =
        generateContentRequest {
            content { part { text { text } } }
            safetySetting {
                category = HarmCategory.HARM_CATEGORY_HARASSMENT
                threshold = Threshold.BLOCK_ONLY_HIGH
            }
        }

    /**
     * Holds the response.
     */
    val response = gemini.generateContent(inputJson, model = FLASH_MODEL)
    println(
        response.candidates
            .firstOrNull()
            ?.content
            ?.parts
            ?.firstOrNull()
            ?.text
            ?.replace("\n\n", "\n"),
    )

    println("--- testCountTokens ---")
    /**
     * Holds the input json2.
     */
    val inputJson2 =
        CountTokensRequest(
            contents = listOf(Content(parts = arrayOf(Part(text)))),
        )
    println(gemini.countTokens(inputJson2))

    println("--- testEmbedContent ---")
    /**
     * Holds the embed request.
     */
    val embedRequest =
        EmbedContentRequest(
            content = Content(parts = arrayOf(Part(text))),
            model = "models/$EMBED_MODEL",
        )
    println(gemini.embedContent(embedRequest, model = EMBED_MODEL))

    println("--- testBatchEmbedContent ---")
    /**
     * Holds the batch embed request.
     */
    val batchEmbedRequest =
        BatchEmbedRequest(
            requests =
                listOf(
                    EmbedContentRequest(
                        content = Content(parts = arrayOf(Part(text))),
                        model = "models/$EMBED_MODEL",
                    ),
                ),
        )
    println(gemini.batchEmbedContents(batchEmbedRequest, model = EMBED_MODEL))
}

/**
 * Tests test models and content.
 *
 * @param gemini The gemini.
 */
private suspend fun testModelsAndContent(gemini: Gemini) {
    println("--- testGetModels ---")
    println(gemini.getModels())

    println("--- testGenerateContentWithImage ---")
    /**
     * Holds the path.
     */
    val path = Gemini::class.java.getResource("/scones.jpg")

    /**
     * Holds the image.
     */
    val image = File(path.toURI())

    /**
     * Holds the base64 image.
     */
    val base64Image = Base64.getEncoder().encodeToString(image.readBytes())

    /**
     * Holds the input with image.
     */
    val inputWithImage =
        GenerateContentRequest(
            contents =
                arrayOf(
                    Content(
                        parts =
                            arrayOf(
                                Part(text = "What is this picture?"),
                                Part(
                                    inlineData =
                                        InlineData(
                                            mimeType = "image/jpeg",
                                            data = base64Image,
                                        ),
                                ),
                            ),
                    ),
                ),
        )

    /**
     * Holds the response.
     */
    val response = gemini.generateContent(inputWithImage, PRO_MODEL)
    println(
        response.candidates
            .firstOrNull()
            ?.content
            ?.parts
            ?.firstOrNull()
            ?.text
            ?.replace("\n\n", "\n"),
    )
}

/**
 * Handles find movies function.
 */
private fun findMoviesFunction(): FunctionDeclaration =
    functionDeclaration {
        name = "find_movies"
        description = "find movie titles currently playing in theaters " +
            "based on any description, genre, title words, etc."
        parameters {
            type = "object"
            property("location") {
                type = "string"
                description = "The city and state, e.g. San Francisco, CA or a zip code e.g. 95616"
            }
            property("description") {
                type = "string"
                description = "Any kind of description including category or genre"
            }
            required("description")
        }
    }

/**
 * Handles find theaters function.
 */
private fun findTheatersFunction(): FunctionDeclaration =
    functionDeclaration {
        name = "find_theaters"
        description = "find theaters based on location and optionally movie title " +
            "which is currently playing in theaters"
        parameters {
            type = "object"
            property("location") {
                type = "string"
                description = "The city and state, e.g. San Francisco, CA or a zip code e.g. 95616"
            }
            property("movie") {
                type = "string"
                description = "Any movie title"
            }
            required("location")
        }
    }

/**
 * Handles get showtimes function.
 */
private fun getShowtimesFunction(): FunctionDeclaration =
    FunctionDeclaration(
        name = "get_showtimes",
        description = "Find the start times for movies playing in a specific theater",
        parameters =
            Schema(
                type = "object",
                properties =
                    mapOf(
                        "location" to
                            Schema(
                                type = "string",
                                description = "The city and state, e.g. San Francisco, CA or a zip code e.g. 95616",
                            ),
                        "movie" to
                            Schema(
                                type = "string",
                                description = "Any movie title",
                            ),
                        "theater" to
                            Schema(
                                type = "string",
                                description = "Name of the theater",
                            ),
                        "date" to
                            Schema(
                                type = "string",
                                description = "Date for requested showtime",
                            ),
                    ),
                required = listOf("location", "movie", "theater", "date"),
            ),
    )

/**
 * Handles define function tools.
 */
private fun defineFunctionTools(): Array<Tool> =
    arrayOf(
        Tool(
            functionDeclarations =
                arrayOf(
                    findMoviesFunction(),
                    findTheatersFunction(),
                    getShowtimesFunction(),
                ),
        ),
    )

/**
 * Tests test function calling first turn.
 *
 * @param gemini The gemini.
 * @param tools The tools.
 */
private suspend fun testFunctionCallingFirstTurn(
    gemini: Gemini,
    tools: Array<Tool>,
) {
    println("--- testFunctionCallingFirstTurn ---")
    /**
     * Holds the ex function.
     */
    val exFunction =
        GenerateContentRequest(
            contents =
                arrayOf(
                    content {
                        role = "user"
                        part {
                            text {
                                "Which theaters in Mountain View show Barbie movie?"
                            }
                        }
                    },
                ),
            tools = tools,
        )

    println(
        gemini
            .generateContent(exFunction, PRO_MODEL)
            .candidates
            .firstOrNull()
            ?.content
            ?.parts
            ?.firstOrNull(),
    )
}

/**
 * Tests test function calling second turn.
 *
 * @param gemini The gemini.
 * @param tools The tools.
 */
private suspend fun testFunctionCallingSecondTurn(
    gemini: Gemini,
    tools: Array<Tool>,
) {
    println("--- testFunctionCallingSecondTurn ---")
    /**
     * Holds the content.
     */
    val content =
        buildJsonObject {
            put("name", "the_theater")
            putJsonObject("content") {
                put("movie", "Barbie")
                putJsonArray("theaters") {
                    addJsonObject {
                        put("name", "AMC Mountain View 16")
                        put("address", "2000 W El Camino Real, Mountain View, CA 94040")
                    }
                    addJsonObject {
                        put("name", "Regal Edwards 14")
                        put("address", "245 Castro St, Mountain View, CA 94040")
                    }
                }
            }
        }

    val exFunction =
        GenerateContentRequest(
            contents =
                arrayOf(
                    content {
                        role = "user"
                        part { text { "Which theaters in Mountain View show Barbie movie?" } }
                    },
                ),
            tools = tools,
        )
    val firstTurnResponse = gemini.generateContent(exFunction, PRO_MODEL)
    val modelPart =
        firstTurnResponse.candidates
            .firstOrNull()
            ?.content
            ?.parts
            ?.firstOrNull()
            ?: error("Model response or parts are null in the first turn.")

    /**
     * Holds the ex function2.
     */
    val exFunction2 =
        GenerateContentRequest(
            contents =
                arrayOf(
                    content {
                        role = "user"
                        part { text { "Which theaters in Mountain View show Barbie movie?" } }
                    },
                    Content(
                        parts = arrayOf(modelPart),
                        role = "model",
                    ),
                    content {
                        role = "function"
                        part {
                            functionResponse {
                                FunctionResponse(
                                    name = "find_theaters",
                                    response = content,
                                )
                            }
                        }
                    },
                ),
            tools = tools,
        )

    println(
        gemini
            .generateContent(exFunction2, PRO_MODEL)
            .candidates
            .firstOrNull()
            ?.content
            ?.parts
            ?.firstOrNull(),
    )
}

/**
 * Tests test part builder.
 */
private fun testPartBuilder() {
    println("--- testPartBuilder ---")
    /**
     * Holds the example part.
     */
    val examplePart =
        part {
            text { "This is an example text." }
            inlineData {
                mimeType { "text/plain" }
                data { "This is an example inline data." }
            }
        }
    println(examplePart)
}

/**
 * Tests the Agent API.
 *
 * @param apiKey The API key.
 */
private suspend fun testAgentAPI(apiKey: String) {
    println("--- testAgentAPI ---")
    val geminiAI = GeminiAI(apiKey = apiKey)
    val agentId = "fibonacci-analyst-jvm-${System.currentTimeMillis()}"
    val request =
        CreateAgentRequest(
            id = agentId,
            baseAgent = "antigravity-preview-05-2026",
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
                input = JsonPrimitive("Generate the first 5 Fibonacci numbers."),
                environment = buildJsonObject { put("type", "remote") },
                stream = false,
            )
        val interaction = geminiAI.createInteraction(interactionRequest)
        println("Interaction output text: ${interaction.outputText}")
        println("Interaction status: ${interaction.status}")
    } catch (e: GeminiException) {
        println("Agent API test failed: ${e.message}")
        e.printStackTrace()
        if (e.error.code == HTTP_TOO_MANY_REQUESTS || e.message?.contains("too_many_requests") == true) {
            println("Skipping quota limit error to prevent build failure.")
        } else {
            throw e
        }
    } catch (e: Exception) {
        println("Agent API test failed: ${e.message}")
        e.printStackTrace()
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
    }
}

/**
 * Tests the Live API.
 *
 * @param apiKey The API key.
 */
private suspend fun testLiveAPI(apiKey: String) {
    println("--- testLiveAPI ---")
    val gemini = Gemini(apiKey)
    val modelName = if (LIVE_MODEL.startsWith("models/")) LIVE_MODEL else "models/$LIVE_MODEL"
    val setup =
        BidiGenerateContentSetup(
            model = modelName,
            generationConfig =
                io.github.ugaikit.gemini4kt.GenerationConfig(
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
    val liveClient = gemini.getLiveClient(LIVE_MODEL, null)
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
                withTimeoutOrNull(LIVE_TIMEOUT_MS) {
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
            val outputFile = File("live_audio_response.wav")
            try {
                val channels = 1
                val signed = true
                val bigEndian = false
                val format =
                    AudioFormat(
                        LIVE_AUDIO_SAMPLE_RATE,
                        LIVE_AUDIO_SAMPLE_SIZE_IN_BITS,
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
    } catch (e: Throwable) {
        println("Live API test failed/skipped: ${e.message}")
        e.printStackTrace()
    }
}

/**
 * Handles main.
 */
fun main() =
    runBlocking {
        var apiKey = System.getenv("GEMINI_API_KEY")
        if (apiKey == null) {
            apiKey =
                Gemini::class.java.getResourceAsStream("/prop.properties").use { inputStream ->
                    Properties()
                        .apply {
                            load(inputStream)
                        }.getProperty("apiKey")
                }
        }
        if (apiKey.isNullOrEmpty()) {
            println("API key not found. Please set the GEMINI_API_KEY environment variable.")
            return@runBlocking
        }
        val gemini = Gemini(apiKey)
        val tools = defineFunctionTools()

        testContentGeneration(gemini)
        testModelsAndContent(gemini)
//    testCachedContent(gemini)
        testFunctionCallingFirstTurn(gemini, tools)
        testFunctionCallingSecondTurn(gemini, tools)
        testPartBuilder()
        testAgentAPI(apiKey)
        testLiveAPI(apiKey)
    }

/**
 * Represents the ittest.
 */
class ITTest
