package io.github.ugaikit.gemini4kt

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Represents the generation config test.
 */
class GenerationConfigTest {
    /**
     * Holds the json.
     */
    private val json = Json { prettyPrint = true }

    /**
     * Tests test generation config serialization with response modalities.
     */
    @Test
    fun testGenerationConfigSerializationWithResponseModalities() {
        val config =
            generationConfig {
                responseModality(Modality.TEXT)
                responseModality(Modality.IMAGE)
            }

        val jsonString = json.encodeToString(config)

        // We verify that the field is present and has correct values
        assertTrue(jsonString.contains("\"response_modalities\":"))
        assertTrue(jsonString.contains("\"TEXT\""))
        assertTrue(jsonString.contains("\"IMAGE\""))
    }

    /**
     * Tests test generation config serialization without response modalities.
     */
    @Test
    fun testGenerationConfigSerializationWithoutResponseModalities() {
        val config =
            generationConfig {
                temperature = 0.5
            }

        val jsonString = json.encodeToString(config)

        // We verify that the field is NOT present (as it is null)
        assertTrue(!jsonString.contains("\"response_modalities\":"))
    }

    /**
     * Tests test generation config serialization with image config.
     */
    @Test
    fun testGenerationConfigSerializationWithImageConfig() {
        val config =
            generationConfig {
                imageConfig {
                    aspectRatio { "16:9" }
                    imageSize { "2K" }
                }
            }

        val jsonString = json.encodeToString(config)

        assertTrue(jsonString.contains("\"imageConfig\":"))
        assertTrue(jsonString.contains("\"aspectRatio\": \"16:9\""))
        assertTrue(jsonString.contains("\"imageSize\": \"2K\""))
    }

    @Test
    fun testGenerationConfigBuilderCoversExtendedFields() {
        val config =
            generationConfig {
                stopSequence("END")
                temperature = 0.6
                maxOutputTokens = 256
                topP = 0.9
                topK = 20
                responseMimeType = "application/json"
                responseModality(Modality.TEXT)
                thinkingConfig =
                    ThinkingConfig(
                        thinkingBudget = 64,
                        thinkingLevel = ThinkingLevel.MEDIUM,
                        includeThoughts = true,
                    )
                responseJsonSchema = Json.parseToJsonElement("""{"type":"object"}""")
                seed = 42
                presencePenalty = 0.2
                frequencyPenalty = 0.1
                responseLogprobs = true
                logprobs = 3
                enableEnhancedCivicAnswers = true
                mediaResolution = MediaResolutionLevel.MEDIA_RESOLUTION_HIGH
            }

        assertEquals("END", config.stopSequences?.first())
        assertEquals(Modality.TEXT, config.responseModalities?.first())
        assertEquals(42, config.seed)
        assertEquals(MediaResolutionLevel.MEDIA_RESOLUTION_HIGH, config.mediaResolution)

        val encoded = json.encodeToString(config)
        assertTrue(encoded.contains("\"response_json_schema\""))
        assertTrue(encoded.contains("\"MEDIA_RESOLUTION_HIGH\""))
    }

    @Test
    fun testGenerationConfigRejectsMultipleResponseSchemas() {
        assertFailsWith<IllegalArgumentException> {
            generationConfig {
                responseSchema {
                    type = "object"
                }
                responseJsonSchema = Json.parseToJsonElement("""{"type":"object"}""")
            }
        }
    }
}
