package io.github.ugaikit.gemini4kt

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class SpeechConfigTest {
    private val json = Json { prettyPrint = true }

    @Test
    fun `test single voice config serialization`() {
        val config =
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

        val expectedJson =
            """
            {
                "response_modalities": [
                    "AUDIO"
                ],
                "speechConfig": {
                    "voiceConfig": {
                        "prebuiltVoiceConfig": {
                            "voiceName": "Kore"
                        }
                    }
                }
            }
            """.trimIndent()

        // We only care about the fields we set. The actual JSON might contain nulls if we didn't use `encodeDefaults = false` which is default.
        // Wait, standard Json configuration in this project likely omits nulls.
        // Let's check how GenerationConfig is serialized usually.
        // Based on GenerationConfig.kt, it's a data class with nullable fields default to null.
        // Kotlinx serialization by default doesn't encode nulls if they are optional?
        // Actually, default is `encodeDefaults = true` but nulls are only encoded if they are explicit null?
        // No, standard kotlinx serialization omits nulls if `explicitNulls = false` (default is true).
        // BUT, if the property is nullable and has default value null, and we don't set it...

        // Let's verify by running. But I'll write the test assuming it might generate minimal JSON.
        // Actually, to be safe, I should parse both to JsonElement and compare.

        val actualJsonString = json.encodeToString(config)
        // Simple string comparison might fail due to formatting/ordering.
        // I will use `assertEquals` but I need to be careful about whitespace if I use formatted string.
        // Better to check specific structure if possible, or just rely on the fact that I'm testing the builder -> object structure.

        // Let's just print it in the test for debugging if it fails, and do a simple check.

        // The most important part is that the structure matches what the API expects.

        val parsedActual = json.parseToJsonElement(actualJsonString)
        val parsedExpected = json.parseToJsonElement(expectedJson)

        assertEquals(parsedExpected, parsedActual)
    }

    @Test
    fun `test multi speaker config serialization`() {
        val config =
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

        val expectedJson =
            """
            {
                "response_modalities": [
                    "AUDIO"
                ],
                "speechConfig": {
                    "multiSpeakerVoiceConfig": {
                        "speakerVoiceConfigs": [
                            {
                                "speaker": "Joe",
                                "voiceConfig": {
                                    "prebuiltVoiceConfig": {
                                        "voiceName": "Kore"
                                    }
                                }
                            },
                            {
                                "speaker": "Jane",
                                "voiceConfig": {
                                    "prebuiltVoiceConfig": {
                                        "voiceName": "Puck"
                                    }
                                }
                            }
                        ]
                    }
                }
            }
            """.trimIndent()

        val parsedActual = json.parseToJsonElement(json.encodeToString(config))
        val parsedExpected = json.parseToJsonElement(expectedJson)

        assertEquals(parsedExpected, parsedActual)
    }
}
