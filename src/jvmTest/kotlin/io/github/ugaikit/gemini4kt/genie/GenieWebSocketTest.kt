package io.github.ugaikit.gemini4kt.genie

import io.github.ugaikit.gemini4kt.GeminiAI
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpStatusCode
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class GenieWebSocketTest {
    @Test
    fun connectsWithScopedTokenAndExchangesFrames() =
        runTest {
            val incoming = Channel<Frame>(Channel.UNLIMITED)
            val outgoing = Channel<Frame>(Channel.UNLIMITED)
            val socket = mockk<DefaultWebSocketSession>(relaxed = true)
            every { socket.coroutineContext } returns EmptyCoroutineContext
            coEvery { socket.send(any()) } coAnswers { outgoing.send(firstArg()) }
            every { socket.incoming } returns incoming
            every { socket.outgoing } returns outgoing
            val engine =
                MockEngine { request ->
                    assertEquals("/v1beta/interactions/live", request.url.encodedPath)
                    assertEquals("auth_tokens/scoped", request.url.parameters["auth_token"])
                    assertEquals(null, request.headers["x-goog-api-key"])
                    upgrade(socket)
                }
            try {
                HttpClient(engine).use { client ->
                    val api = GeminiAI(client = client, apiKey = "server-key")
                    val session =
                        withContext(Dispatchers.Default) {
                            api.connectGenieWebSocket("interactions/world", InteractionAuthToken("auth_tokens/scoped"))
                        }
                    try {
                        assertEquals(
                            """{"event_type":"interaction.connect","id":"world"}""",
                            assertIs<Frame.Text>(outgoing.receive()).readText(),
                        )
                        session.setupWorld(text = "A forest")
                        session.cancel()
                        assertEquals(
                            """{"event_type":"world_setup","world_prompt":{"text":"A forest"}}""",
                            assertIs<Frame.Text>(outgoing.receive()).readText(),
                        )
                        assertEquals(
                            """{"event_type":"interaction.cancel"}""",
                            assertIs<Frame.Text>(outgoing.receive()).readText(),
                        )
                        assertReceivedMedia(session, incoming)
                    } finally {
                        session.close()
                    }
                    assertIs<Frame.Close>(outgoing.receive())
                }
            } finally {
                engine.close()
                incoming.cancel()
                outgoing.cancel()
            }
        }

    private suspend fun assertReceivedMedia(
        session: GenieSession,
        incoming: Channel<Frame>,
    ) {
        val delta = """{"event_type":"step.delta","delta":{"type":"video","data":"AQID","mime_type":"image/jpeg"}}"""
        val terminal = """{"event_type":"interaction.cancelled"}"""
        incoming.send(Frame.Ping(byteArrayOf()))
        incoming.send(Frame.Text(delta))
        incoming.send(Frame.Binary(true, terminal.encodeToByteArray()))
        val events = session.receive().toList()
        assertEquals(listOf("step.delta", "interaction.cancelled"), events.map { it.eventType })
        assertEquals("AQID", events.first().delta?.data)
        assertEquals("image/jpeg", events.first().delta?.mimeType)
    }

    @Test
    fun cancelsUpgradedSocketWhenSendingConnectFrameFails() =
        runTest {
            val socketJob = Job()
            val socket = mockk<DefaultWebSocketSession>(relaxed = true)
            val failure = IllegalStateException("Could not send interaction.connect")
            every { socket.coroutineContext } returns socketJob
            coEvery { socket.send(any()) } throws failure
            val engine = MockEngine { upgrade(socket) }
            try {
                HttpClient(engine).use { client ->
                    val api = GeminiAI(client = client, apiKey = "server-key")
                    val thrown =
                        assertFailsWith<IllegalStateException> {
                            withContext(Dispatchers.Default) { api.connectGenieWebSocket("world") }
                        }
                    assertEquals(failure.message, thrown.message)
                    assertTrue(socketJob.isCancelled)
                }
            } finally {
                socketJob.cancel()
                engine.close()
            }
        }

    private fun MockRequestHandleScope.upgrade(socket: DefaultWebSocketSession): HttpResponseData {
        val response = respond("", HttpStatusCode.SwitchingProtocols)
        return HttpResponseData(
            response.statusCode,
            response.requestTime,
            response.headers,
            response.version,
            socket,
            response.callContext,
        )
    }

    @Test
    fun sendsApiKeyHeaderAndSurfacesHandshakeFailure() =
        runTest {
            val engine =
                MockEngine { request ->
                    assertEquals(null, request.url.parameters["auth_token"])
                    assertEquals("server-key", request.headers["x-goog-api-key"])
                    respond("Unauthorized", HttpStatusCode.Unauthorized)
                }
            try {
                HttpClient(engine).use { client ->
                    val api = GeminiAI(client = client, apiKey = "server-key")
                    assertFailsWith<IllegalStateException> {
                        withContext(Dispatchers.Default) { api.connectGenieWebSocket("world") }
                    }
                }
            } finally {
                engine.close()
            }
        }
}
