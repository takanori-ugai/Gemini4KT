package io.github.ugaikit.gemini4kt.batch

import io.github.ugaikit.gemini4kt.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class BatchTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `test batch creation request serialization`() {
        val generateContentRequest = GenerateContentRequest(
            contents = listOf(
                Content(
                    parts = listOf(Part(text = "Hello world"))
                )
            )
        )

        val batchItemRequest = BatchItemRequest(
            request = json.encodeToJsonElement(generateContentRequest),
            metadata = mapOf("key" to "request-1")
        )

        val batchRequestInput = BatchRequestInput(
            requests = listOf(batchItemRequest)
        )

        val batchConfig = BatchConfig(
            displayName = "test-batch",
            inputConfig = BatchInputConfig(
                requests = batchRequestInput
            )
        )

        val createBatchRequest = CreateBatchRequest(
            batch = batchConfig
        )

        val serialized = json.encodeToString(CreateBatchRequest.serializer(), createBatchRequest)
        println(serialized)

        assertNotNull(serialized)
        // Basic check to see if structure is present
        assert(serialized.contains("test-batch"))
        assert(serialized.contains("request-1"))
        assert(serialized.contains("Hello world"))
    }
}
