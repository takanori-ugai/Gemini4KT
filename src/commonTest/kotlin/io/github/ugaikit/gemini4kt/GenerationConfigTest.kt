package io.github.ugaikit.gemini4kt

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
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
}
