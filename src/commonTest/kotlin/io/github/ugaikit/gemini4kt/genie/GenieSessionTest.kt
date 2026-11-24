package io.github.ugaikit.gemini4kt.genie

import io.github.ugaikit.gemini4kt.GeminiAI
import io.github.ugaikit.gemini4kt.interaction.InteractionStatus
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpMethod
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
import kotlin.test.assertNull

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
        }

    private class TestPeer : GenieWebRtcPeer {
        val sent = mutableListOf<String>()
        var answer: String? = null
        var closed = false

        override val events =
            flowOf(
                """
                {"event_type":"step.delta","delta":{"type":"audio","data":"AQID","sample_rate":48000,"channels":2}}
                """.trimIndent(),
                """{"event_type":"interaction.completed"}""",
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
        }
    }
}
