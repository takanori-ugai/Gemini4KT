package io.github.ugaikit.gemini4kt

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Represents the generate content request test.
 */
class GenerateContentRequestTest {
    /**
     * Holds the json.
     */
    private val json = Json { prettyPrint = true }

    /**
     * Handles serialization with only contents.
     */
    @Test
    fun serializationWithOnlyContents() {
        val request =
            GenerateContentRequest(
                contents =
                    arrayOf(
                        Content(
                            role = "user",
                            parts = arrayOf(Part(text = "Hello")),
                        ),
                    ),
            )
        val expectedJson = """{"contents":[{"parts":[{"text":"Hello"}],"role":"user"}],"tools":[],"safetySettings":[]}"""
        val actualJson = json.encodeToString(request)
        assertEquals(json.parseToJsonElement(expectedJson), json.parseToJsonElement(actualJson))
    }
}
