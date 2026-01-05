package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.Content
import io.github.ugaikit.gemini4kt.InlineData
import io.github.ugaikit.gemini4kt.Part
import io.github.ugaikit.gemini4kt.live.BidiGenerateContentClientContent
import io.github.ugaikit.gemini4kt.live.BidiGenerateContentRealtimeInput
import io.github.ugaikit.gemini4kt.live.BidiGenerateContentServerContent
import io.github.ugaikit.gemini4kt.live.BidiGenerateContentServerMessage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LiveSampleTest {
    @Test
    fun runStreamsAudioAndClosesSession() =
        runTest {
            val session = FakeSession()
            val client = FakeLiveClient(session)
            val audioData = mutableListOf<String>()

            val job =
                launch {
                    LiveSample.run(
                        inputAudioBase64 = "input-audio",
                        onAudioData = { audioData.add(it) },
                        clientFactory = { client },
                    )
                }

            session.emit(
                BidiGenerateContentServerMessage(
                    serverContent =
                        BidiGenerateContentServerContent(
                            turnComplete = true,
                            modelTurn =
                                Content(
                                    parts =
                                        arrayOf(
                                            Part(
                                                inlineData =
                                                    InlineData(
                                                        mimeType = "audio/wav",
                                                        data = "server-audio",
                                                    ),
                                            ),
                                        ),
                                ),
                        ),
                ),
            )

            job.join()

            assertEquals(listOf("server-audio"), audioData)
            assertEquals(2, session.realtimeInputs.size)
            val firstInput = session.realtimeInputs.first()
            assertEquals("input-audio", firstInput.media?.data)
            assertEquals("audio/pcm;rate=16000", firstInput.media?.mimeType)
            assertTrue(session.realtimeInputs.last().audioStreamEnd == true)
            assertEquals(1, session.clientContents.size)
            assertTrue(session.closed)
        }

    @Test
    fun runTimesOutWhenTurnCompleteMissing() =
        runTest {
            val session = FakeSession()
            val client = FakeLiveClient(session)

            val job =
                launch {
                    LiveSample.run(
                        inputAudioBase64 = null,
                        onAudioData = {},
                        clientFactory = { client },
                    )
                }

            advanceTimeBy(30_000)
            advanceUntilIdle()
            job.join()

            assertTrue(session.closed)
            assertEquals(0, session.realtimeInputs.size)
            assertEquals(1, session.clientContents.size)
        }

    private class FakeSession : LiveSample.Session {
        private val messages = MutableSharedFlow<BidiGenerateContentServerMessage>(replay = 1)
        val realtimeInputs = mutableListOf<BidiGenerateContentRealtimeInput>()
        val clientContents = mutableListOf<BidiGenerateContentClientContent>()
        var closed: Boolean = false

        override fun receive() = messages

        override suspend fun sendRealtimeInput(input: BidiGenerateContentRealtimeInput) {
            realtimeInputs.add(input)
        }

        override suspend fun sendClientContent(content: BidiGenerateContentClientContent) {
            clientContents.add(content)
        }

        override suspend fun close() {
            closed = true
        }

        suspend fun emit(message: BidiGenerateContentServerMessage) {
            messages.emit(message)
        }
    }

    private class FakeLiveClient(
        private val session: FakeSession,
    ) : LiveSample.LiveClient {
        override suspend fun connect(): LiveSample.Session = session
    }
}
