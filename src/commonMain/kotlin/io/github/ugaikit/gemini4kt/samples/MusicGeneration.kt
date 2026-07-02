package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.getApiKey
import io.github.ugaikit.gemini4kt.live.music.LiveMusic
import io.github.ugaikit.gemini4kt.live.music.LiveMusicGenerationConfig
import io.github.ugaikit.gemini4kt.live.music.LiveMusicSession
import io.github.ugaikit.gemini4kt.live.music.WeightedPrompt
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collect

/**
 * Represents the music generation sample.
 */
object MusicGeneration {
    /**
     * Runs the Music Generation Sample.
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
}
