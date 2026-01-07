package io.github.ugaikit.gemini4kt.interaction

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals

class InteractionSerializationTest {
    private val json =
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = false
        }

    @Test
    fun interactionContentSerializesCorrectly() {
        val content =
            InteractionContent(
                type = "image",
                data = "base64",
                mimeType = "image/png",
            )
        val encoded = json.encodeToString(content)
        val expected =
            """
            {
              "type": "image",
              "data": "base64",
              "mime_type": "image/png"
            }
            """.trimIndent()
        assertEquals(json.parseToJsonElement(expected), json.parseToJsonElement(encoded))

        val decoded = json.decodeFromString<InteractionContent>(encoded)
        assertEquals(content, decoded)
    }

    @Test
    fun interactionToolSerializesAllowedTools() {
        val tool =
            InteractionTool(
                type = "function",
                name = "get_weather",
                description = "Weather lookup",
                allowedTools =
                    arrayOf(
                        InteractionAllowedTools(
                            mode = "auto",
                            tools = arrayOf("web_search", "code_execution"),
                        ),
                    ),
            )

        val encoded = json.encodeToString(tool)
        val expected =
            """
            {
              "type": "function",
              "name": "get_weather",
              "description": "Weather lookup",
              "allowed_tools": [
                {
                  "mode": "auto",
                  "tools": ["web_search", "code_execution"]
                }
              ]
            }
            """.trimIndent()
        assertEquals(json.parseToJsonElement(expected), json.parseToJsonElement(encoded))

        val decoded = json.decodeFromString<InteractionTool>(encoded)
        assertEquals(tool, decoded)
    }

    @Test
    fun interactionEnumSerializesToSnakeCase() {
        val statusJson = json.encodeToString(InteractionStatus.IN_PROGRESS)
        assertEquals("\"in_progress\"", statusJson)

        val modalityJson = json.encodeToString(InteractionResponseModality.TEXT)
        assertEquals("\"text\"", modalityJson)

        val decodedStatus = json.decodeFromString<InteractionStatus>("\"requires_action\"")
        assertEquals(InteractionStatus.REQUIRES_ACTION, decodedStatus)
    }

    @Test
    fun interactionRoundTripsWithOutputsAndTools() {
        val content = InteractionContent(type = "text", text = "Hello")
        val tool =
            InteractionTool(
                type = "function",
                name = "say_hi",
                parameters =
                    buildJsonObject {
                        put("type", "object")
                    },
            )
        val interaction =
            Interaction(
                id = "inter_1",
                status = InteractionStatus.COMPLETED,
                outputs = arrayOf(content),
                tools = arrayOf(tool),
                responseModalities = arrayOf(InteractionResponseModality.TEXT),
                responseFormat = JsonObject(mapOf("mode" to JsonPrimitive("json"))),
            )

        val encoded = json.encodeToString(interaction)
        val decoded = json.decodeFromString<Interaction>(encoded)
        assertEquals(interaction, decoded)
    }
}
