package io.github.ugaikit.gemini4kt.samples

import kotlinx.coroutines.runBlocking
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.Base64

/**
 * Runner for the Lyria 3.5 music generation sample.
 */
object MusicGenerationRunner {
    /**
     * Generates a track through the Interactions API and saves the returned MP3.
     */
    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            println("Running MusicGeneration Sample...")

            val outputDir = File("build/outputs")
            if (!outputDir.exists() && !outputDir.mkdirs()) {
                println("Warning: Failed to create output directory: ${outputDir.absolutePath}")
            }
            val outputFile = File(outputDir, "generated_music.mp3")
            val audioData = ByteArrayOutputStream()

            val interaction =
                MusicGeneration.run(
                    prompt = "An upbeat electronic track with a driving beat and hints of classical violin.",
                    onAudioData = { base64Data ->
                        audioData.write(Base64.getDecoder().decode(base64Data))
                    },
                )

            if (audioData.size() > 0) {
                outputFile.writeBytes(audioData.toByteArray())
                println("Saved generated music to ${outputFile.absolutePath}")
            } else {
                println("No audio data received.")
            }
            interaction.outputText?.let { println("Lyrics:\n$it") }
        }
    }
}
