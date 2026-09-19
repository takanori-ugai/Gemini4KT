package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.interaction.Interaction
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
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
                retainAudioData = false,
            )
        },
    ): File? {
        println("Running MusicGeneration Sample...")

        if (!outputDir.exists()) outputDir.mkdirs()
        if (!outputDir.isDirectory) {
            throw IOException("Output path is not a directory: ${outputDir.absolutePath}")
        }
        val outputFile = File(outputDir, "generated_music.mp3")
        val temporaryFile = File.createTempFile("generated_music-", ".mp3", outputDir)
        var moved = false
        var hasAudio = false

        try {
            val interaction =
                temporaryFile.outputStream().buffered().use { output ->
                    musicGenerator { base64Data ->
                        val decodedAudio = Base64.getDecoder().decode(base64Data)
                        if (decodedAudio.isNotEmpty()) {
                            output.write(decodedAudio)
                            hasAudio = true
                        }
                    }
                }

            if (!hasAudio) {
                println("No audio data received.")
                interaction.outputText?.let { println("Lyrics:\n$it") }
                return null
            }

            Files.move(
                temporaryFile.toPath(),
                outputFile.toPath(),
                StandardCopyOption.REPLACE_EXISTING,
            )
            moved = true
            println("Saved generated music to ${outputFile.absolutePath}")
            interaction.outputText?.let { println("Lyrics:\n$it") }
            return outputFile
        } finally {
            if (!moved) {
                temporaryFile.delete()
            }
        }
    }
}
