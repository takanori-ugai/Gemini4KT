package io.github.ugaikit.gemini4kt

import io.github.ugaikit.gemini4kt.webhooks.CreateWebhookRequest
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
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GeminiAIWebhookTest {
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
    fun webhookCrudWorks() =
        runTest {
            val ai =
                createGeminiAI { request ->
                    val path = request.url.encodedPath
                    val response =
                        when {
                            path == "/v1beta/webhooks" && request.method == HttpMethod.Post -> {
                                val body = (request.body as TextContent).text
                                assertEquals("wh_123", request.url.parameters["webhookId"])
                                assertTrue(body.contains("\"name\":\"Build status\""))
                                assertTrue(body.contains("\"uri\":\"https://example.com/webhook\""))
                                assertTrue(body.contains("\"subscribed_events\":[\"interaction.completed\"]"))
                                """
                                {"id":"wh_123","name":"Build status","uri":"https://example.com/webhook","subscribed_events":["interaction.completed"],"state":"enabled"}
                                """.trimIndent()
                            }
                            path == "/v1beta/webhooks" && request.method == HttpMethod.Get -> {
                                assertEquals("10", request.url.parameters["pageSize"])
                                """
                                {"webhooks":[{"id":"wh_123","uri":"https://example.com/webhook"}],"next_page_token":"next-token"}
                                """.trimIndent()
                            }
                            path == "/v1beta/webhooks/wh_123" && request.method == HttpMethod.Get -> {
                                """{"id":"wh_123","uri":"https://example.com/webhook"}"""
                            }
                            path == "/v1beta/webhooks/wh_123" && request.method == HttpMethod.Delete -> ""
                            else -> error("Unexpected request: ${request.method} ${request.url}")
                        }

                    respond(
                        content = response,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )
                }

            val created =
                ai.createWebhook(
                    CreateWebhookRequest(
                        name = "Build status",
                        uri = "https://example.com/webhook",
                        subscribedEvents = listOf("interaction.completed"),
                    ),
                    webhookId = "wh_123",
                )
            assertEquals("wh_123", created.id)
            assertEquals("https://example.com/webhook", created.uri)

            val listed = ai.listWebhooks()
            assertEquals(1, listed.webhooks.size)
            assertEquals("next-token", listed.nextPageToken)

            val fetched = ai.getWebhook("wh_123")
            assertEquals("wh_123", fetched.id)

            ai.deleteWebhook("wh_123")
        }
}
