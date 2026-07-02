package io.github.ugaikit.gemini4kt.samples

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.util.Base64
import javax.sound.sampled.AudioFileFormat
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem

/**
 * Runner for MusicGeneration sample.
 */
object MusicGenerationRunner {
    /**
     * Runs the MusicGeneration sample, collects base64-encoded PCM audio, and writes it to a WAV file.
     *
     * Creates the build/outputs directory if it does not exist, invokes MusicGeneration.run with an
     * onAudioData callback that decodes and accumulates PCM bytes in memory, and—if any audio was
     * received—saves the accumulated PCM to build/outputs/generated_music.wav using a 44.1 kHz sample
     * rate and 1 channel. Accumulating PCM in memory may consume significant memory for long sessions.
     */
    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            println("Running MusicGeneration Sample...")

            val outputDir = File("build/outputs")
            if (!outputDir.exists()) {
                if (!outputDir.mkdirs()) {
                    println("Warning: Failed to create output directory: ${outputDir.absolutePath}")
                }
            }
            val outputFile = File(outputDir, "generated_music.wav")
            val maxBufferedBytes = 10L * 1024L * 1024L
            val pcmData = java.io.ByteArrayOutputStream()

            MusicGeneration.run(
                onAudioData = { base64Data ->
                    val decoded = Base64.getDecoder().decode(base64Data)
                    if (pcmData.size().toLong() + decoded.size > maxBufferedBytes) {
                        throw CancellationException("PCM buffer limit reached.")
                    }
                    pcmData.write(decoded)
                },
            )

            if (pcmData.size() > 0) {
                // Assuming 44.1kHz for music, 1 channel. Adjust if needed.
                savePcmToWav(pcmData.toByteArray(), outputFile.absolutePath, 44100.0f, 1)
                println("Saved generated music to ${outputFile.absolutePath}")
            } else {
                println("No audio data received.")
            }
        }
    }

    /**
     * Saves PCM byte array as a WAV file.
     *
     * @param pcmData       Raw PCM data
     * @param filePath      Path to save the WAV file (e.g., "output.wav")
     * @param sampleRate    Sampling rate (e.g., 24000.0f or 44100.0f)
     * @param channels      Number of channels (1 for mono, 2 for stereo)
     */
    private fun savePcmToWav(
        pcmData: ByteArray,
        filePath: String,
        sampleRate: Float,
        channels: Int,
    ) {
        try {
            // Format specification (assuming standard 16-bit, Signed, Little Endian)
            // Gemini and similar AI audio are typically 16-bit mono
            val sampleSizeInBits = 16
            val signed = true
            val bigEndian = false

            val format =
                AudioFormat(
                    sampleRate,
                    sampleSizeInBits,
                    channels,
                    signed,
                    bigEndian,
                )

            // Read PCM data as input stream
            val bais = ByteArrayInputStream(pcmData)

            // Calculate data length (number of frames)
            val length = pcmData.size / format.frameSize.toLong()

            // Create AudioInputStream
            val ais = AudioInputStream(bais, format, length)

            // Write as WAV file
            val file = File(filePath)
            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, file)

            println("WAV file saved: $filePath")
        } catch (e: IOException) {
            println("Error saving WAV file: ${e.message}")
        }
    }
}
