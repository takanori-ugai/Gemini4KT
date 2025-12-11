package io.github.ugaikit.gemini4kt.batch

import io.github.ugaikit.gemini4kt.GeminiException
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondOK
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.io.IOException

class BatchTest {
    private lateinit var batch: Batch
    private val apiKey = "test-api-key"
    private val bUrl = "https://generativelanguage.googleapis.com/v1beta"
    private val baseUrl = "$bUrl/models"

    private fun createBatch(handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponse): Batch {
        val client =
            HttpClient(MockEngine) {
                engine {
                    addHandler(handler)
                }
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
            }
        return Batch(apiKey = apiKey, client = client)
    }

    @Test
    fun `createBatch calls getContent with correct parameters`() =
        runTest {
            val model = "gemini-pro"
            val request =
                CreateBatchRequest(
                    batch =
                        BatchConfig(
                            inputConfig =
                                BatchInputConfig(
                                    gcsSource = BatchGcsSource(uris = listOf("gs://bucket/file")),
                                ),
                        ),
                )
            val responseJson = """{"name": "batches/123", "done": false}"""

            batch =
                createBatch { request ->
                    assertEquals(HttpMethod.Post, request.method)
                    assertEquals("$baseUrl/$model:batchGenerateContent", request.url.toString())
                    respond(responseJson, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
                }

            val result = batch.createBatch(model, request)

            assertEquals("batches/123", result.name)
            assertEquals(false, result.done)
        }

    @Test
    fun `getBatch calls getContent with correct parameters`() =
        runTest {
            val name = "batches/123"
            val responseJson = """{"name": "batches/123", "done": true}"""

            batch =
                createBatch { request ->
                    assertEquals(HttpMethod.Get, request.method)
                    assertEquals("$bUrl/$name", request.url.toString())
                    respond(responseJson, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
                }

            val result = batch.getBatch(name)

            assertEquals("batches/123", result.name)
            assertEquals(true, result.done)
        }

    @Test
    fun `cancelBatch calls getContent with correct parameters`() =
        runTest {
            val name = "batches/123"
            val responseJson = "{}"

            batch =
                createBatch { request ->
                    assertEquals(HttpMethod.Post, request.method)
                    assertEquals("$bUrl/$name:cancel", request.url.toString())
                    respond(responseJson, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
                }

            batch.cancelBatch(name)
        }

    @Test
    fun `deleteBatch calls deleteContent with correct parameters`() =
        runTest {
            val name = "batches/123"

            batch =
                createBatch { request ->
                    assertEquals(HttpMethod.Delete, request.method)
                    assertEquals("$bUrl/$name", request.url.toString())
                    respondOK()
                }

            batch.deleteBatch(name)
        }

    @Test
    fun `createBatchEmbeddings calls getContent with correct parameters`() =
        runTest {
            val model = "embedding-001"
            val request =
                CreateBatchRequest(
                    batch =
                        BatchConfig(
                            inputConfig =
                                BatchInputConfig(
                                    gcsSource = BatchGcsSource(uris = listOf("gs://bucket/file")),
                                ),
                        ),
                )
            val responseJson = """{"name": "batches/456", "done": false}"""

            batch =
                createBatch { request ->
                    assertEquals(HttpMethod.Post, request.method)
                    assertEquals("$baseUrl/$model:asyncBatchEmbedContent", request.url.toString())
                    respond(responseJson, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
                }

            val result = batch.createBatchEmbeddings(model, request)

            assertEquals("batches/456", result.name)
            assertEquals(false, result.done)
        }

    @Test
    fun `listBatches calls getContent with correct parameters`() =
        runTest {
            val responseJson = """{"operations": [{"name": "batches/1"}, {"name": "batches/2"}]}"""

            batch =
                createBatch { request ->
                    assertEquals(HttpMethod.Get, request.method)
                    assert(request.url.toString().startsWith("$bUrl/batches"))
                    respond(responseJson, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
                }

            val result = batch.listBatches(pageSize = 10, pageToken = "token")

            assertEquals(2, result.operations?.size)
            assertEquals("batches/1", result.operations?.get(0)?.name)
        }

    @Test
    fun `API error handling throws GeminiException`() =
        runTest {
            val errorJson =
                """
                {
                    "error": {
                        "code": 400,
                        "message": "Bad Request",
                        "status": "INVALID_ARGUMENT"
                    }
                }
                """.trimIndent()

            batch =
                createBatch {
                    respond(
                        content = errorJson,
                        status = HttpStatusCode.BadRequest,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }

            val exception =
                assertThrows(GeminiException::class.java) {
                    runTest {
                        batch.getBatch("batches/invalid")
                    }
                }

            assertEquals("Bad Request", exception.message)
            assertEquals(400, exception.error.code)
        }

    @Test
    fun `IOException handling returns empty JSON (or throws depending on impl)`() =
        runTest {
            batch = createBatch { throw IOException("Network error") }

            // Same as original test, expects exception because "" is invalid JSON for BatchJob
            assertThrows(Exception::class.java) {
                runTest {
                    batch.getBatch("batches/123")
                }
            }
        }
}
