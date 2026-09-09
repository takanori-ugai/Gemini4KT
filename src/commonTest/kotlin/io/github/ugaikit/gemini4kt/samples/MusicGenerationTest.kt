package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.GeminiAI
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
                            assertTrue(request.url.toString().endsWith("/v1beta/interactions"))
                            val body = (request.body as TextContent).text
                            assertTrue(body.contains("\"model\":\"lyria-3.5\""))
                            assertTrue(body.contains("A cinematic piano track"))
                            assertTrue(body.contains("\"type\":\"image\""))
                            assertTrue(body.contains("\"response_format\":{\"type\":\"audio\"}"))

                            respond(
                                content =
                                    """
                                    {
                                      "id": "lyria_123",
                                      "status": "completed",
                                      "output_text": "[Verse] Homeward",
                                      "output_audio": {"type": "audio", "data": "fallback-audio", "mime_type": "audio/mp3"},
                                      "steps": [
                                        {
                                          "type": "model_output",
                                          "content": [
                                            {"type": "text", "text": "[Verse] Homeward"},
                                            {"type": "audio", "data": "first-audio", "mime_type": "audio/mp3"}
                                          ]
                                        },
                                        {
                                          "type": "model_output",
                                          "content": [
                                            {"type": "audio", "data": "second-audio", "mime_type": "audio/mp3"}
                                          ]
                                        }
                                      ]
                                    }
                                    """.trimIndent(),
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

            try {
                val interaction =
                    MusicGeneration.run(
                        prompt = "A cinematic piano track",
                        images = listOf(MusicGenerationImage("image-data", "image/jpeg")),
                        onAudioData = { audioEvents.add(it) },
                        client = ai,
                    )

                assertEquals("lyria_123", interaction.id)
                assertEquals("[Verse] Homeward", interaction.outputText)
                assertEquals(listOf("first-audio", "second-audio"), audioEvents)
            } finally {
                client.close()
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
}
