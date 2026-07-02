package io.github.ugaikit.gemini4kt.batch

import io.github.ugaikit.gemini4kt.Content
import io.github.ugaikit.gemini4kt.GenerateContentRequest
import io.github.ugaikit.gemini4kt.Part
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Represents the batch client test.
 */
class BatchClientTest {
    /**
     * Holds the json.
     */
    private val json = Json { ignoreUnknownKeys = true }

    private val createdBatches = mutableListOf<Batch>()

    /**
     * Handles create batch.
     *
     * @param handler The handler.
     */
    private fun createBatch(handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData): Batch {
        val client =
            HttpClient(MockEngine) {
                engine {
                    addHandler(handler)
                }
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
            }
        return Batch(apiKey = "api-key", client = client).also { createdBatches.add(it) }
    }

    @AfterTest
    fun closeClients() {
        createdBatches.forEach(Batch::close)
        createdBatches.clear()
    }

    /**
     * Handles create batch sends correct request and parses response.
     */
    @Test
    fun createBatchSendsCorrectRequestAndParsesResponse() =
        runTest {
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

            val batchClient =
                createBatch { request ->
                    assertEquals(HttpMethod.Post, request.method)
                    respond(expectedResponse, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
                }

            val generateContentRequest =
                GenerateContentRequest(
                    contents = arrayOf(Content(parts = arrayOf(Part(text = "test")))),
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

    /**
     * Handles get batch parses response correctly.
     */
    @Test
    fun getBatchParsesResponseCorrectly() =
        runTest {
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

            val batchClient =
                createBatch { request ->
                    assertEquals(HttpMethod.Get, request.method)
                    respond(expectedResponse, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
                }

            val result = batchClient.getBatch("batches/123456")

            assertEquals("batches/123456", result.name)
            assertEquals("JOB_STATE_SUCCEEDED", result.metadata?.state)
            assertEquals(emptyList<BatchInlineResponse>(), result.response?.inlinedResponses?.inlinedResponses)
        }
}
