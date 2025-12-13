package io.github.ugaikit.gemini4kt.samples

import kotlinx.coroutines.runBlocking
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.util.Base64
import javax.sound.sampled.AudioFileFormat
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem

object AudioGenerationRunner {
    @JvmStatic
    fun main(args: Array<String>) =
        runBlocking {
            val base64Audio = AudioGeneration.run()
            if (base64Audio != null) {
                val audioBytes = Base64.getDecoder().decode(base64Audio)
                savePcmToWav(audioBytes, "output.wav", 24000.0f, 1)
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
fun savePcmToWav(
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
