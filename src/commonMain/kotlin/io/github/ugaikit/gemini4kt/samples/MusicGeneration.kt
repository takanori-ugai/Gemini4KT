package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.GeminiAI
import io.github.ugaikit.gemini4kt.getApiKey
import io.github.ugaikit.gemini4kt.interaction.AudioResponseFormat
import io.github.ugaikit.gemini4kt.interaction.CreateInteractionRequest
import io.github.ugaikit.gemini4kt.interaction.Interaction
import io.github.ugaikit.gemini4kt.interaction.InteractionAudioContent
import io.github.ugaikit.gemini4kt.interaction.InteractionInput
import io.github.ugaikit.gemini4kt.live.music.LiveMusic
import io.github.ugaikit.gemini4kt.live.music.LiveMusicGenerationConfig
import io.github.ugaikit.gemini4kt.live.music.LiveMusicSession
import io.github.ugaikit.gemini4kt.live.music.WeightedPrompt
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collect
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Represents the music generation sample.
 */
object MusicGeneration {
    /**
     * Generates a music track with the Lyria 3.5 Interactions API.
     *
     * The callback receives base64-encoded audio data. Lyria returns MP3 by default;
     * the returned interaction also contains generated lyrics in [Interaction.outputText]
     * when the model produces them.
     *
     * @param prompt Text description of the requested music.
     * @param onAudioData Callback invoked for each generated audio block.
     * @param model Lyria model ID, such as `lyria-3.5` or `lyria-3-clip-preview`.
     * @param images Optional images to use as additional musical inspiration.
     * @param client Optional GeminiAI client for reuse or testing.
     * @param apiKey Optional API key used when [client] is null.
     * @return The completed interaction returned by the API.
     */
    suspend fun run(
        prompt: String,
        onAudioData: (String) -> Unit,
        model: String = LYRIA_3_5_MODEL,
        images: List<MusicGenerationImage> = emptyList(),
        client: GeminiAI? = null,
        apiKey: String? = null,
    ): Interaction {
        require(prompt.isNotBlank()) { "prompt must not be blank." }
        require(images.size <= MAX_IMAGE_COUNT) {
            "Lyria accepts at most $MAX_IMAGE_COUNT images."
        }

        val ai = client ?: GeminiAI(apiKey = apiKey ?: getApiKey())
        return try {
            val input =
                buildJsonArray {
                    add(
                        buildJsonObject {
                            put("type", "text")
                            put("text", prompt)
                        },
                    )
                    images.forEach { image ->
                        add(
                            buildJsonObject {
                                put("type", "image")
                                put("data", image.data)
                                put("mime_type", image.mimeType)
                            },
                        )
                    }
                }

            val interaction =
                ai.createInteraction(
                    CreateInteractionRequest(
                        model = model,
                        input = InteractionInput.RawJson(input),
                        responseFormat = AudioResponseFormat(),
                    ),
                )

            interaction.audioOutputs().forEach { audio ->
                audio.data?.let(onAudioData)
            }
            interaction
        } finally {
            if (client == null) {
                ai.close()
            }
        }
    }

    /**
     * Runs the legacy Live Music API sample.
     *
     * @param onAudioData Callback to handle received audio data (Base64 encoded string).
     * @param liveMusicClient Optional LiveMusic client for testing.
     * @param apiKey Optional API key override; when null, the platform API key lookup is used.
     */
    suspend fun run(
        onAudioData: (String) -> Unit,
        liveMusicClient: LiveMusic? = null,
        apiKey: String? = null,
        sessionFactory: (suspend () -> LiveMusicSession)? = null,
    ) {
        val resolvedApiKey = apiKey ?: getApiKey()
        if (resolvedApiKey.isBlank()) {
            println("GEMINI_API_KEY not found. Skipping API call.")
            return
        }

        // Use a valid model for music generation, e.g., "lyria-realtime-exp" or similar if available
        // or documented.
        // Assuming "models/lyria-realtime-exp" based on the TypeScript example.
        val musicModel = "lyria-realtime-exp"

        try {
            val session: LiveMusicSession =
                when {
                    sessionFactory != null -> sessionFactory()
                    else -> {
                        val client = liveMusicClient ?: LiveMusic(resolvedApiKey, musicModel)
                        client.connect()
                    }
                }

            println("Connected to Music API.")

            // 1. Set Weighted Prompts
            val prompts =
                listOf(
                    WeightedPrompt("Upbeat electronic dance music with a driving beat.", 0.8),
                    WeightedPrompt("Hints of classical violin.", 0.2),
                )
            session.setWeightedPrompts(prompts)
            println("Sent weighted prompts.")

            // 2. Set Configuration
            val config =
                LiveMusicGenerationConfig(
                    temperature = 0.5,
                    bpm = 120,
                )
            session.setMusicGenerationConfig(config)
            println("Sent generation config.")

            // 3. Start Playback
            session.play()
            println("Started playback.")

            // 4. Receive Audio
            // We'll listen for a bit and then stop.
            try {
                // Launch a coroutine to stop after some time?
                // Or just collect for a while.
                // Let's rely on receiving chunks.
                var chunksReceived = 0
                session.receive().collect { msg ->
                    msg.serverContent?.audioChunks?.forEach { chunk ->
                        chunk.data?.let { data ->
                            onAudioData(data)
                            chunksReceived++
                            print(".")
                        }
                    }

                    // Stop after receiving some chunks for demonstration
                    if (chunksReceived > 50) { // Arbitrary number of chunks
                        println("\nReceived enough chunks, stopping.")
                        throw CancellationException("Sample complete")
                    }
                }
            } catch (e: CancellationException) {
                println("Music generation stopped.")
            } catch (e: Exception) {
                println("Error during receiving: ${e.message}")
            } finally {
                session.stop()
                session.close()
                println("Session closed.")
            }
        } catch (e: Exception) {
            println("Error in MusicGeneration: ${e.message}")
            println(e.stackTraceToString())
        }
    }

    /**
     * Resolves generated audio blocks in timeline order, with aggregate fallbacks.
     *
     * @return Audio blocks emitted by model output steps, aggregate outputs, or output audio.
     */
    private fun Interaction.audioOutputs(): List<InteractionAudioContent> {
        val stepOutputs =
            steps
                ?.filter { it.type == "model_output" }
                ?.flatMap { step ->
                    step
                        .content
                        .orEmpty()
                        .filter { it.type == "audio" }
                        .map { content ->
                            InteractionAudioContent(
                                type = content.type,
                                data = content.data,
                                uri = content.uri,
                                mimeType = content.mimeType,
                            )
                        }
                }.orEmpty()
        if (stepOutputs.isNotEmpty()) return stepOutputs

        val outputContents =
            outputs
                ?.filter { it.type == "audio" }
                ?.map { content ->
                    InteractionAudioContent(
                        type = content.type,
                        data = content.data,
                        uri = content.uri,
                        mimeType = content.mimeType,
                    )
                }.orEmpty()
        if (outputContents.isNotEmpty()) return outputContents

        return outputAudio?.let(::listOf).orEmpty()
    }

    private const val MAX_IMAGE_COUNT = 10
    const val LYRIA_3_5_MODEL = "lyria-3.5"
}

/**
 * Base64-encoded image input for Lyria music generation.
 *
 * @property data Base64-encoded image bytes.
 * @property mimeType Image MIME type, for example `image/jpeg`.
 */
data class MusicGenerationImage(
    val data: String,
    val mimeType: String,
)
