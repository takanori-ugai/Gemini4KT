package io.github.ugaikit.gemini4kt.live

import io.github.ugaikit.gemini4kt.live.music.LiveMusicServerMessage
import io.github.ugaikit.gemini4kt.live.music.LiveMusicSession
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LiveSessionCleanupTest {
    @Test
    fun cancelledCallerClosesOwnedClients() =
        runTest {
            for (music in listOf(false, true)) {
                val fixture = SessionFixture(music, ownsClient = true, scope = this)
                try {
                    val caller =
                        launch {
                            currentCoroutineContext().cancel()
                            fixture.close()
                        }
                    caller.join()
                    assertTrue(caller.isCancelled)
                    fixture.assertReleased()
                } finally {
                    fixture.dispose()
                }
            }
        }

    @Test
    fun closingSessionsPreservesBorrowedClients() =
        runTest {
            for (music in listOf(false, true)) {
                val fixture = SessionFixture(music, ownsClient = false, scope = this)
                try {
                    fixture.close()
                    fixture.assertReleased()
                } finally {
                    fixture.dispose()
                }
            }
        }

    @Test
    fun suspendedGracefulCloseCompletesWithoutCancellingTheSocket() =
        runTest {
            for (music in listOf(false, true)) {
                for (ownsClient in listOf(false, true)) {
                    val fixture = SessionFixture(music, ownsClient, this)
                    coEvery { fixture.socket.flush() } coAnswers { delay(1) }
                    try {
                        fixture.close()
                        assertIs<Frame.Close>(fixture.outgoing.tryReceive().getOrThrow())
                        assertTrue(fixture.socketJob.isActive)
                        fixture.assertReleased()
                    } finally {
                        fixture.dispose()
                    }
                }
            }
        }

    @Test
    fun stalledOutgoingQueuesAreCancelledAfterTheCleanupTimeout() =
        runTest {
            for (music in listOf(false, true)) {
                for (ownsClient in listOf(false, true)) {
                    val fixture = SessionFixture(music, ownsClient, this, outgoingCapacity = 1)
                    fixture.outgoing.send(Frame.Text("Pending output"))
                    val startedAt = testScheduler.currentTime
                    try {
                        fixture.close()
                        assertEquals(LIVE_SESSION_CLOSE_TIMEOUT_MS, testScheduler.currentTime - startedAt)
                        assertTrue(fixture.socketJob.isCancelled)
                        fixture.assertReleased()
                    } finally {
                        fixture.dispose()
                    }
                }
            }
        }

    @Test
    fun stalledFlushIsBoundedEvenWhenTheCallerIsAlreadyCancelled() =
        runTest {
            for (music in listOf(false, true)) {
                for (ownsClient in listOf(false, true)) {
                    val fixture = SessionFixture(music, ownsClient, this)
                    var flushStarted = false
                    coEvery { fixture.socket.flush() } coAnswers {
                        flushStarted = true
                        awaitCancellation()
                    }
                    val startedAt = testScheduler.currentTime
                    try {
                        val caller =
                            launch {
                                currentCoroutineContext().cancel()
                                fixture.close()
                            }
                        caller.join()
                        assertTrue(flushStarted)
                        assertEquals(LIVE_SESSION_CLOSE_TIMEOUT_MS, testScheduler.currentTime - startedAt)
                        assertTrue(fixture.socketJob.isCancelled)
                        fixture.assertReleased()
                    } finally {
                        fixture.dispose()
                    }
                }
            }
        }

    @Test
    fun failedFlushStillReleasesSessionResources() =
        runTest {
            for (music in listOf(false, true)) {
                for (ownsClient in listOf(false, true)) {
                    val fixture = SessionFixture(music, ownsClient, this)
                    coEvery { fixture.socket.flush() } throws IOException("Disconnected")
                    try {
                        fixture.close()
                        fixture.assertReleased()
                    } finally {
                        fixture.dispose()
                    }
                }
            }
        }

    @Test
    fun cancellationDuringGracefulCloseStillReleasesResources() =
        runTest {
            for (music in listOf(false, true)) {
                for (ownsClient in listOf(false, true)) {
                    val fixture = SessionFixture(music, ownsClient, this)
                    coEvery { fixture.socket.flush() } coAnswers {
                        currentCoroutineContext().cancel(CancellationException("Transport cancelled close"))
                        currentCoroutineContext().ensureActive()
                    }
                    try {
                        assertFailsWith<CancellationException> { fixture.close() }
                        fixture.assertReleased()
                    } finally {
                        fixture.dispose()
                    }
                }
            }
        }

    private class SessionFixture(
        music: Boolean,
        private val ownsClient: Boolean,
        scope: CoroutineScope,
        outgoingCapacity: Int = Channel.UNLIMITED,
    ) {
        private val client = HttpClient(MockEngine) { engine { addHandler { respond("OK") } } }
        private val clientJob = requireNotNull(client.coroutineContext[Job])
        val socket = mockk<WebSocketSession>(relaxed = true)
        val socketJob = Job()
        val outgoing = Channel<Frame>(outgoingCapacity)
        private val messages = Channel<BidiGenerateContentServerMessage>()
        private val musicMessages = Channel<LiveMusicServerMessage>()
        private val listener = scope.launch(start = CoroutineStart.UNDISPATCHED) { awaitCancellation() }
        private val closeSession: suspend () -> Unit =
            if (music) {
                LiveMusicSession(socket, musicMessages, Json, listener, client, ownsClient)::close
            } else {
                GeminiLiveSession(socket, messages, Json, listener, client, ownsClient)::close
            }

        init {
            every { socket.coroutineContext } returns socketJob
            coEvery { socket.send(any()) } coAnswers {
                currentCoroutineContext().ensureActive()
                outgoing.send(firstArg())
            }
        }

        suspend fun close() {
            closeSession()
        }

        fun assertReleased() {
            assertTrue(listener.isCompleted)
            assertTrue(messages.tryReceive().isClosed || musicMessages.tryReceive().isClosed)
            assertEquals(!ownsClient, clientJob.isActive)
        }

        fun dispose() {
            listener.cancel()
            socketJob.cancel()
            outgoing.cancel()
            messages.close()
            musicMessages.close()
            client.close()
        }
    }
}
