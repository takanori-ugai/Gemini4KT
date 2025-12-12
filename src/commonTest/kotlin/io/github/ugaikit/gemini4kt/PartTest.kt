package io.github.ugaikit.gemini4kt

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlin.test.Test
import kotlin.test.assertEquals

class PartTest {
    private val json = Json { ignoreUnknownKeys = true }
    private val image = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII="

    @Test
    fun `serialization with text`() {
        val part = Part(text = "Hello")
        val expectedJson = """{"text":"Hello"}"""
        val actualJson = json.encodeToString(part)
        assertEquals(json.parseToJsonElement(expectedJson), json.parseToJsonElement(actualJson))
    }

    @Test
    fun `serialization with inlineData`() {
        val part =
            Part(
                inlineData =
                    InlineData(
                        mimeType = "image/png",
                        data = image,
                    ),
            )
        val expectedJson =
            """
            {
              "inlineData": {
                "mimeType": "image/png",
                "data": "$image"
              }
            }
            """.trimIndent()
        val actualJson = json.encodeToString(part)
        assertEquals(json.parseToJsonElement(expectedJson), json.parseToJsonElement(actualJson))
    }

    @Test
    fun `serialization with functionCall`() {
        val part =
            Part(
                functionCall =
                    FunctionCall(
                        name = "get_weather",
                        args = mapOf("city" to Json.parseToJsonElement("\"New York\"")),
                    ),
            )
        val expectedJson =
            """
            {
              "functionCall": {
                "name": "get_weather",
                "args": {
                  "city": "New York"
                }
              }
            }
            """.trimIndent()
        val actualJson = json.encodeToString(part)
        assertEquals(json.parseToJsonElement(expectedJson), json.parseToJsonElement(actualJson))
    }

    @Test
    fun `serialization with functionResponse`() {
        val part =
            Part(
                functionResponse =
                    FunctionResponse(
                        name = "get_weather",
                        response = JsonObject(mapOf("weather" to Json.parseToJsonElement("\"sunny\""))),
                    ),
            )
        val expectedJson =
            """
            {
              "functionResponse": {
                "name": "get_weather",
                "response": {
                  "weather": "sunny"
                }
              }
            }
            """.trimIndent()
        val actualJson = json.encodeToString(part)
        assertEquals(json.parseToJsonElement(expectedJson), json.parseToJsonElement(actualJson))
    }

    @Test
    fun `serialization with fileData`() {
        val part =
            Part(
                fileData =
                    FileData(
                        mimeType = "image/png",
                        fileUri = "gs://bucket/image.png",
                    ),
            )
        val expectedJson =
            """
            {
              "fileData": {
                "mimeType": "image/png",
                "fileUri": "gs://bucket/image.png"
              }
            }
            """.trimIndent()
        val actualJson = json.encodeToString(part)
        assertEquals(json.parseToJsonElement(expectedJson), json.parseToJsonElement(actualJson))
    }
}
