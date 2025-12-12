package io.github.ugaikit.gemini4kt.batch

import io.github.ugaikit.gemini4kt.Content
import io.github.ugaikit.gemini4kt.GenerateContentRequest
import io.github.ugaikit.gemini4kt.Part
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class CreateBatchRequestTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `test batch creation request with inline requests DSL`() {
        val request =
            createBatchRequest {
                batch {
                    inputConfig {
                        requests {
                            request {
                                // Helper to set request using GenerateContentRequest
                                val genRequest =
                                    GenerateContentRequest(
                                        contents = listOf(Content(parts = listOf(Part(text = "Hello")))),
                                    )
                                request(genRequest)
                                metadata {
                                    key = "req-1"
                                }
                            }
                        }
                    }
                }
            }

        assertEquals(
            1,
            request.batch.inputConfig.requests
                ?.requests
                ?.size,
        )
        // Verify metadata
        assertEquals(
            "req-1",
            request.batch.inputConfig.requests
                ?.requests
                ?.first()
                ?.metadata
                ?.key,
        )

        // Verify structure
        val jsonString = json.encodeToString(request)
        val deserialized = json.decodeFromString<CreateBatchRequest>(jsonString)
        assertEquals(
            "req-1",
            deserialized.batch.inputConfig.requests
                ?.requests
                ?.first()
                ?.metadata
                ?.key,
        )
    }
}
