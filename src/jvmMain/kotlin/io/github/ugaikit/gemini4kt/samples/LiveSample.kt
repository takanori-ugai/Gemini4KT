package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.Gemini
import io.github.ugaikit.gemini4kt.Modality
import io.github.ugaikit.gemini4kt.content
import io.github.ugaikit.gemini4kt.live.BidiGenerateContentRealtimeInput
import io.github.ugaikit.gemini4kt.live.Blob
import io.github.ugaikit.gemini4kt.live.LiveConnectConfig
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.util.Base64
import java.util.Properties
import javax.sound.sampled.AudioFileFormat
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem

object LiveSample {
    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            runSample()
        }
    }

    // Helper to move accumulatedAudio out
    suspend fun runSample() {
        val apiKey =
            Gemini::class.java.getResourceAsStream("/prop.properties").use { inputStream ->
                if (inputStream == null) return@use null
                Properties().apply { load(inputStream) }.getProperty("apiKey")
            }

        if (apiKey == null) {
            println("GEMINI_API_KEY not found. Skipping.")
            return
        }

        val liveModel = "gemini-2.5-flash-native-audio-preview-09-2025"
        val config =
            LiveConnectConfig(
                responseModalities = listOf(Modality.AUDIO),
                systemInstruction = content { part { text { "You are a helpful assistant and answer in a friendly tone." } } },
            )

        val gemini = Gemini(apiKey)
        val liveClient = gemini.getLiveClient(liveModel, config)
        val accumulatedAudio = java.io.ByteArrayOutputStream()

        try {
            val session = liveClient.connect()
            val inputFile = File("voice-note.wav")
            if (inputFile.exists()) {
                val pcmData = readWavToPcm(inputFile)
                val base64Audio = Base64.getEncoder().encodeToString(pcmData)
                session.sendRealtimeInput(
                    BidiGenerateContentRealtimeInput(
                        audio = Blob(data = base64Audio, mimeType = "audio/pcm;rate=16000"),
                    ),
                )
            } else {
                println("voice-note.wav missing")
            }

            try {
                session.receive().collect { msg ->
                    if (msg.serverContent?.turnComplete == true) {
                        throw kotlinx.coroutines.CancellationException("Turn complete")
                    }
                    println(msg)
                    msg.serverContent?.modelTurn?.parts?.forEach { part ->
                        part.inlineData?.let {
                            if (it.mimeType.startsWith("audio")) {
                                accumulatedAudio.write(Base64.getDecoder().decode(it.data))
                            }
                        }
                    }
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                println("Turn completed.")
            }

            session.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (accumulatedAudio.size() > 0) {
            savePcmToWav(accumulatedAudio.toByteArray(), "live_output.wav", 24000.0f, 1)
        }
    }

    fun readWavToPcm(file: File): ByteArray {
        // Read WAV and convert to 16kHz 16bit mono PCM.
        // For this sample, we try to use AudioSystem.
        val audioInputStream = AudioSystem.getAudioInputStream(file)
        val format = audioInputStream.format

        // We want 16000Hz, 16bit, Mono.
        val targetFormat = AudioFormat(16000f, 16, 1, true, false) // Little endian
        val convertedStream = AudioSystem.getAudioInputStream(targetFormat, audioInputStream)

        return convertedStream.readAllBytes()
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
}
