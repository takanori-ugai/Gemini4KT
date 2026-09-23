package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.GeminiAI
import io.github.ugaikit.gemini4kt.interaction.Interaction
import io.github.ugaikit.gemini4kt.live.MockWebSocketSession
import io.github.ugaikit.gemini4kt.live.music.AudioChunk
import io.github.ugaikit.gemini4kt.live.music.LiveMusicServerContent
import io.github.ugaikit.gemini4kt.live.music.LiveMusicServerMessage
import io.github.ugaikit.gemini4kt.live.music.LiveMusicSession
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MusicGenerationTest {
    @Test
    fun testLyriaMusicGenerationFlow() =
        runTest {
            val json =
                Json {
                    ignoreUnknownKeys = true
                    encodeDefaults = false
                    explicitNulls = false
                }
            val client =
                HttpClient(MockEngine) {
                    engine {
                        addHandler { request: HttpRequestData ->
                            assertEquals(HttpMethod.Post, request.method)
                            assertTrue(request.url.toString().contains("/v1beta/interactions"))
                            val body = (request.body as TextContent).text
                            assertTrue(body.contains("\"model\":\"lyria-3.5\""))
                            assertTrue(body.contains("A cinematic piano track"))
                            assertTrue(body.contains("\"type\":\"image\""))
                            assertTrue(body.contains("\"response_format\":{\"type\":\"audio\"}"))
                            assertTrue(body.contains("\"stream\":true"))

                            respond(
                                content =
                                    """
                                    event: interaction.created
                                    data: {"interaction":{"id":"lyria_123","status":"in_progress","model":"lyria-3.5"},"event_type":"interaction.created"}

                                    event: step.delta
                                    data: {"index":0,"delta":{"type":"text","text":"[Verse] Homeward"},"event_type":"step.delta"}

                                    event: step.delta
                                    data: {"index":0,"delta":{"type":"audio","data":"first-audio","mime_type":"audio/mp3"},"event_type":"step.delta"}

                                    event: step.delta
                                    data: {"index":0,"delta":{"type":"audio","data":"second-audio","mime_type":"audio/mp3"},"event_type":"step.delta"}

                                    event: interaction.completed
                                    data: {"interaction":{"id":"lyria_123","status":"completed","model":"lyria-3.5"},"event_type":"interaction.completed"}

                                    event: done
                                    data: [DONE]
                                    """.trimIndent() + "\n",
                                status = HttpStatusCode.OK,
                                headers = headersOf(HttpHeaders.ContentType, ContentType.Text.EventStream.toString()),
                            )
                        }
                    }
                    install(ContentNegotiation) {
                        json(json)
                    }
                }
            val ai = GeminiAI(client = client, apiKey = "test-api-key")
            val audioEvents = mutableListOf<String>()

            try {
                val interaction =
                    MusicGeneration.run(
                        prompt = "A cinematic piano track",
                        images = listOf(MusicGenerationImage("image-data", "image/jpeg")),
                        onAudioData = { audioEvents.add(it) },
                        retainAudioData = false,
                        client = ai,
                    )

                assertEquals("lyria_123", interaction.id)
                assertEquals("[Verse] Homeward", interaction.outputText)
                assertEquals(listOf("first-audio", "second-audio"), audioEvents)
                assertNull(interaction.outputAudio)
                assertNull(interaction.outputs)
                assertNull(interaction.steps)
            } finally {
                client.close()
            }
        }

    @Test
    fun testLyriaStreamingCompletesWithoutLyrics() =
        runTest {
            val interaction =
                runLyriaWithStreamResponse(
                    """
                    event: interaction.created
                    data: {"interaction":{"id":"lyria_audio_only","status":"in_progress"},"event_type":"interaction.created"}

                    event: step.delta
                    data: {"index":0,"delta":{"type":"audio","data":"audio-only"},"event_type":"step.delta"}

                    event: interaction.completed
                    data: {"interaction":{"id":"lyria_audio_only","status":"completed"},"event_type":"interaction.completed"}
                    """.trimIndent() + "\n",
                )

            assertEquals("lyria_audio_only", interaction.id)
            assertNull(interaction.outputText)
        }

    @Test
    fun testLyriaStreamingRejectsIncompleteInteraction() =
        runTest {
            assertFailsWith<IllegalStateException> {
                runLyriaWithStreamResponse(
                    """
                    event: interaction.created
                    data: {"interaction":{"id":"lyria_incomplete","status":"in_progress"},"event_type":"interaction.created"}

                    event: step.delta
                    data: {"index":0,"delta":{"type":"audio","data":"partial-audio"},"event_type":"step.delta"}
                    """.trimIndent() + "\n",
                )
            }
        }

    @Test
    fun testLyriaStreamingRejectsNonCompletedInteraction() =
        runTest {
            assertFailsWith<IllegalStateException> {
                runLyriaWithStreamResponse(
                    """
                    event: interaction.created
                    data: {"interaction":{"id":"lyria_not_completed","status":"in_progress"},"event_type":"interaction.created"}

                    event: interaction.completed
                    data: {"interaction":{"id":"lyria_not_completed","status":"in_progress"},"event_type":"interaction.completed"}
                    """.trimIndent() + "\n",
                )
            }
        }

    @Test
    fun testLyriaStreamingReportsErrorEvent() =
        runTest {
            assertFailsWith<IllegalStateException> {
                runLyriaWithStreamResponse(
                    """
                    event: error
                    data: {"error":{"message":"generation failed"},"event_type":"error"}
                    """.trimIndent() + "\n",
                )
            }
        }

    @Test
    fun testLyriaMusicGenerationFallsBackToAggregateOutputs() =
        runTest {
            val audioEvents =
                runLyriaWithResponse(
                    """
                    {
                      "id": "lyria_outputs",
                      "status": "completed",
                      "outputs": [
                        {"type": "text", "text": "Lyrics"},
                        {"type": "audio", "data": "aggregate-audio", "mime_type": "audio/mp3"}
                      ],
                      "steps": [
                        {"type": "model_output", "content": [{"type": "text", "text": "Lyrics"}]}
                      ]
                    }
                    """.trimIndent(),
                )

            assertEquals(listOf("aggregate-audio"), audioEvents)
        }

    @Test
    fun testLyriaMusicGenerationFallsBackToOutputAudio() =
        runTest {
            val audioEvents =
                runLyriaWithResponse(
                    """
                    {
                      "id": "lyria_output_audio",
                      "status": "completed",
                      "output_audio": {"type": "audio", "data": "output-audio", "mime_type": "audio/mp3"}
                    }
                    """.trimIndent(),
                )

            assertEquals(listOf("output-audio"), audioEvents)
        }

    @Test
    fun testLyriaMusicGenerationRejectsBlankPrompt() =
        runTest {
            assertFailsWith<IllegalArgumentException> {
                MusicGeneration.run(prompt = "  ", onAudioData = {}, apiKey = "test-api-key")
            }
        }

    @Test
    fun testLyriaMusicGenerationRejectsTooManyImages() =
        runTest {
            val images = List(11) { MusicGenerationImage("image-data", "image/jpeg") }

            assertFailsWith<IllegalArgumentException> {
                MusicGeneration.run(
                    prompt = "A cinematic piano track",
                    images = images,
                    onAudioData = {},
                    apiKey = "test-api-key",
                )
            }
        }

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

                MusicGeneration.run(
                    onAudioData = { audioEvents.add(it) },
                    liveMusicClient = null,
                    apiKey = "test-api-key",
                    sessionFactory = { musicSession },
                )
            } finally {
                incoming.close()
                client.close()
            }

            assertEquals(51, audioEvents.size)
        }

    private suspend fun runLyriaWithResponse(response: String): List<String> {
        val json =
            Json {
                ignoreUnknownKeys = true
                encodeDefaults = false
                explicitNulls = false
            }
        val client =
            HttpClient(MockEngine) {
                engine {
                    addHandler {
                        respond(
                            content = response,
                            status = HttpStatusCode.OK,
                            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                        )
                    }
                }
                install(ContentNegotiation) {
                    json(json)
                }
            }
        val ai = GeminiAI(client = client, apiKey = "test-api-key")
        val audioEvents = mutableListOf<String>()

        return try {
            MusicGeneration.run(
                prompt = "A cinematic piano track",
                onAudioData = { audioEvents.add(it) },
                client = ai,
            )
            audioEvents
        } finally {
            client.close()
        }
    }

    private suspend fun runLyriaWithStreamResponse(response: String): Interaction {
        val json =
            Json {
                ignoreUnknownKeys = true
                encodeDefaults = false
                explicitNulls = false
            }
        val client =
            HttpClient(MockEngine) {
                engine {
                    addHandler {
                        respond(
                            content = response,
                            status = HttpStatusCode.OK,
                            headers = headersOf(HttpHeaders.ContentType, ContentType.Text.EventStream.toString()),
                        )
                    }
                }
                install(ContentNegotiation) {
                    json(json)
                }
            }
        val ai = GeminiAI(client = client, apiKey = "test-api-key")

        return try {
            MusicGeneration.run(
                prompt = "A cinematic piano track",
                onAudioData = {},
                retainAudioData = false,
                client = ai,
            )
        } finally {
            client.close()
        }
    }
}
