package io.github.ugaikit.gemini4kt.batch

import io.github.ugaikit.gemini4kt.Content
import io.github.ugaikit.gemini4kt.GenerateContentRequest
import io.github.ugaikit.gemini4kt.Part
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BatchClientTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `createBatch sends correct request and parses response`() =
        runBlocking {
            val expectedResponse =
                """
                {
                  "name": "batches/123456",
                  "metadata": {
                      "state": "JOB_STATE_PENDING",
                      "createTime": "2024-01-01T00:00:00Z"
                  },
                  "done": false
                }
                """.trimIndent()

            val mockEngine =
                MockEngine { request ->
                    assertEquals(HttpMethod.Post, request.method)
                    assertEquals("https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:batchGenerateContent", request.url.toString())
                    respond(
                        content = expectedResponse,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            val client =
                HttpClient(mockEngine) {
                    install(ContentNegotiation) {
                        json(json)
                    }
                }
            val batchClient = Batch("api-key", client)

            val generateContentRequest =
                GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = "test")))),
                )
            val createBatchRequest =
                CreateBatchRequest(
                    batch =
                        BatchConfig(
                            inputConfig =
                                BatchInputConfig(
                                    requests =
                                        BatchRequestInput(
                                            requests =
                                                listOf(
                                                    BatchItemRequest(
                                                        request = json.encodeToJsonElement(generateContentRequest),
                                                    ),
                                                ),
                                        ),
                                ),
                        ),
                )

            val result = batchClient.createBatch("gemini-pro", createBatchRequest)

            assertEquals("batches/123456", result.name)
            assertEquals("JOB_STATE_PENDING", result.metadata?.state)
        }

    @Test
    fun `getBatch parses response correctly`() =
        runBlocking {
            val expectedResponse =
                """
                {
                  "name": "batches/123456",
                  "metadata": {
                      "state": "JOB_STATE_SUCCEEDED"
                  },
                  "done": true,
                  "response": {
                      "inlinedResponses": {
                          "inlinedResponses": []
                      }
                  }
                }
                """.trimIndent()

            val mockEngine =
                MockEngine { request ->
                    assertEquals(HttpMethod.Get, request.method)
                    assertEquals("https://generativelanguage.googleapis.com/v1beta/batches/123456", request.url.toString())
                    respond(
                        content = expectedResponse,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            val client =
                HttpClient(mockEngine) {
                    install(ContentNegotiation) {
                        json(json)
                    }
                }
            val batchClient = Batch("api-key", client)

            val result = batchClient.getBatch("batches/123456")

            assertEquals("batches/123456", result.name)
            assertEquals("JOB_STATE_SUCCEEDED", result.metadata?.state)
            assertEquals(emptyList<BatchInlineResponse>(), result.response?.inlinedResponses?.inlinedResponses)
        }
}
