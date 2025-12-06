package io.github.ugaikit.gemini4kt.batch

import io.github.ugaikit.gemini4kt.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.net.HttpURLConnection
import java.net.URL

class BatchClientTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `createBatch sends correct request and parses response`() {
        val mockConnection = mockk<HttpURLConnection>(relaxed = true)
        val mockProvider = object : HttpConnectionProvider {
            override fun getConnection(url: URL): HttpURLConnection = mockConnection
        }

        val batchClient = Batch("api-key", mockProvider)

        val expectedResponse = """
            {
              "name": "batches/123456",
              "state": "JOB_STATE_PENDING",
              "create_time": "2024-01-01T00:00:00Z"
            }
        """.trimIndent()

        every { mockConnection.inputStream } returns ByteArrayInputStream(expectedResponse.toByteArray())
        every { mockConnection.responseCode } returns 200

        val generateContentRequest = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = "test"))))
        )
        val createBatchRequest = CreateBatchRequest(
            batch = BatchConfig(
                inputConfig = BatchInputConfig(
                    requests = BatchRequestInput(
                        requests = listOf(
                            BatchItemRequest(
                                request = json.encodeToJsonElement(generateContentRequest)
                            )
                        )
                    )
                )
            )
        )

        val result = batchClient.createBatch("gemini-pro", createBatchRequest)

        assertEquals("batches/123456", result.name)
        assertEquals("JOB_STATE_PENDING", result.state)

        verify { mockConnection.requestMethod = "POST" }
        verify { mockConnection.setRequestProperty("x-goog-api-key", "api-key") }
    }

    @Test
    fun `getBatch parses response correctly`() {
        val mockConnection = mockk<HttpURLConnection>(relaxed = true)
        val mockProvider = object : HttpConnectionProvider {
            override fun getConnection(url: URL): HttpURLConnection = mockConnection
        }
        val batchClient = Batch("api-key", mockProvider)

        val expectedResponse = """
            {
              "name": "batches/123456",
              "state": "JOB_STATE_SUCCEEDED",
              "dest": {
                  "inlined_responses": []
              }
            }
        """.trimIndent()

        every { mockConnection.inputStream } returns ByteArrayInputStream(expectedResponse.toByteArray())
        every { mockConnection.responseCode } returns 200

        val result = batchClient.getBatch("batches/123456")

        assertEquals("batches/123456", result.name)
        assertEquals("JOB_STATE_SUCCEEDED", result.state)
        assertEquals(emptyList<BatchInlineResponse>(), result.dest?.inlinedResponses)

        verify { mockConnection.requestMethod = "GET" }
    }
}
