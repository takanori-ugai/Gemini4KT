package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.interaction.Interaction
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
     *
     * @param args Command-line arguments, reserved for future runner options.
     */
    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking { run() }
    }

    /**
     * Runs the file-writing portion of the sample.
     *
     * @param outputDir Directory where `generated_music.mp3` is written.
     * @param musicGenerator Generator callback that emits base64 audio and returns its interaction.
     * @return The generated file, or null when no audio was returned.
     */
    internal suspend fun run(
        outputDir: File = File("build/outputs"),
        musicGenerator: suspend ((String) -> Unit) -> Interaction = { onAudioData ->
            MusicGeneration.run(
                prompt = "An upbeat electronic track with a driving beat and hints of classical violin.",
                onAudioData = onAudioData,
            )
        },
    ): File? {
        println("Running MusicGeneration Sample...")

        if (!outputDir.exists() && !outputDir.mkdirs()) {
            println("Warning: Failed to create output directory: ${outputDir.absolutePath}")
        }
        val outputFile = File(outputDir, "generated_music.mp3")
        val audioData = ByteArrayOutputStream()

        val interaction =
            musicGenerator { base64Data ->
                audioData.write(Base64.getDecoder().decode(base64Data))
            }

        if (audioData.size() > 0) {
            outputFile.writeBytes(audioData.toByteArray())
            println("Saved generated music to ${outputFile.absolutePath}")
        } else {
            println("No audio data received.")
        }
        interaction.outputText?.let { println("Lyrics:\n$it") }
        return outputFile.takeIf { audioData.size() > 0 }
    }
}
