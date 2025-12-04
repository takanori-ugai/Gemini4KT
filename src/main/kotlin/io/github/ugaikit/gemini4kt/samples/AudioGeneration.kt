package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.Base64

fun main() {
    val apiKey = System.getenv("GEMINI_API_KEY") ?: "YOUR_API_KEY"

    // Example 1: Single voice
    val config1 = generationConfig {
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
    val config2 = generationConfig {
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
    /*
    val gemini = Gemini(apiKey)
    val response = gemini.generateContent(
        model = "gemini-2.5-flash-preview-tts",
        contents = listOf(Content(parts = listOf(Part(text = "Say cheerfully: Have a wonderful day!")))),
        config = config1
    )

    val base64Audio = response.candidates[0].content.parts[0].inlineData?.data
    if (base64Audio != null) {
        val audioBytes = Base64.getDecoder().decode(base64Audio)
        File("out.pcm").writeBytes(audioBytes)
        println("Audio saved to out.pcm")
    }
    */
}
