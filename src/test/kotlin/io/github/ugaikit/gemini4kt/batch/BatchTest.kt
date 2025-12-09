package io.github.ugaikit.gemini4kt.batch

import io.github.ugaikit.gemini4kt.Content
import io.github.ugaikit.gemini4kt.GenerateContentRequest
import io.github.ugaikit.gemini4kt.Part
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertNotNull

class BatchTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `test batch creation request serialization`() {
        val generateContentRequest =
            GenerateContentRequest(
                contents =
                    listOf(
                        Content(
                            parts = listOf(Part(text = "Hello world")),
                        ),
                    ),
            )

        val createBatchRequest =
            createBatchRequest {
                batch {
                    displayName = "test-batch"
                    inputConfig {
                        requests {
                            request {
                                request(generateContentRequest)
                                metadata {
                                    key = "request-1"
                                }
                            }
                        }
                    }
                }
            }

        val serialized = json.encodeToString(CreateBatchRequest.serializer(), createBatchRequest)
        println(serialized)

        assertNotNull(serialized)
        // Basic check to see if structure is present
        assert(serialized.contains("test-batch"))
        assert(serialized.contains("request-1"))
        assert(serialized.contains("Hello world"))
    }
}
