package io.github.ugaikit.gemini4kt.live.music

import io.github.ugaikit.gemini4kt.live.MockWebSocketSession
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
class LiveMusicTest {
    private val json =
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

    private data class TrackedClient(
        val client: HttpClient,
        val closed: CompletableDeferred<Unit>,
    )

    private fun buildTrackingClient(): TrackedClient {
        val closed = CompletableDeferred<Unit>()
        val client =
            HttpClient(MockEngine) {
                install(WebSockets)
                engine {
                    addHandler { respondOk() }
                }
            }
        client.coroutineContext[Job]?.invokeOnCompletion { closed.complete(Unit) }
        return TrackedClient(client, closed)
    }

    private fun buildSession(
        mockSession: MockWebSocketSession,
        incoming: Channel<LiveMusicServerMessage>,
        listenerJob: Job = Job(),
        ownsClient: Boolean = true,
    ): LiveMusicSession =
        buildTrackingClient().let { tracked ->
            LiveMusicSession(
                session = mockSession,
                incomingMessages = incoming,
                json = json,
                listenerJob = listenerJob,
                httpClient = tracked.client,
                ownsClient = ownsClient,
            )
        }

    private fun buildSessionWithClient(
        mockSession: MockWebSocketSession,
        incoming: Channel<LiveMusicServerMessage>,
        listenerJob: Job = Job(),
        trackedClient: TrackedClient,
        ownsClient: Boolean,
    ): LiveMusicSession =
        LiveMusicSession(
            session = mockSession,
            incomingMessages = incoming,
            json = json,
            listenerJob = listenerJob,
            httpClient = trackedClient.client,
            ownsClient = ownsClient,
        )

    @Test
    fun testSetWeightedPromptsSendsCorrectJSON() =
        runTest {
            val mockSession = MockWebSocketSession()
            val incoming = Channel<LiveMusicServerMessage>()
            val session = buildSession(mockSession, incoming)

            val prompts =
                listOf(
                    WeightedPrompt("Prompt 1", 0.8),
                    WeightedPrompt("Prompt 2", 0.2),
                )
            session.setWeightedPrompts(prompts)

            assertEquals(1, mockSession.sentFrames.size)
            val frame = mockSession.sentFrames[0]
            assertTrue(frame is Frame.Text)
            val text = frame.readText()
            assertTrue(text.contains("\"clientContent\""))
            assertTrue(text.contains("\"weightedPrompts\""))
            assertTrue(text.contains("\"text\":\"Prompt 1\""))
            assertTrue(text.contains("\"weight\":0.8"))
        }

    @Test
    fun testSetMusicGenerationConfigSendsCorrectJSON() =
        runTest {
            val mockSession = MockWebSocketSession()
            val incoming = Channel<LiveMusicServerMessage>()
            val session = buildSession(mockSession, incoming)

            val config =
                LiveMusicGenerationConfig(
                    temperature = 0.7,
                    bpm = 120,
                    scale = Scale.C_MAJOR_A_MINOR,
                )
            session.setMusicGenerationConfig(config)

            assertEquals(1, mockSession.sentFrames.size)
            val frame = mockSession.sentFrames[0]
            assertTrue(frame is Frame.Text)
            val text = frame.readText()
            assertTrue(text.contains("\"musicGenerationConfig\""))
            assertTrue(text.contains("\"temperature\":0.7"))
            assertTrue(text.contains("\"bpm\":120"))
            assertTrue(text.contains("\"scale\":\"C_MAJOR_A_MINOR\""))
        }

    @Test
    fun testPlaySendsCorrectJSON() =
        runTest {
            val mockSession = MockWebSocketSession()
            val incoming = Channel<LiveMusicServerMessage>()
            val session = buildSession(mockSession, incoming)

            session.play()

            assertEquals(1, mockSession.sentFrames.size)
            val frame = mockSession.sentFrames[0]
            assertTrue(frame is Frame.Text)
            val text = frame.readText()
            assertTrue(text.contains("\"playbackControl\":\"PLAY\""))
        }

    @Test
    fun testPauseSendsCorrectJSON() =
        runTest {
            val mockSession = MockWebSocketSession()
            val incoming = Channel<LiveMusicServerMessage>()
            val session = buildSession(mockSession, incoming)

            session.pause()

            assertEquals(1, mockSession.sentFrames.size)
            val frame = mockSession.sentFrames[0]
            assertTrue(frame is Frame.Text)
            val text = frame.readText()
            assertTrue(text.contains("\"playbackControl\":\"PAUSE\""))
        }

