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
import io.ktor.http.headersOf
import io.ktor.http.content.TextContent
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AgentSampleTest {
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
    fun runPrintsAgentLifecycle() =
        runTest {
            var createdAgentId: String? = null
            val ai =
                createGeminiAI { request ->
                    val path = request.url.encodedPath
                    val response =
                        when {
                            path == "/v1beta/agents" && request.method == HttpMethod.Post -> {
                                val body = (request.body as TextContent).text
                                assertTrue(body.contains("agent-sample-"))
                                assertTrue(body.contains("\"base_agent\":\"antigravity-preview-05-2026\""))
                                createdAgentId =
                                    "\"id\"\\s*:\\s*\"([^\"]+)\""
                                        .toRegex()
                                        .find(body)
                                        ?.groupValues
                                        ?.getOrNull(1)
                                        ?: error("Create agent request did not include an id")
                                """{"id":"$createdAgentId","base_agent":"antigravity-preview-05-2026"}"""
                            }
                            path == "/v1beta/agents" && request.method == HttpMethod.Get -> {
                                val agentId = createdAgentId ?: error("Agent id was not captured before get")
                                assertEquals("5", request.url.parameters["pageSize"])
                                """{"agents":[{"id":"$agentId"}],"nextPageToken":""}"""
                            }
                            path.startsWith("/v1beta/agents/") -> {
                                val agentId = createdAgentId ?: error("Agent id was not captured before agent lookup")
                                assertEquals("/v1beta/agents/$agentId", path)
                                when (request.method) {
                                    HttpMethod.Get ->
                                        """{"id":"$agentId","base_agent":"antigravity-preview-05-2026"}"""
                                    HttpMethod.Delete -> ""
                                    else -> error("Unexpected method for /v1beta/agents/$agentId: ${request.method}")
                                }
                            }
                            path == "/v1beta/interactions" -> {
                                assertEquals(HttpMethod.Post, request.method)
                                val body = (request.body as TextContent).text
                                val agentId = createdAgentId ?: error("Agent id was not captured before interaction")
                                assertTrue(body.contains("\"agent\":\"$agentId\""))
                                """{"id":"interaction-1","status":"completed","output_text":"It is a short demo agent."}"""
                            }
                            else -> error("Unexpected request path: ${request.url}")
                        }

                    respond(
                        content = response,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )
                }

            val output =
                captureStdout {
                    AgentSample.run(ai)
                }

            assertTrue(output.contains("--- Agent API Sample ---"))
            assertTrue(output.contains("Created agent:"))
            assertTrue(output.contains("Retrieved agent:"))
            assertTrue(output.contains("First page agent ids:"))
            assertTrue(output.contains("Interaction id: interaction-1"))
            assertTrue(output.contains("Interaction status: completed"))
            assertTrue(output.contains("Interaction output: It is a short demo agent."))
            assertTrue(output.contains("Deleted agent:"))
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
