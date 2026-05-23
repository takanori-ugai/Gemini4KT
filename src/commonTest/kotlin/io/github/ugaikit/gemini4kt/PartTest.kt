package io.github.ugaikit.gemini4kt

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Represents the part test.
 */
class PartTest {
    /**
     * Holds the json.
     */
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Holds the image.
     */
    private val image = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII="

    /**
     * Handles serialization with text.
     */
    @Test
    fun serializationWithText() {
        val part = Part(text = "Hello")
        val expectedJson = """{"text":"Hello"}"""
        val actualJson = json.encodeToString(part)
        assertEquals(json.parseToJsonElement(expectedJson), json.parseToJsonElement(actualJson))
    }

    /**
     * Handles serialization with inline data.
     */
    @Test
    fun serializationWithInlineData() {
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

    /**
     * Handles serialization with function call.
     */
    @Test
    fun serializationWithFunctionCall() {
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

    /**
     * Handles serialization with function response.
     */
    @Test
    fun serializationWithFunctionResponse() {
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

    /**
     * Handles serialization with file data.
     */
    @Test
    fun serializationWithFileData() {
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

    /**
     * Handles serialization with thought signature.
     */
    @Test
    fun serializationWithThoughtSignature() {
        val part = Part(thoughtSignature = "opaque-token-123")
        val expectedJson = """{"thoughtSignature":"opaque-token-123"}"""
        val actualJson = json.encodeToString(part)
        assertEquals(json.parseToJsonElement(expectedJson), json.parseToJsonElement(actualJson))
    }

    /**
     * Handles deserialization with snake_case thought_signature.
     */
    @Test
    fun deserializationWithThoughtSignatureSnakeCase() {
        val jsonStr = """{"thought_signature":"opaque-token-123"}"""
        val part = json.decodeFromString<Part>(jsonStr)
        assertEquals("opaque-token-123", part.thoughtSignature)
    }

    /**
     * Handles serialization with thought.
     */
    @Test
    fun serializationWithThought() {
        val part = Part(thought = true)
        val expectedJson = """{"thought":true}"""
        val actualJson = json.encodeToString(part)
        assertEquals(json.parseToJsonElement(expectedJson), json.parseToJsonElement(actualJson))
    }

    /**
     * Handles deserialization with thought.
     */
    @Test
    fun deserializationWithThought() {
        val jsonStr = """{"thought":true}"""
        val part = json.decodeFromString<Part>(jsonStr)
        assertEquals(true, part.thought)
    }

    /**
     * Handles serialization with thought from builder path.
     */
    @Test
    fun serializationWithThoughtFromBuilder() {
        val part = part { thought { true } }
        val expectedJson = """{"thought":true}"""
        val actualJson = json.encodeToString(part)
        assertEquals(json.parseToJsonElement(expectedJson), json.parseToJsonElement(actualJson))
    }

    /**
     * Handles serialization with tool call/response and video metadata.
     */
    @Test
    fun serializationWithToolCallResponseAndVideoMetadata() {
        val part =
            part {
                text { "assistant output" }
                partMetadata {
                    buildMap {
                        put("origin", Json.parseToJsonElement("\"unit-test\""))
                    }
                }
                mediaResolution { MediaResolution(level = MediaResolutionLevel.MEDIA_RESOLUTION_MEDIUM) }
                toolCall {
                    ToolCall(
                        id = "call-1",
                        toolType = ToolType.GOOGLE_SEARCH_WEB,
                        args = mapOf("query" to Json.parseToJsonElement("\"kotlin\"")),
                    )
                }
                toolResponse {
                    ToolResponse(
                        id = "call-1",
                        toolType = ToolType.GOOGLE_SEARCH_WEB,
                        response = mapOf("answer" to Json.parseToJsonElement("\"ok\"")),
                    )
                }
                videoMetadata {
                    VideoMetadata(
                        startOffset = "0s",
                        endOffset = "1.5s",
                        fps = 24.0,
                    )
                }
            }

        val encoded = json.encodeToString(part)
        val decoded = json.decodeFromString<Part>(encoded)

        assertEquals(ToolType.GOOGLE_SEARCH_WEB, decoded.toolCall?.toolType)
        assertEquals(
            "ok",
            decoded.toolResponse
                ?.response
                ?.get("answer")
                ?.toString()
                ?.trim('"'),
        )
        assertEquals(MediaResolutionLevel.MEDIA_RESOLUTION_MEDIUM, decoded.mediaResolution?.level)
        assertEquals(24.0, decoded.videoMetadata?.fps)
    }
}
