package io.github.ugaikit.gemini4kt

import io.github.ugaikit.gemini4kt.interaction.CreateInteractionRequest
import io.github.ugaikit.gemini4kt.interaction.Interaction
import io.github.ugaikit.gemini4kt.interaction.InteractionGenerationConfig
import io.github.ugaikit.gemini4kt.interaction.InteractionStatus
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import io.github.ugaikit.gemini4kt.interaction.ThinkingLevel as InteractionThinkingLevel

class HydrogenSupportTest {
    @Test
    fun interactionsSerializeAndDecodeXhigh() {
        val config = InteractionGenerationConfig(thinkingLevel = InteractionThinkingLevel.XHIGH)
        val encoded = Json.encodeToString(config)
        assertEquals(JsonPrimitive("xhigh"), Json.parseToJsonElement(encoded).jsonObject["thinking_level"])
        assertEquals(config, Json.decodeFromString<InteractionGenerationConfig>(encoded))
        val response =
            Json.decodeFromString<Interaction>(
                """{"id":"hydrogen_1","status":"completed","generation_config":{"thinking_level":"xhigh"}}""",
            )
        assertEquals(InteractionThinkingLevel.XHIGH, response.generationConfig?.thinkingLevel)
    }

    @Test
    fun generateContentOmitsTheLegacyBudgetWhenUsingXhighEvenWithDefaultsEnabled() {
        val config = ThinkingConfig(thinkingLevel = ThinkingLevel.XHIGH)
        for (encodeDefaults in listOf(false, true)) {
            val json = Json { this.encodeDefaults = encodeDefaults }
            val fields = json.parseToJsonElement(json.encodeToString(config)).jsonObject
            assertEquals(JsonPrimitive("XHIGH"), fields["thinking_level"])
            assertFalse(fields.containsKey("thinking_budget"))
        }
        val oldBudget = Json.parseToJsonElement(Json.encodeToString(ThinkingConfig(thinkingBudget = 256))).jsonObject
        assertEquals(JsonPrimitive(256), oldBudget["thinking_budget"])
    }

    private fun createClient(onRequest: () -> Unit): HttpClient {
        val json = Json { ignoreUnknownKeys = true }
        return HttpClient(MockEngine) {
            engine {
                addHandler { request ->
                    onRequest()
                    val body = Json.parseToJsonElement((request.body as TextContent).text).jsonObject
                    if (request.url.encodedPath.endsWith("interactions")) {
                        assertEquals(JsonPrimitive("gemini-4-hydrogen"), body["model"])
                        val config = body.getValue("generation_config").jsonObject
                        assertEquals(JsonPrimitive("xhigh"), config["thinking_level"])
                        respond(
                            """{"id":"hydrogen_1","status":"completed"}""",
                            headers = headersOf(HttpHeaders.ContentType, "application/json"),
                        )
                    } else {
                        val config =
                            body
                                .getValue("generationConfig")
                                .jsonObject
                                .getValue("thinkingConfig")
                                .jsonObject
                        assertEquals(JsonPrimitive("XHIGH"), config["thinking_level"])
                        assertFalse(config.containsKey("thinking_budget"))
                        if (request.url.encodedPath.endsWith(":streamGenerateContent")) {
                            respond(
                                "data: {\"candidates\":[]}\n\n",
                                headers = headersOf(HttpHeaders.ContentType, "text/event-stream"),
                            )
                        } else {
                            assertEquals(
                                "/v1beta/models/gemini-4-hydrogen:generateContent",
                                request.url.encodedPath,
                            )
                            respond(
                                """{"candidates":[]}""",
                                headers = headersOf(HttpHeaders.ContentType, "application/json"),
                            )
                        }
                    }
                }
            }
            install(ContentNegotiation) { json(json) }
        }
    }

    @Test
    fun hydrogenRequestsWorkAcrossBothApisAndStreaming() =
        runTest {
            var calls = 0
            val client = createClient { calls++ }
            try {
                val request =
                    generateContentRequest {
                        content { part { text { "Review this migration." } } }
                        generationConfig { thinkingConfig = ThinkingConfig(thinkingLevel = ThinkingLevel.XHIGH) }
                    }
                val gemini = Gemini("test-key", client = client)
                gemini.generateContent(request, model = "gemini-4-hydrogen")
                assertEquals(1, gemini.streamGenerateContent(request, model = "gemini-4-hydrogen").toList().size)
                GeminiAI(client = client, apiKey = "test-key").createInteraction(
                    CreateInteractionRequest(
                        model = "gemini-4-hydrogen",
                        input = "Review this migration.",
                        generationConfig = InteractionGenerationConfig(thinkingLevel = InteractionThinkingLevel.XHIGH),
                    ),
                )
                assertEquals(3, calls)
            } finally {
                client.close()
            }
        }

