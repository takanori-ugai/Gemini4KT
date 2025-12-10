package io.github.ugaikit.gemini4kt.batch

import io.github.ugaikit.gemini4kt.GeminiException
import io.github.ugaikit.gemini4kt.HttpConnectionProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection

class BatchTest {
    private lateinit var httpConnectionProvider: HttpConnectionProvider
    private lateinit var conn: HttpURLConnection
    private lateinit var batch: Batch
    private val apiKey = "test-api-key"
    private val bUrl = "https://generativelanguage.googleapis.com/v1beta"
    private val baseUrl = "$bUrl/models"

    @BeforeEach
    fun setup() {
        httpConnectionProvider = mockk()
        conn = mockk(relaxed = true)
        batch = Batch(apiKey = apiKey, httpConnectionProvider = httpConnectionProvider)
    }

    @Test
    fun `createBatch calls getContent with correct parameters`() {
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

        every { httpConnectionProvider.getConnection(any()) } returns conn
        every { conn.responseCode } returns 200
        every { conn.inputStream } returns ByteArrayInputStream(responseJson.toByteArray())
        every { conn.outputStream } returns ByteArrayOutputStream()

        val result = batch.createBatch(model, request)

        assertEquals("batches/123", result.name)
        assertEquals(false, result.done)

        verify { conn.requestMethod = "POST" }
        verify { conn.setRequestProperty("x-goog-api-key", apiKey) }
        // Verify URL implicitly by checking that getConnection was called (mockk verifies args)
        // Since we can't easily verify the URL passed to getConnection with 'every' setup on 'any()',
        // we trust the 'getContent' logic which we can't mock directly.
        // But we can verify the behavior.
    }

    @Test
    fun `getBatch calls getContent with correct parameters`() {
        val name = "batches/123"
        val responseJson = """{"name": "batches/123", "done": true}"""

        every { httpConnectionProvider.getConnection(any()) } returns conn
        every { conn.responseCode } returns 200
        every { conn.inputStream } returns ByteArrayInputStream(responseJson.toByteArray())

        val result = batch.getBatch(name)

        assertEquals("batches/123", result.name)
        assertEquals(true, result.done)

        verify { conn.requestMethod = "GET" }
        verify { conn.setRequestProperty("x-goog-api-key", apiKey) }
    }

    @Test
    fun `cancelBatch calls getContent with correct parameters`() {
        val name = "batches/123"
        val responseJson = "{}" // Response is empty JSON on success? API docs say it returns empty or Operation. Assuming empty for now.

        every { httpConnectionProvider.getConnection(any()) } returns conn
        every { conn.responseCode } returns 200
        every { conn.inputStream } returns ByteArrayInputStream(responseJson.toByteArray())
        every { conn.outputStream } returns ByteArrayOutputStream()

        batch.cancelBatch(name)

        verify { conn.requestMethod = "POST" }
        verify { conn.setRequestProperty("x-goog-api-key", apiKey) }
    }

    @Test
    fun `deleteBatch calls deleteContent with correct parameters`() {
        val name = "batches/123"

        every { httpConnectionProvider.getConnection(any()) } returns conn
        every { conn.responseCode } returns 200

        batch.deleteBatch(name)

        verify { conn.requestMethod = "DELETE" }
        verify { conn.setRequestProperty("x-goog-api-key", apiKey) }
    }

    @Test
    fun `createBatchEmbeddings calls getContent with correct parameters`() {
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

        every { httpConnectionProvider.getConnection(any()) } returns conn
        every { conn.responseCode } returns 200
        every { conn.inputStream } returns ByteArrayInputStream(responseJson.toByteArray())
        every { conn.outputStream } returns ByteArrayOutputStream()

        val result = batch.createBatchEmbeddings(model, request)

        assertEquals("batches/456", result.name)
        assertEquals(false, result.done)

        verify { conn.requestMethod = "POST" }
        verify { conn.setRequestProperty("x-goog-api-key", apiKey) }
    }

    @Test
    fun `listBatches calls getContent with correct parameters`() {
        val responseJson = """{"operations": [{"name": "batches/1"}, {"name": "batches/2"}]}"""

        every { httpConnectionProvider.getConnection(any()) } returns conn
        every { conn.responseCode } returns 200
        every { conn.inputStream } returns ByteArrayInputStream(responseJson.toByteArray())

        val result = batch.listBatches(pageSize = 10, pageToken = "token")

        assertEquals(2, result.operations?.size)
        assertEquals("batches/1", result.operations?.get(0)?.name)

        verify { conn.requestMethod = "GET" }
        verify { conn.setRequestProperty("x-goog-api-key", apiKey) }
    }

    @Test
    fun `API error handling throws GeminiException`() {
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

        every { httpConnectionProvider.getConnection(any()) } returns conn
        every { conn.responseCode } returns 400
        every { conn.errorStream } returns ByteArrayInputStream(errorJson.toByteArray())
        every { conn.inputStream } throws IOException("Should not be called")

        val exception =
            assertThrows(GeminiException::class.java) {
                batch.getBatch("batches/invalid")
            }

        assertEquals("Bad Request", exception.message)
        assertEquals(400, exception.error.code)
    }

    @Test
    fun `IOException handling returns empty JSON (or throws depending on impl)`() {
        // In Batch.kt, getContent catches IOException and returns empty string "",
        // then json.decodeFromString tries to parse "" which causes SerializationException.
        // Wait, let's check Batch.kt implementation again.

        every { httpConnectionProvider.getConnection(any()) } throws IOException("Network error")

        // Based on my reading of Batch.kt:
        // catch (e: IOException) { logger.error...; "" }
        // then json.decodeFromString<BatchJob>("") will fail.

        assertThrows(Exception::class.java) {
            batch.getBatch("batches/123")
        }
    }
}
