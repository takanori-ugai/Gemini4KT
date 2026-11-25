package io.github.ugaikit.gemini4kt.genie

import io.github.ugaikit.gemini4kt.GeminiAI
import io.github.ugaikit.gemini4kt.GeminiException
import io.github.ugaikit.gemini4kt.interaction.InteractionStatus
import io.github.ugaikit.gemini4kt.interaction.InteractionUsage
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GenieSessionTest {
    @Test
    fun createsWorldAndScopedTokenThenExchangesWebRtcSdp() =
        runTest {
            val requests = mutableListOf<String>()
            val client =
                HttpClient(
                    MockEngine { request ->
                        val path = request.url.encodedPath
                        requests += path
                        assertEquals(HttpMethod.Post, request.method)
                        val body = (request.body as TextContent).text
                        when (path) {
                            "/v1beta/interactions" -> {
                                assertEquals("server-key", request.headers["x-goog-api-key"])
                                assertEquals("""{"model":"genie-4"}""", body)
                                respond("""{"id":"world","status":"idle"}""")
                            }
                            "/v1beta/interactions/world/auth_tokens" -> {
                                assertEquals("server-key", request.headers["x-goog-api-key"])
                                assertEquals("{}", body)
                                respond("""{"auth_token":"auth_tokens/scoped"}""")
                            }
                            "/v1beta/interactions/world:webrtc" -> {
                                assertEquals("auth_tokens/scoped", request.headers["x-goog-api-key"])
                                assertNull(request.headers["Api-Revision"])
                                assertEquals("""{"offer_sdp":"offer"}""", body)
                                respond("""{"answer_sdp":"answer"}""")
                            }
                            else -> error("Unexpected request path.")
                        }
                    },
                )
            try {
                val api = GeminiAI(client = client, apiKey = "server-key")
                val world = api.createGenieInteraction()
                assertEquals(InteractionStatus.IDLE, world.status)
                val token = api.createInteractionAuthToken("interactions/${world.id}")
                val peer = TestPeer()
                val session = withContext(Dispatchers.Default) { api.connectGenieWebRtc(world.id, peer, token) }
                assertEquals("answer", peer.answer)
                session.close()
                assertEquals(true, peer.closed)
                assertEquals(3, requests.size)
            } finally {
                client.close()
            }
        }

    @Test
    fun sendsWorldControlsAndReadsMediaUntilCompletion() =
        runTest {
            val peer = TestPeer()
            val session =
                GenieSession(
                    "world",
                    GenieTransport(peer.events, peer::send, peer::close),
                    Json { ignoreUnknownKeys = true },
                )
            assertFailsWith<IllegalArgumentException> { session.setupWorld(text = " ") }
            session.setupWorld(referenceImage = GenieReferenceImage("image/jpeg", "AQID"))
            session.sendKey(GenieKey.W, GenieKeyAction.DOWN)
            session.sendKey(GenieKey.W, GenieKeyAction.UP)
            session.sendText("Turn on the lights.")
            session.complete()

            val events = peer.sent.map { Json.parseToJsonElement(it).jsonObject }
            assertEquals(
                """{"event_type":"world_setup","reference_image":{"mime_type":"image/jpeg","data":"AQID"}}""",
                events[0].toString(),
            )
            assertEquals(JsonPrimitive("key_w"), events[1]["input"])
            assertEquals(JsonPrimitive("key_down"), events[1]["action"])
            assertEquals(JsonPrimitive("key_up"), events[2]["action"])
            val text = """{"event_type":"input","content":[{"type":"text","text":"Turn on the lights."}]}"""
            assertEquals(text, events[3].toString())
            assertEquals(JsonPrimitive("interaction.complete"), events[4]["event_type"])

            val received = session.receive().toList()
            assertEquals(2, received.size)
            assertEquals(48_000, received[0].delta?.sampleRate)
            assertEquals(2, received[0].delta?.channels)
            assertEquals("interaction.completed", received[1].eventType)
            assertEquals("done", received[1].eventId)
            assertEquals(42, received[1].metadata?.totalUsage?.totalTokens)
            assertEquals(received, Json.decodeFromString<List<GenieEvent>>(Json.encodeToString(received)))
        }

    @Test
    fun blankWorldPromptIsOmittedWhenAnImageIsPresent() =
        runTest {
            val peer = TestPeer()
            val session = GenieSession("world", GenieTransport(peer.events, peer::send, peer::close), Json)
            session.setupWorld(text = " ", referenceImage = GenieReferenceImage("image/png", "AQID"))

            assertEquals(
                """{"event_type":"world_setup","reference_image":{"mime_type":"image/png","data":"AQID"}}""",
                peer.sent.single(),
            )
        }

    @Test
    fun decodesOptionalEventFieldsAndRoundTripsTypedEvents() {
        val audio =
            """
                {
                  "event_type":"step.delta",
                  "index":3,
                  "delta":{"type":"audio","data":"AQID","rate":48000},
                  "metadata":{"total_usage":{"total_tokens":42}}
                }
            """.trimIndent()
        val expected =
            GenieEvent(
                eventType = "step.delta",
                index = 3,
                delta = GenieMediaDelta(type = "audio", data = "AQID", rate = 48_000),
                metadata = GenieEventMetadata(InteractionUsage(totalTokens = 42)),
            )
        val decoded = Json.decodeFromString<GenieEvent>(audio)
        assertEquals(expected, decoded)
        assertEquals(3, decoded.index)
        assertEquals("audio", decoded.delta?.type)
        assertEquals(48_000, decoded.delta?.rate)
        assertEquals(Json.parseToJsonElement(audio), Json.parseToJsonElement(Json.encodeToString(expected)))

        val text =
            Json.decodeFromString<GenieEvent>(
                """{"event_type":"step.delta","delta":{"type":"text","text":"Ready"}}""",
            )
        assertEquals("Ready", text.delta?.text)
        val completed =
            Json.decodeFromString<GenieEvent>(
                """{"event_type":"interaction.completed","interaction":{"id":"world","status":"completed"}}""",
            )
        assertEquals(JsonPrimitive("world"), completed.interaction?.get("id"))
        val error = Json.decodeFromString<GenieEvent>("""{"event_type":"error","error":{"message":"Access denied"}}""")
        assertEquals(JsonPrimitive("Access denied"), error.error?.get("message"))
        assertEquals(GenieEventMetadata(), Json.decodeFromString<GenieEventMetadata>("{}"))
    }

    @Test
    fun rejectedWebRtcOfferClosesPeerAndPreservesServerError() =
        runTest {
            val peer = TestPeer(closeFailure = IllegalStateException("Peer close failed"))
            val engine =
                MockEngine { request ->
                    assertEquals("/v1beta/interactions/world:webrtc", request.url.encodedPath)
                    assertEquals("server-key", request.headers["x-goog-api-key"])
                    respond(
                        """{"error":{"code":403,"message":"Access denied","status":"PERMISSION_DENIED"}}""",
                        HttpStatusCode.Forbidden,
                    )
                }
            try {
                HttpClient(engine).use { client ->
                    val api = GeminiAI(client = client, apiKey = "server-key")
                    val error =
                        assertFailsWith<GeminiException> {
                            withContext(Dispatchers.Default) { api.connectGenieWebRtc("world", peer) }
                        }
                    assertTrue(error.message.orEmpty().contains("Access denied"))
                    assertNull(peer.answer)
                    assertTrue(peer.closed)
                }
            } finally {
                engine.close()
            }
        }

    @Test
    fun rejectsNestedInteractionIdBeforeConnecting() =
        runTest {
            val engine = MockEngine { error("Invalid IDs must be rejected before making a request.") }
            try {
                HttpClient(engine).use { client ->
                    val api = GeminiAI(client = client, apiKey = "server-key")
                    assertFailsWith<IllegalArgumentException> {
                        api.connectGenieWebSocket("interactions/world/nested")
                    }
                }
            } finally {
                engine.close()
            }
        }

    private class TestPeer(
        private val closeFailure: IllegalStateException? = null,
    ) : GenieWebRtcPeer {
        val sent = mutableListOf<String>()
        var answer: String? = null
        var closed = false

        override val events =
            flowOf(
                """
                {"event_type":"step.delta","delta":{"type":"audio","data":"AQID","sample_rate":48000,"channels":2}}
                """.trimIndent(),
                """
                {"event_type":"interaction.completed","event_id":"done","metadata":{"total_usage":{"total_tokens":42}}}
                """.trimIndent(),
                """{"event_type":"unused"}""",
            )

        override suspend fun createOffer() = "offer"

        override suspend fun acceptAnswer(sdp: String) {
            answer = sdp
        }

        override suspend fun send(message: String) {
            sent += message
        }

        override suspend fun close() {
            closed = true
            closeFailure?.let { throw it }
        }
    }
}
