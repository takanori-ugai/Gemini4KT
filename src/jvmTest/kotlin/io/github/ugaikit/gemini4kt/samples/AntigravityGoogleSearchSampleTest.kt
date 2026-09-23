package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.GeminiAI
import io.github.ugaikit.gemini4kt.TestHttpClientTracker
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue

class AntigravityGoogleSearchSampleTest {
    private val clientTracker = TestHttpClientTracker()

    private fun createGeminiAI(handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData): GeminiAI {
        val client =
            HttpClient(MockEngine) {
                engine {
                    addHandler(handler)
                }
                install(ContentNegotiation) {
                    json(
                        Json {
                            ignoreUnknownKeys = true
                            encodeDefaults = false
                            explicitNulls = false
                        },
                    )
                }
            }
        return GeminiAI(client = clientTracker.track(client), apiKey = "test-key")
    }

    @AfterTest
    fun closeClients() {
        clientTracker.closeAll()
    }

    @Test
    fun runPrintsGoogleSearchQueriesAndOutput() =
        runTest {
            val ai =
                createGeminiAI { request ->
                    assertTrue(request.method == HttpMethod.Post)
                    assertTrue(request.url.encodedPath == "/v1beta/interactions")
                    val body = (request.body as TextContent).text
                    assertTrue(body.contains("\"agent\":\"antigravity-preview-09-2026\""))
                    assertTrue(body.contains("\"type\":\"google_search\""))

                    respond(
                        content =
                            """
                            {
                              "id": "search-interaction-1",
                              "status": "completed",
                              "steps": [
                                {
                                  "type": "google_search_call",
                                  "id": "search-call-1",
                                  "arguments": {
                                    "queries": ["Gemini API Interactions API"]
                                  }
                                },
                                {
                                  "type": "model_output",
                                  "content": [
                                    {"type": "text", "text": "The Interactions API supports models, agents, and tools."}
                                  ]
                                }
                              ],
                              "output_text": "The Interactions API supports models, agents, and tools."
                            }
                            """.trimIndent(),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )
                }

            val output = captureStdout { AntigravityGoogleSearchSample.run(ai) }

            assertTrue(output.contains("google_search[0](queries=Gemini API Interactions API)"))
            assertTrue(output.contains("Output: The Interactions API supports models, agents, and tools."))
        }

    private suspend fun captureStdout(block: suspend () -> Unit): String {
        val original = System.out
        val buffer = java.io.ByteArrayOutputStream()
        val printStream = java.io.PrintStream(buffer, true, Charsets.UTF_8.name())
        return try {
            System.setOut(printStream)
            block()
            printStream.flush()
            buffer.toString(Charsets.UTF_8.name())
        } finally {
            System.setOut(original)
        }
    }
}
