package io.github.ugaikit.gemini4kt.batch

import io.github.ugaikit.gemini4kt.Candidate
import io.github.ugaikit.gemini4kt.Content
import io.github.ugaikit.gemini4kt.GenerateContentResponse
import io.github.ugaikit.gemini4kt.Part
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BatchDataClassesTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `BatchInlineResponse serialization and deserialization`() {
        val response =
            GenerateContentResponse(
                candidates =
                    listOf(
                        Candidate(
                            content = Content(parts = listOf(Part(text = "Response Text"))),
                        ),
                    ),
            )
        val batchInlineResponse =
            BatchInlineResponse(
                response = json.encodeToJsonElement(response),
                metadata = ResponseMetadata(key = "request-1"),
            )

        val jsonString = json.encodeToString(batchInlineResponse)
        val deserialized = json.decodeFromString<BatchInlineResponse>(jsonString)

        assertEquals("request-1", deserialized.metadata?.key)

        val deserializedResponse = json.decodeFromJsonElement(GenerateContentResponse.serializer(), deserialized.response!!)
        assertEquals(
            "Response Text",
            deserializedResponse
                .candidates
                ?.first()
                ?.content
                ?.parts
                ?.first()
                ?.text,
        )
    }

    @Test
    fun `BatchInlineResponse with error deserialization`() {
        val jsonString =
            """
            {
                "error": {
                    "code": 400,
                    "message": "Invalid request"
                },
                "metadata": {
                    "key": "request-2"
                }
            }
            """.trimIndent()

        val deserialized = json.decodeFromString<BatchInlineResponse>(jsonString)

        assertEquals("request-2", deserialized.metadata?.key)
        assertNotNull(deserialized.error)
        assertTrue(deserialized.error.toString().contains("Invalid request"))
    }

    @Test
    fun `BatchJob serialization and deserialization`() {
        val batchJob =
            BatchJob(
                name = "batches/123",
                metadata =
                    BatchJobMetadata(
                        state = "SUCCEEDED",
                        batchStats =
                            BatchStats(
                                requestCount = 10,
                                successfulRequestCount = 9,
                            ),
                        createTime = "2023-10-26T12:00:00Z",
                    ),
                done = true,
                response =
                    BatchJobResponse(
                        inlinedResponses =
                            InlinedResponsesWrapper(
                                inlinedResponses = emptyList(),
                            ),
                    ),
            )

        val jsonString = json.encodeToString(batchJob)
        val deserialized = json.decodeFromString<BatchJob>(jsonString)

        assertEquals("batches/123", deserialized.name)
        assertEquals("SUCCEEDED", deserialized.metadata?.state)
        assertEquals(10, deserialized.metadata?.batchStats?.requestCount)
        assertEquals(true, deserialized.done)
    }

    @Test
    fun `BatchJob with error deserialization`() {
        val jsonString =
            """
            {
                "name": "batches/456",
                "done": true,
                "error": {
                    "code": 500,
                    "message": "Internal error",
                    "status": "INTERNAL"
                }
            }
            """.trimIndent()

        val deserialized = json.decodeFromString<BatchJob>(jsonString)

        assertEquals("batches/456", deserialized.name)
        assertEquals(true, deserialized.done)
        assertEquals(500, deserialized.error?.code)
        assertEquals("Internal error", deserialized.error?.message)
        assertEquals("INTERNAL", deserialized.error?.status)
    }

    @Test
    fun `ListBatchesResponse serialization and deserialization`() {
        val listBatchesResponse =
            ListBatchesResponse(
                operations =
                    listOf(
                        BatchJob(name = "batches/1"),
                        BatchJob(name = "batches/2"),
                    ),
                nextPageToken = "token-123",
            )

        val jsonString = json.encodeToString(listBatchesResponse)
        val deserialized = json.decodeFromString<ListBatchesResponse>(jsonString)

        assertEquals(2, deserialized.operations?.size)
        assertEquals("batches/1", deserialized.operations?.get(0)?.name)
        assertEquals("token-123", deserialized.nextPageToken)
    }
}
