package io.github.ugaikit.gemini4kt.samples

import kotlinx.coroutines.runBlocking
import java.io.File
import java.util.Base64
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem

/**
 * Represents the live sample runner.
 */
object LiveSampleRunner {
    /**
     * Handles main.
     *
     * @param args The args.
     */
    @JvmStatic
    fun main(args: Array<String>) =
        runBlocking {
            val accumulatedAudio = java.io.ByteArrayOutputStream()
            val inputFile = File("voice-note.wav")
            val inputAudioBase64 =
                if (inputFile.exists()) {
                    val pcmData = readWavToPcm(inputFile)
                    Base64.getEncoder().encodeToString(pcmData)
                } else {
                    println("voice-note.wav missing")
                    null
                }

            LiveSample.run(
                inputAudioBase64,
                onAudioData = { base64AudioChunk ->
                    accumulatedAudio.write(Base64.getDecoder().decode(base64AudioChunk))
                },
            )

            if (accumulatedAudio.size() > 0) {
                savePcmToWav(accumulatedAudio.toByteArray(), "live_output.wav", 24000.0f, 1)
            }
        }
}

/**
 * Handles read wav to pcm.
 *
 * @param file The file.
 */
fun readWavToPcm(file: File): ByteArray {
    // Read WAV and convert to 16kHz 16bit mono PCM.
    // For this sample, we try to use AudioSystem.

    /**
     * Holds the audio input stream.
     */
    val audioInputStream = AudioSystem.getAudioInputStream(file)

    /**
     * Holds the format.
     */
    val format = audioInputStream.format

    // We want 16000Hz, 16bit, Mono.

    /**
     * Holds the target format.
     */
    val targetFormat = AudioFormat(16000f, 16, 1, true, false) // Little endian

    /**
     * Holds the converted stream.
     */
    val convertedStream = AudioSystem.getAudioInputStream(targetFormat, audioInputStream)

    return convertedStream.readAllBytes()
}
