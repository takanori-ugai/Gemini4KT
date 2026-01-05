package io.github.ugaikit.gemini4kt.batch

import io.github.ugaikit.gemini4kt.Content
import io.github.ugaikit.gemini4kt.GenerateContentRequest
import io.github.ugaikit.gemini4kt.Part
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Represents the create batch request test.
 */
class CreateBatchRequestTest {
    /**
     * Holds the json.
     */
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Tests test batch creation request with inline requests dsl.
     */
    @Test
    fun testBatchCreationRequestWithInlineRequestsDSL() {
        val request =
            createBatchRequest {
                batch {
                    inputConfig {
                        requests {
                            request {
                                // Helper to set request using GenerateContentRequest
                                val genRequest =
                                    GenerateContentRequest(
                                        contents = arrayOf(Content(parts = arrayOf(Part(text = "Hello")))),
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
