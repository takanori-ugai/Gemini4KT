package io.github.ugaikit.gemini4kt

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Represents the speech config test.
 */
class SpeechConfigTest {
    /**
     * Holds the json.
     */
    private val json =
        Json {
            prettyPrint = true
            encodeDefaults = false
            explicitNulls = false
        }

    /**
     * Tests test single voice config serialization.
     */
    @Test
    fun testSingleVoiceConfigSerialization() {
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
                "speech_config": {
                    "voice_config": {
                        "prebuilt_voice_config": {
                            "voice_name": "Kore"
                        }
                    }
                }
            }
            """.trimIndent()

        val actualJsonString = json.encodeToString(config)
        val parsedActual = json.parseToJsonElement(actualJsonString)
        val parsedExpected = json.parseToJsonElement(expectedJson)

        assertEquals(parsedExpected, parsedActual)
    }

    /**
     * Tests test multi speaker config serialization.
     */
    @Test
    fun testMultiSpeakerConfigSerialization() {
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
                "speech_config": {
                    "multi_speaker_voice_config": {
                        "speaker_voice_configs": [
                            {
                                "speaker": "Joe",
                                "voice_config": {
                                    "prebuilt_voice_config": {
                                        "voice_name": "Kore"
                                    }
                                }
                            },
                            {
                                "speaker": "Jane",
                                "voice_config": {
                                    "prebuilt_voice_config": {
                                        "voice_name": "Puck"
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

    @Test
    fun testSpeechConfigRejectsMultiplePrimaryModes() {
        assertFailsWith<IllegalArgumentException> {
            speechConfig {
                voiceConfig {
                    prebuiltVoiceConfig {
                        voiceName { "Kore" }
                    }
                }
                multiSpeakerVoiceConfig {
                    speakerVoiceConfig {
                        speaker { "Joe" }
                        voiceConfig {
                            prebuiltVoiceConfig {
                                voiceName { "Puck" }
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    fun testSpeechConfigRejectsEmptyBuilder() {
        assertFailsWith<IllegalArgumentException> {
            speechConfig {}
        }
    }

    @Test
    fun testVoiceConfigRejectsEmptyBuilder() {
        assertFailsWith<IllegalArgumentException> {
            VoiceConfigBuilder().build()
        }
    }

    @Test
    fun testPrebuiltVoiceConfigRejectsEmptyBuilder() {
        assertFailsWith<IllegalArgumentException> {
            PrebuiltVoiceConfigBuilder().build()
        }
    }

    @Test
    fun testPrebuiltVoiceConfigRejectsBlankVoiceName() {
        val exception =
            assertFailsWith<IllegalArgumentException> {
                PrebuiltVoiceConfigBuilder()
                    .apply {
                        voiceName { "" }
                    }.build()
            }
        assertEquals("PrebuiltVoiceConfigBuilder requires voiceName.", exception.message)
    }

    @Test
    fun testSpeakerVoiceConfigRejectsBlankSpeaker() {
        val exception =
            assertFailsWith<IllegalArgumentException> {
                SpeakerVoiceConfigBuilder()
                    .apply {
                        speaker { "" }
                        voiceConfig {
                            prebuiltVoiceConfig {
                                voiceName { "Kore" }
                            }
                        }
                    }.build()
            }
        assertEquals("SpeakerVoiceConfigBuilder requires speaker.", exception.message)
    }

    @Test
    fun testMultiSpeakerVoiceConfigRejectsEmptyBuilder() {
        assertFailsWith<IllegalArgumentException> {
            MultiSpeakerVoiceConfigBuilder().build()
        }
    }

    @Test
    fun testSpeakerVoiceConfigRejectsEmptyBuilder() {
        assertFailsWith<IllegalArgumentException> {
            SpeakerVoiceConfigBuilder().build()
        }
    }
}
