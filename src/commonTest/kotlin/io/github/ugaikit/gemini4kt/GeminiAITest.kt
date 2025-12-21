package io.github.ugaikit.gemini4kt

import io.github.ugaikit.gemini4kt.interaction.CreateInteractionRequest
import io.github.ugaikit.gemini4kt.interaction.InteractionStatus
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
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GeminiAITest {
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
        return GeminiAI(client = client, apiKey = "test-key")
    }

    @Test
    fun testCreateInteraction() =
        runTest {
            val responseJson =
                """
                {
                  "id": "v1_123",
                  "status": "completed",
                  "outputs": [
                    {
                      "type": "text",
                      "text": "Hello world"
                    }
                  ]
                }
                """.trimIndent()

            val geminiAI =
                createGeminiAI { request ->
                    assertEquals(HttpMethod.Post, request.method)
                    assertTrue(request.url.toString().contains("v1beta/interactions"))
                    respond(
                        content = responseJson,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )
                }

            val request =
                CreateInteractionRequest(
                    model = "gemini-2.5-flash",
                    input = JsonPrimitive("Hello"),
                )
            val interaction = geminiAI.createInteraction(request)

            assertEquals("v1_123", interaction.id)
            assertEquals(InteractionStatus.COMPLETED, interaction.status)
            assertEquals(1, interaction.outputs?.size)
            assertEquals("text", interaction.outputs?.get(0)?.type)
            assertEquals("Hello world", interaction.outputs?.get(0)?.text)
        }

    @Test
    fun testGetInteraction() =
        runTest {
            val responseJson =
                """
                {
                  "id": "v1_123",
                  "status": "completed"
                }
                """.trimIndent()

            val geminiAI =
                createGeminiAI { request ->
                    assertEquals(HttpMethod.Get, request.method)
                    assertTrue(request.url.toString().contains("v1beta/interactions/v1_123"))
                    respond(
                        content = responseJson,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )
                }

            val interaction = geminiAI.getInteraction("v1_123")
            assertEquals("v1_123", interaction.id)
            assertEquals(InteractionStatus.COMPLETED, interaction.status)
        }

    @Test
    fun testCancelInteraction() =
        runTest {
            val responseJson =
                """
                {
                  "id": "v1_123",
                  "status": "cancelled"
                }
                """.trimIndent()

            val geminiAI =
                createGeminiAI { request ->
                    assertEquals(HttpMethod.Post, request.method)
                    assertTrue(request.url.toString().contains("v1beta/interactions/v1_123/cancel"))
                    respond(
                        content = responseJson,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )
                }

            val interaction = geminiAI.cancelInteraction("v1_123")
            assertEquals("v1_123", interaction.id)
            assertEquals(InteractionStatus.CANCELLED, interaction.status)
        }

    @Test
    fun testDeleteInteraction() =
        runTest {
            val geminiAI =
                createGeminiAI { request ->
                    assertEquals(HttpMethod.Delete, request.method)
                    assertTrue(request.url.toString().contains("v1beta/interactions/v1_123"))
                    respond(
                        content = "",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )
                }

            geminiAI.deleteInteraction("v1_123")
        }
}
