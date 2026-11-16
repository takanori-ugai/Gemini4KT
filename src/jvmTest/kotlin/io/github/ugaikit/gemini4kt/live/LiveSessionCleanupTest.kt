package io.github.ugaikit.gemini4kt.live

import io.github.ugaikit.gemini4kt.live.music.LiveMusicServerMessage
import io.github.ugaikit.gemini4kt.live.music.LiveMusicSession
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.websocket.WebSocketSession
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LiveSessionCleanupTest {
    @Test
    fun cancelledCallerClosesOwnedClients() =
        runTest {
            for (music in listOf(false, true)) {
                val client = HttpClient(MockEngine) { engine { addHandler { respond("OK") } } }
                val clientJob = requireNotNull(client.coroutineContext[Job])
                val socket = mockk<WebSocketSession>(relaxed = true)
                coEvery { socket.send(any()) } coAnswers { currentCoroutineContext().ensureActive() }
                val messages = Channel<BidiGenerateContentServerMessage>()
                val musicMessages = Channel<LiveMusicServerMessage>()
                val listener = launch(start = CoroutineStart.UNDISPATCHED) { awaitCancellation() }
                val close: suspend () -> Unit =
                    if (music) {
                        LiveMusicSession(socket, musicMessages, Json, listener, client, true)::close
                    } else {
                        GeminiLiveSession(socket, messages, Json, listener, client, true)::close
                    }
                try {
                    val caller =
                        launch {
                            currentCoroutineContext().cancel()
                            close()
                        }
                    caller.join()
                    assertTrue(listener.isCompleted)
                    assertTrue(if (music) musicMessages.tryReceive().isClosed else messages.tryReceive().isClosed)
                    assertFalse(clientJob.isActive)
                } finally {
                    listener.cancel()
                    messages.close()
                    musicMessages.close()
                    client.close()
                }
            }
        }

    @Test
    fun closingSessionsPreservesBorrowedClients() =
        runTest {
            for (music in listOf(false, true)) {
                val client = HttpClient(MockEngine) { engine { addHandler { respond("OK") } } }
                val socket = mockk<WebSocketSession>(relaxed = true)
                val listener = Job()
                try {
                    if (music) {
                        LiveMusicSession(socket, Channel(), Json, listener, client, false).close()
                    } else {
                        GeminiLiveSession(socket, Channel(), Json, listener, client, false).close()
                    }
                    assertTrue(requireNotNull(client.coroutineContext[Job]).isActive)
                    assertTrue(listener.isCompleted)
                } finally {
                    listener.cancel()
                    client.close()
                }
            }
        }
}