    @Test
    fun testStopSendsCorrectJSON() =
        runTest {
            val mockSession = MockWebSocketSession()
            val incoming = Channel<LiveMusicServerMessage>()
            val session = buildSession(mockSession, incoming)

            session.stop()

            assertEquals(1, mockSession.sentFrames.size)
            val frame = mockSession.sentFrames[0]
            assertTrue(frame is Frame.Text)
            val text = frame.readText()
            assertTrue(text.contains("\"playbackControl\":\"STOP\""))
        }

    @Test
    fun testResetContextSendsCorrectJSON() =
        runTest {
            val mockSession = MockWebSocketSession()
            val incoming = Channel<LiveMusicServerMessage>()
            val session = buildSession(mockSession, incoming)

            session.resetContext()

            assertEquals(1, mockSession.sentFrames.size)
            val frame = mockSession.sentFrames[0]
            assertTrue(frame is Frame.Text)
            val text = frame.readText()
            assertTrue(text.contains("\"playbackControl\":\"RESET_CONTEXT\""))
        }

    @Test
    fun testReceiveGetsMessagesFromChannel() =
        runTest {
            val mockSession = MockWebSocketSession()
            val incoming = Channel<LiveMusicServerMessage>(1)
            val session = buildSession(mockSession, incoming)

            val msg = LiveMusicServerMessage(setupComplete = LiveMusicServerSetupComplete())
            incoming.send(msg)

            val received = session.receive().first()
            assertEquals(msg, received)
        }

    @Test
    fun testReceiveAudioChunk() =
        runTest {
            val mockSession = MockWebSocketSession()
            val incoming = Channel<LiveMusicServerMessage>(1)
            val session = buildSession(mockSession, incoming)

            val chunk = AudioChunk(data = "base64data", mimeType = "audio/pcm")
            val msg = LiveMusicServerMessage(serverContent = LiveMusicServerContent(audioChunks = listOf(chunk)))
            incoming.send(msg)

            val received = session.receive().first()
            assertEquals(chunk, received.serverContent?.audioChunks?.firstOrNull())
        }

    @Test
    fun testCloseClosesSessionAndChannel() =
        runTest {
            val mockSession = MockWebSocketSession()
            val incoming = Channel<LiveMusicServerMessage>()
            val job = Job()
            val session = buildSession(mockSession, incoming, job)

            session.close()

            assertTrue(job.isCancelled)
            assertTrue(incoming.isClosedForSend)
        }

    @Test
    fun testCloseClosesOwnedHttpClient() =
        runTest {
            val mockSession = MockWebSocketSession()
            val incoming = Channel<LiveMusicServerMessage>()
            val job = Job()
            val client = buildTrackingClient()
            val session = buildSessionWithClient(mockSession, incoming, job, client, ownsClient = true)

            session.close()

            assertTrue(job.isCancelled)
            assertTrue(incoming.isClosedForSend)
            assertTrue(client.closed.isCompleted)
        }

    @Test
    fun testCloseDoesNotCloseUnownedHttpClient() =
        runTest {
            val mockSession = MockWebSocketSession()
            val incoming = Channel<LiveMusicServerMessage>()
            val job = Job()
            val client = buildTrackingClient()
            val session = buildSessionWithClient(mockSession, incoming, job, client, ownsClient = false)

            session.close()

            assertTrue(job.isCancelled)
            assertTrue(incoming.isClosedForSend)
            assertFalse(client.closed.isCompleted)
        }

    @Test
    fun testServerMessageRoundTripsMetadataAndFilteredPrompt() {
        val message =
            LiveMusicServerMessage(
                serverContent =
                    LiveMusicServerContent(
                        audioChunks =
                            listOf(
                                AudioChunk(
                                    data = "ZGF0YQ==",
                                    mimeType = "audio/pcm",
                                    sourceMetadata =
                                        LiveMusicSourceMetadata(
                                            clientContent =
                                                LiveMusicClientContent(weightedPrompts = emptyList()),
                                            musicGenerationConfig = LiveMusicGenerationConfig(bpm = 120),
                                        ),
                                ),
                            ),
                    ),
                filteredPrompt =
                    LiveMusicFilteredPrompt(
                        text = "prompt",
                        filteredReason = "policy",
                    ),
            )

        val encoded = json.encodeToString(message)
        val decoded = json.decodeFromString<LiveMusicServerMessage>(encoded)
        assertEquals("policy", decoded.filteredPrompt?.filteredReason)
        assertEquals(120, decoded.serverContent?.audioChunks?.first()?.sourceMetadata?.musicGenerationConfig?.bpm)
    }
}
