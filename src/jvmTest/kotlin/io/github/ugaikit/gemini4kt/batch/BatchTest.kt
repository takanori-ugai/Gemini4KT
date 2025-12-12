package io.github.ugaikit.gemini4kt.batch

import io.github.ugaikit.gemini4kt.GeminiException
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class BatchTest {
    private val apiKey = "test-api-key"
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `createBatch calls getContent with correct parameters`() =
        runBlocking {
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

            val mockEngine =
                MockEngine { requestData ->
                    assertEquals("https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:batchGenerateContent", requestData.url.toString())
                    assertEquals(HttpMethod.Post, requestData.method)
                    respond(
                        content = responseJson,
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
            val batch = Batch(apiKey, client)

            val result = batch.createBatch(model, request)

            assertEquals("batches/123", result.name)
            assertEquals(false, result.done)
        }

    @Test
    fun `getBatch calls getContent with correct parameters`() =
        runBlocking {
            val name = "batches/123"
            val responseJson = """{"name": "batches/123", "done": true}"""

            val mockEngine =
                MockEngine { requestData ->
                    assertEquals("https://generativelanguage.googleapis.com/v1beta/batches/123", requestData.url.toString())
                    assertEquals(HttpMethod.Get, requestData.method)
                    respond(
                        content = responseJson,
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
            val batch = Batch(apiKey, client)

            val result = batch.getBatch(name)

            assertEquals("batches/123", result.name)
            assertEquals(true, result.done)
        }

    @Test
    fun `cancelBatch calls getContent with correct parameters`() =
        runBlocking {
            val name = "batches/123"
            val responseJson = "{}"

            val mockEngine =
                MockEngine { requestData ->
                    assertEquals("https://generativelanguage.googleapis.com/v1beta/batches/123:cancel", requestData.url.toString())
                    assertEquals(HttpMethod.Post, requestData.method)
                    respond(
                        content = responseJson,
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
            val batch = Batch(apiKey, client)

            batch.cancelBatch(name)
        }

    @Test
    fun `deleteBatch calls deleteContent with correct parameters`() =
        runBlocking {
            val name = "batches/123"

            val mockEngine =
                MockEngine { requestData ->
                    assertEquals("https://generativelanguage.googleapis.com/v1beta/batches/123", requestData.url.toString())
                    assertEquals(HttpMethod.Delete, requestData.method)
                    respond(
                        content = "",
                        status = HttpStatusCode.OK,
                    )
                }
            val client = HttpClient(mockEngine)
            val batch = Batch(apiKey, client)

            batch.deleteBatch(name)
        }

    @Test
    fun `createBatchEmbeddings calls getContent with correct parameters`() =
        runBlocking {
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

            val mockEngine =
                MockEngine { requestData ->
                    assertEquals("https://generativelanguage.googleapis.com/v1beta/models/embedding-001:asyncBatchEmbedContent", requestData.url.toString())
                    assertEquals(HttpMethod.Post, requestData.method)
                    respond(
                        content = responseJson,
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
            val batch = Batch(apiKey, client)

            val result = batch.createBatchEmbeddings(model, request)

            assertEquals("batches/456", result.name)
            assertEquals(false, result.done)
        }

    @Test
    fun `listBatches calls getContent with correct parameters`() =
        runBlocking {
            val responseJson = """{"operations": [{"name": "batches/1"}, {"name": "batches/2"}]}"""

            val mockEngine =
                MockEngine { requestData ->
                    assertEquals("https://generativelanguage.googleapis.com/v1beta/batches?pageSize=10&pageToken=token", requestData.url.toString())
                    assertEquals(HttpMethod.Get, requestData.method)
                    respond(
                        content = responseJson,
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
            val batch = Batch(apiKey, client)

            val result = batch.listBatches(pageSize = 10, pageToken = "token")

            assertEquals(2, result.operations?.size)
            assertEquals("batches/1", result.operations?.get(0)?.name)
        }

    @Test
    fun `API error handling throws GeminiException`() =
        runBlocking {
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

            val mockEngine =
                MockEngine {
                    respond(
                        content = errorJson,
                        status = HttpStatusCode.BadRequest,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            val client =
                HttpClient(mockEngine) {
                    install(ContentNegotiation) {
                        json(json)
                    }
                }
            val batch = Batch(apiKey, client)

            val exception =
                assertThrows(GeminiException::class.java) {
                    runBlocking { batch.getBatch("batches/invalid") }
                }

            assertEquals("Bad Request", exception.message)
            assertEquals(400, exception.error.code)
        }
}