    @Test
    fun interactionsExposeContinuationAndJoinBackgroundOutput() {
        val response =
            Json.decodeFromString<Interaction>(
                """
                {
                  "id": "hop1",
                  "status": "requires_continuation",
                  "continuation_token": "next",
                  "steps": [
                    {"type": "model_output", "content": [{"type": "text", "text": "First "}]},
                    {"type": "thought", "content": [{"type": "text", "text": "ignored"}]},
                    {"type": "model_output", "content": [{"type": "text", "text": "second"}]}
                  ]
                }
                """.trimIndent(),
            )
        assertEquals(InteractionStatus.REQUIRES_CONTINUATION, response.status)
        assertEquals("second", response.outputText)
        assertEquals("First second", response.allOutputText)
        val request =
            CreateInteractionRequest(
                model = "gemini-4-hydrogen",
                previousInteractionId = response.id,
                continuationToken = response.continuationToken,
            )
        val fields = Json.parseToJsonElement(Json.encodeToString(request)).jsonObject
        assertEquals(JsonPrimitive("hop1"), fields["previous_interaction_id"])
        assertEquals(JsonPrimitive("next"), fields["continuation_token"])
        assertFalse(fields.containsKey("input"))
        assertEquals(
            InteractionStatus.INCOMPLETE,
            Json.decodeFromString<Interaction>("""{"id":"hop2","status":"incomplete"}""").status,
        )
    }

    @Test
    fun generateContentExposesAndResendsCandidateContinuationToken() {
        val response =
            Json.decodeFromString<GenerateContentResponse>(
                """
                {
                  "candidates": [{
                    "content": {"parts": [{"text": ""}]},
                    "finishReason": "CONTINUATION",
                    "continuationToken": "next"
                  }]
                }
                """.trimIndent(),
            )
        val candidate = response.candidates.single()
        assertEquals("CONTINUATION", candidate.finishReason)
        val request =
            generateContentRequest {
                content { part { text { "Write a report." } } }
                generationConfig { maxOutputTokens = 131_072 }
                continuationToken = candidate.continuationToken
            }
        val fields = Json.parseToJsonElement(Json.encodeToString(request)).jsonObject
        assertEquals(JsonPrimitive("next"), fields["continuationToken"])
        assertEquals(JsonPrimitive(131_072), fields.getValue("generationConfig").jsonObject["maxOutputTokens"])
    }

    @Test
    fun resumesInteractionStreamAfterLastEvent() =
        runTest {
            val client =
                HttpClient(
                    MockEngine { request ->
                        assertEquals(HttpMethod.Get, request.method)
                        assertEquals("/v1beta/interactions/hop1", request.url.encodedPath)
                        assertEquals("true", request.url.parameters["stream"])
                        assertEquals("event/+1", request.url.parameters["last_event_id"])
                        assertEquals("test-key", request.headers["x-goog-api-key"])
                        assertEquals("2026-05-20", request.headers["Api-Revision"])
                        respond(
                            "data: {\"event_type\":\"interaction.completed\"}\n\ndata: [DONE]\n\n",
                            headers = headersOf(HttpHeaders.ContentType, "text/event-stream"),
                        )
                    },
                )
            try {
                val events =
                    GeminiAI(client = client, apiKey = "test-key")
                        .resumeInteractionStream("interactions/hop1", "event/+1")
                        .toList()
                assertEquals(JsonPrimitive("interaction.completed"), events.single().jsonObject["event_type"])
            } finally {
                client.close()
            }
        }

    @Test
    fun managedAgentRequestsInheritTheServiceModelDefault() {
        val request = CreateInteractionRequest(agent = "antigravity-preview-09-2026", input = "Review this migration.")
        val fields = Json.parseToJsonElement(Json.encodeToString(request)).jsonObject
        assertFalse(fields.containsKey("model"))
        assertFalse(fields.containsKey("agent_config"))
    }
}
