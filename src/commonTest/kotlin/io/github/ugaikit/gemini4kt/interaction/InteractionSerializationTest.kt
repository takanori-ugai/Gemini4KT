package io.github.ugaikit.gemini4kt.interaction

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
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

    @Test
    fun testInteractionEqualsAndHashCode() {
        val steps1 = arrayOf<JsonElement>(JsonPrimitive("step1"))
        val steps2 = arrayOf<JsonElement>(JsonPrimitive("step1"))
        val stepsDifferent = arrayOf<JsonElement>(JsonPrimitive("stepDifferent"))

        val interaction1 =
            Interaction(
                id = "inter_1",
                status = InteractionStatus.COMPLETED,
                environmentId = "env_1",
                outputText = "output",
                steps = steps1,
            )
        val interaction2 =
            Interaction(
                id = "inter_1",
                status = InteractionStatus.COMPLETED,
                environmentId = "env_1",
                outputText = "output",
                steps = steps2,
            )
        val interactionDiffId =
            Interaction(
                id = "inter_2",
                status = InteractionStatus.COMPLETED,
                environmentId = "env_1",
                outputText = "output",
                steps = steps1,
            )
        val interactionDiffStatus =
            Interaction(
                id = "inter_1",
                status = InteractionStatus.IN_PROGRESS,
                environmentId = "env_1",
                outputText = "output",
                steps = steps1,
            )
        val interactionDiffEnvId =
            Interaction(
                id = "inter_1",
                status = InteractionStatus.COMPLETED,
                environmentId = "env_2",
                outputText = "output",
                steps = steps1,
            )
        val interactionDiffOutput =
            Interaction(
                id = "inter_1",
                status = InteractionStatus.COMPLETED,
                environmentId = "env_1",
                outputText = "output_diff",
                steps = steps1,
            )
        val interactionDiffSteps =
            Interaction(
                id = "inter_1",
                status = InteractionStatus.COMPLETED,
                environmentId = "env_1",
                outputText = "output",
                steps = stepsDifferent,
            )
        val interactionNullSteps1 =
            Interaction(
                id = "inter_1",
                status = InteractionStatus.COMPLETED,
                environmentId = "env_1",
                outputText = "output",
                steps = null,
            )
        val interactionNullSteps2 =
            Interaction(
                id = "inter_1",
                status = InteractionStatus.COMPLETED,
                environmentId = "env_1",
                outputText = "output",
                steps = null,
            )

        // Test Interaction equals and hashCode
        assertEquals(interaction1, interaction1)
        assertEquals(interaction1, interaction2)
        assertEquals(interaction1.hashCode(), interaction2.hashCode())
        assertEquals(interactionNullSteps1, interactionNullSteps2)
        assertEquals(interactionNullSteps1.hashCode(), interactionNullSteps2.hashCode())

        // Check inequality cases
        kotlin.test.assertNotEquals(interaction1, null as Interaction?)
        kotlin.test.assertNotEquals(interaction1, "not an interaction" as Any?)
        kotlin.test.assertNotEquals(interaction1, interactionDiffId)
        kotlin.test.assertNotEquals(interaction1, interactionDiffStatus)
        kotlin.test.assertNotEquals(interaction1, interactionDiffEnvId)
        kotlin.test.assertNotEquals(interaction1, interactionDiffOutput)
        kotlin.test.assertNotEquals(interaction1, interactionDiffSteps)
        kotlin.test.assertNotEquals(interaction1, interactionNullSteps1)
        kotlin.test.assertNotEquals(interactionNullSteps1, interaction1)

        val request1 =
            CreateInteractionRequest(
                model = "gemini-2.5-flash",
                input = JsonPrimitive("Hello"),
                environment = JsonPrimitive("remote"),
            )
        val request2 =
            CreateInteractionRequest(
                model = "gemini-2.5-flash",
                input = JsonPrimitive("Hello"),
                environment = JsonPrimitive("remote"),
            )
        val requestDiffModel =
            CreateInteractionRequest(
                model = "gemini-1.5-flash",
                input = JsonPrimitive("Hello"),
                environment = JsonPrimitive("remote"),
            )
        val requestDiffInput =
            CreateInteractionRequest(
                model = "gemini-2.5-flash",
                input = JsonPrimitive("World"),
                environment = JsonPrimitive("remote"),
            )
        val requestDiffEnv =
            CreateInteractionRequest(
                model = "gemini-2.5-flash",
                input = JsonPrimitive("Hello"),
                environment = JsonPrimitive("local"),
            )

        // Test CreateInteractionRequest equals and hashCode
        assertEquals(request1, request1)
        assertEquals(request1, request2)
        assertEquals(request1.hashCode(), request2.hashCode())

        // Check inequality cases
        kotlin.test.assertNotEquals(request1, null as CreateInteractionRequest?)
        kotlin.test.assertNotEquals(request1, "not a request" as Any?)
        kotlin.test.assertNotEquals(request1, requestDiffModel)
        kotlin.test.assertNotEquals(request1, requestDiffInput)
        kotlin.test.assertNotEquals(request1, requestDiffEnv)
    }
}
