package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.live.MockWebSocketSession
import io.github.ugaikit.gemini4kt.live.music.AudioChunk
import io.github.ugaikit.gemini4kt.live.music.LiveMusicServerContent
import io.github.ugaikit.gemini4kt.live.music.LiveMusicServerMessage
import io.github.ugaikit.gemini4kt.live.music.LiveMusicSession
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.plugins.websocket.WebSockets
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class MusicGenerationTest {
    @Test
    fun testMusicGenerationFlow() =
        runTest {
            val json =
                Json {
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                }
            val mockSession = MockWebSocketSession()
            val incoming = Channel<LiveMusicServerMessage>(Channel.UNLIMITED)
            val client =
                HttpClient(MockEngine) {
                    install(WebSockets)
                    engine {
                        addHandler { respondOk() }
                    }
                }
            val audioEvents = mutableListOf<String>()

            try {
                val musicSession =
                    LiveMusicSession(
                        session = mockSession,
                        incomingMessages = incoming,
                        json = json,
                        listenerJob = Job(),
                        httpClient = client,
                        ownsClient = false,
                    )

                repeat(51) {
                    incoming.send(
                        LiveMusicServerMessage(
                            serverContent =
                                LiveMusicServerContent(
                                    audioChunks =
                                        listOf(
                                            AudioChunk(
                                                data = "chunk-$it",
                                                mimeType = "audio/pcm",
                                            ),
                                        ),
                                ),
                        ),
                    )
                }
                incoming.close()

                MusicGeneration.run(
                    onAudioData = { audioEvents.add(it) },
                    liveMusicClient = null,
                    apiKey = "test-api-key",
                    sessionFactory = { musicSession },
                )
            } finally {
                client.close()
            }

            assertEquals(51, audioEvents.size)
        }
}
