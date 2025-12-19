package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.Content
import io.github.ugaikit.gemini4kt.Gemini
import io.github.ugaikit.gemini4kt.GenerateContentRequest
import io.github.ugaikit.gemini4kt.Modality
import io.github.ugaikit.gemini4kt.Part
import io.github.ugaikit.gemini4kt.generationConfig
import io.github.ugaikit.gemini4kt.getApiKey
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object AudioGeneration {
    suspend fun run(gemini: Gemini? = null): String? {
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

        val client =
            gemini ?: run {
                val apiKey = getApiKey()
                if (apiKey.isNotBlank()) {
                    Gemini(apiKey)
                } else {
                    println("GEMINI_API_KEY not found. Skipping API call.")
                    return null
                }
            }
        try {
            val response =
                client.generateContent(
                    model = "gemini-2.5-flash-preview-tts",
                    inputJson =
                        GenerateContentRequest(
                            contents = arrayOf(Content(role = "user", parts = arrayOf(Part(text = "Say cheerfully: Have a wonderful day!")))),
                            generationConfig = config1,
                        ),
                )

            val base64Audio =
                response.candidates
                    .get(0)
                    .content
                    .parts
                    ?.get(0)
                    ?.inlineData
                    ?.data
            return base64Audio
        } catch (e: Exception) {
            println("Error: ${e.message}")
        }
        return null
    }
}
