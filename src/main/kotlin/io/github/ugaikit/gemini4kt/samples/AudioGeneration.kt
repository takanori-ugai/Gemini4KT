package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.Content
import io.github.ugaikit.gemini4kt.Gemini
import io.github.ugaikit.gemini4kt.GenerateContentRequest
import io.github.ugaikit.gemini4kt.Modality
import io.github.ugaikit.gemini4kt.Part
import io.github.ugaikit.gemini4kt.generationConfig
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.util.Base64
import java.util.Properties
import javax.sound.sampled.AudioFileFormat
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem

fun main() {
    // Example 1: Single voice
    val config1 =
        generationConfig {
            responseModality(Modality.AUDIO)
            speechConfig {
                voiceConfig {
                    prebuiltVoiceConfig {
                        voiceName { "Kore" }
                    }
                }
            }
        }

    println("Config 1 JSON:")
    val json = Json { prettyPrint = true }
    println(json.encodeToString(config1))

    // Example 2: Multi-speaker
    val config2 =
        generationConfig {
            responseModality(Modality.AUDIO)
            speechConfig {
                multiSpeakerVoiceConfig {
                    speakerVoiceConfig {
                        speaker { "Joe" }
                        voiceConfig {
                            prebuiltVoiceConfig {
                                voiceName { "Kore" }
                            }
                        }
                    }
                    speakerVoiceConfig {
                        speaker { "Jane" }
                        voiceConfig {
                            prebuiltVoiceConfig {
                                voiceName { "Puck" }
                            }
                        }
                    }
                }
            }
        }

    println("\nConfig 2 JSON:")
    println(json.encodeToString(config2))

    // Note: To actually run this, we would need to call the API.
    // Since I'm in a sandbox without the proper API key/environment for this specific model,
    // I am demonstrating the configuration construction.

    // Hypothetical usage if we had the client setup for "gemini-2.5-flash-preview-tts"

    val apiKey =
        Gemini::class.java.getResourceAsStream("/prop.properties").use { inputStream ->
            Properties()
                .apply {
                    load(inputStream)
                }.getProperty("apiKey")
        }
    if (apiKey != null) {
        val gemini = Gemini(apiKey)
        try {
            val response =
                gemini.generateContent(
                    model = "gemini-2.5-flash-preview-tts",
                    inputJson =
                        GenerateContentRequest(
                            contents = listOf(Content(role = "user", parts = listOf(Part(text = "Say cheerfully: Have a wonderful day!")))),
                            generationConfig = config1,
                        ),
                )

            val base64Audio =
                response.candidates
                    ?.get(0)
                    ?.content
                    ?.parts
                    ?.get(0)
                    ?.inlineData
                    ?.data
            if (base64Audio != null) {
                val audioBytes = Base64.getDecoder().decode(base64Audio)
                savePcmToWav(audioBytes, "output.wav", 24000.0f, 1)
            }
        } catch (e: Exception) {
            println("Failed to generate audio: ${e.message}")
        }
    } else {
        println("GEMINI_API_KEY not found. Skipping API call.")
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
        e.printStackTrace()
    }
}
