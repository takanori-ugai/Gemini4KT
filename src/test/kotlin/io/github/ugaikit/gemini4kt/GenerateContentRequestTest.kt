package io.github.ugaikit.gemini4kt

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GenerateContentRequestTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `serialization with only contents`() {
        val request =
            GenerateContentRequest(
                contents =
                    listOf(
                        Content(
                            role = "user",
                            parts = listOf(Part(text = "Hello")),
                        ),
                    ),
            )
        val expectedJson = """{"contents":[{"parts":[{"text":"Hello"}],"role":"user"}]}"""
        val actualJson = json.encodeToString(request)
        assertEquals(json.parseToJsonElement(expectedJson), json.parseToJsonElement(actualJson))
    }

}
