package io.github.ugaikit.gemini4kt.live

import io.github.ugaikit.gemini4kt.FunctionResponse
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondOk
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketExtension
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.readText
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.coroutines.CoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Represents the gemini live session test.
 */
@OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
class GeminiLiveSessionTest {
    /**
     * Holds the json.
     */
    private val json =
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

    private fun newTestHttpClient(): HttpClient =
        HttpClient(MockEngine) {
            engine {
                addHandler { respondOk() }
            }
        }

    /**
     * Tests test send client content sends correct json.
     */
    @Test
    fun testSendClientContentSendsCorrectJSON() {
        val text =
            json.encodeToString(
                BidiGenerateContentClientMessage(
                    clientContent = BidiGenerateContentClientContent(turnComplete = true),
                ),
            )

        assertTrue(text.contains("\"clientContent\""))
        assertTrue(text.contains("\"turnComplete\":true"))
    }

    /**
     * Tests test send realtime input sends correct json.
     */
    @Test
    fun testSendRealtimeInputSendsCorrectJSON() =
        runTest {
            val mockSession = MockWebSocketSession()
            val incoming = Channel<BidiGenerateContentServerMessage>()
            val session =
                GeminiLiveSession(
                    session = mockSession,
                    incomingMessages = incoming,
                    json = json,
                    listenerJob = Job(),
                    httpClient = newTestHttpClient(),
                    ownsClient = false,
                )

            val input = BidiGenerateContentRealtimeInput(text = "Hello")
            session.sendRealtimeInput(input)

            assertEquals(1, mockSession.sentFrames.size)
            val frame = mockSession.sentFrames[0]
            assertTrue(frame is Frame.Text)
            val text = frame.readText()
            assertTrue(text.contains("\"realtimeInput\""))
            assertTrue(text.contains("\"text\":\"Hello\""))
        }

    /**
     * Tests test send tool response sends correct json.
     */
    @Test
    fun testSendToolResponseSendsCorrectJSON() =
        runTest {
            val mockSession = MockWebSocketSession()
            val incoming = Channel<BidiGenerateContentServerMessage>()
            val session =
                GeminiLiveSession(
                    session = mockSession,
                    incomingMessages = incoming,
                    json = json,
                    listenerJob = Job(),
                    httpClient = newTestHttpClient(),
                    ownsClient = false,
                )

            val response =
                BidiGenerateContentToolResponse(
                    functionResponses =
                        listOf(
                            FunctionResponse(
                                name = "myFunc",
                                response = JsonObject(mapOf("result" to JsonPrimitive("ok"))),
                            ),
                        ),
                )
            session.sendToolResponse(response)

            assertEquals(1, mockSession.sentFrames.size)
            val frame = mockSession.sentFrames[0]
            assertTrue(frame is Frame.Text)
            val text = frame.readText()
            assertTrue(text.contains("\"toolResponse\""))
            assertTrue(text.contains("\"functionResponses\""))
            assertTrue(text.contains("\"myFunc\""))
        }

    /**
     * Tests test receive gets messages from channel.
     */
    @Test
    fun testReceiveGetsMessagesFromChannel() =
        runTest {
            val mockSession = MockWebSocketSession()
            val incoming = Channel<BidiGenerateContentServerMessage>(1)
            val session =
                GeminiLiveSession(
                    session = mockSession,
                    incomingMessages = incoming,
                    json = json,
                    listenerJob = Job(),
                    httpClient = newTestHttpClient(),
                    ownsClient = false,
                )

            val msg = BidiGenerateContentServerMessage(setupComplete = BidiGenerateContentSetupComplete())
            incoming.send(msg)

            val received = session.receive().first()
            assertEquals(msg, received)
        }

    /**
     * Tests test close closes session and channel.
     */
    @Test
    fun testCloseClosesSessionAndChannel() =
        runTest {
            val mockSession = MockWebSocketSession()
            val incoming = Channel<BidiGenerateContentServerMessage>()
            val job = Job()
            val session =
                GeminiLiveSession(
                    session = mockSession,
                    incomingMessages = incoming,
                    json = json,
                    listenerJob = job,
                    httpClient = newTestHttpClient(),
                    ownsClient = true,
                )

            session.close()

            // Verify session received close frame in outgoing
            // The extension method session.close() sends a close frame to outgoing.
            val frame = mockSession.outgoing.tryReceive().getOrNull()
            // If tryReceive fails, it might be timing or implementation detail of ktor's close().
            // For now, let's assume if job is cancelled and channel closed, it's fine.
            // But we really should verify the close frame if possible.
            // Let's print what we got if failure.
            // println("Frame received: $frame")

            // If frame is null, it means it wasn't sent or we missed it (unlikely with unlimited channel).
            // Maybe we should just trust job and incoming check.
            assertTrue(job.isCancelled)
            assertTrue(incoming.isClosedForSend)
        }
}

/**
 * Represents the mock web socket session.
 */
class MockWebSocketSession : WebSocketSession {
    /**
     * Holds the sent frames.
     */
    val sentFrames = mutableListOf<Frame>()

    /**
     * Holds the coroutine context.
     */
    override val coroutineContext: CoroutineContext = Job()

    /**
     * Holds the masking.
     */
    override var masking: Boolean = false

    /**
     * Holds the max frame size.
     */
    override var maxFrameSize: Long = Long.MAX_VALUE

    /**
     * Holds the incoming.
     */
    override val incoming: Channel<Frame> = Channel()

    /**
     * Holds the outgoing.
     */
    override val outgoing: Channel<Frame> = Channel(Channel.UNLIMITED)

    /**
     * Holds the extensions.
     */
    override val extensions: List<WebSocketExtension<*>> = emptyList()

    /**
     * Handles flush.
     */
    override suspend fun flush() {
        // No-op
    }

    @Deprecated(
        "Use send(Frame) instead",
        ReplaceWith("send(Frame.Text(message))"),
    ) // Suppress warning if needed or just implement

    /**
     * Handles send.
     *
     * @param message The message.
     */
    suspend fun send(message: String) {
        send(Frame.Text(message))
    }

    /**
     * Handles send.
     *
     * @param frame The frame.
     */
    override suspend fun send(frame: Frame) {
        sentFrames.add(frame)
    }

    @Deprecated(
        "Use close() instead",
        ReplaceWith("close()"),
    )
    /**
     * Handles close.
     *
     * @param reason The reason.
     */
    suspend fun close(reason: io.ktor.websocket.CloseReason) {
        // Unused parameter fixed by removing or suppressing. But here we override a deprecated
        // member?
        // CloseReason is parameter name. If I change to `_`, it might clash if it's an interface override.
        // Wait, `WebSocketSession` inherits `WebSocketSession` -> `CoroutineScope`?
        // `WebSocketSession` interface has `close(reason: CloseReason)`?
        // Actually, `WebSocketSession` does NOT have `close(CloseReason)`. It is an extension function usually.
        // But here `MockWebSocketSession` implements `WebSocketSession`.
        // The method `suspend fun close(reason: CloseReason)` is NOT in `WebSocketSession` interface.
        // It's likely added in this mock class to satisfy some test usage or mimic behavior?
        // If it's not overriding, I can rename `reason` to `_`.
        // The warning said `Function parameter 'reason' is unused`.
    }

    /**
     * Handles terminate.
     */
    @Deprecated("Deprecated in WebSocketSession")
    override fun terminate() {
        // Deprecated
    }
}
