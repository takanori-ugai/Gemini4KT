package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object AudioGeneration {
    suspend fun run(): String? {
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

        val apiKey = getApiKey()
        if (apiKey.isNotBlank()) {
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
                return base64Audio
            } catch (e: Exception) {
                println("Error: ${e.message}")
            }
        } else {
            println("GEMINI_API_KEY not found. Skipping API call.")
        }
        return null
    }
}
