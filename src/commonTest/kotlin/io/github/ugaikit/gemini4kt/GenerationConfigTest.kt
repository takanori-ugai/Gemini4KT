package io.github.ugaikit.gemini4kt

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test

class GenerationConfigTest {
    private val json = Json { prettyPrint = true }

    @Test
    fun `test GenerationConfig serialization with responseModalities`() {
        val config =
            generationConfig {
                responseModality(Modality.TEXT)
                responseModality(Modality.IMAGE)
            }

        val jsonString = json.encodeToString(config)

        // We verify that the field is present and has correct values
        assert(jsonString.contains("\"response_modalities\":"))
        assert(jsonString.contains("\"TEXT\""))
        assert(jsonString.contains("\"IMAGE\""))
    }

    @Test
    fun `test GenerationConfig serialization without responseModalities`() {
        val config =
            generationConfig {
                temperature = 0.5
            }

        val jsonString = json.encodeToString(config)

        // We verify that the field is NOT present (as it is null)
        assert(!jsonString.contains("\"response_modalities\":"))
    }

    @Test
    fun `test GenerationConfig serialization with imageConfig`() {
        val config =
            generationConfig {
                imageConfig {
                    aspectRatio { "16:9" }
                    imageSize { "2K" }
                }
            }

        val jsonString = json.encodeToString(config)

        assert(jsonString.contains("\"imageConfig\":"))
        assert(jsonString.contains("\"aspectRatio\": \"16:9\""))
        assert(jsonString.contains("\"imageSize\": \"2K\""))
    }
}
