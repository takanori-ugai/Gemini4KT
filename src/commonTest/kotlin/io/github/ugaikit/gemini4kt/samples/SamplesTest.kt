package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.Gemini
import io.github.ugaikit.gemini4kt.batch.Batch
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class SamplesTest {
    private fun createMockGemini(responseText: String): Gemini {
        val mockEngine =
            MockEngine { _ ->
                respond(
                    content = ByteReadChannel(responseText),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }
        val client = HttpClient(mockEngine)
        return Gemini(apiKey = "test_key", client = client)
    }

    @Test
    fun testSamples1() =
        runTest {
            val gemini =
                createMockGemini(
                    """
                    {
                      "candidates": [
                        {
                          "content": {
                            "parts": [
                              {
                                "text": "A story about a magic backpack."
                              }
                            ]
                          }
                        }
                      ]
                    }
                    """.trimIndent(),
                )

            Samples1.run(gemini)
        }

    @Test
    fun testAudioGeneration() =
        runTest {
            val gemini =
                createMockGemini(
                    """
                    {
                      "candidates": [
                        {
                          "content": {
                            "parts": [
                              {
                                "inlineData": {
                                    "mimeType": "audio/wav",
                                    "data": "base64audio"
                                }
                              }
                            ]
                          }
                        }
                      ]
                    }
                    """.trimIndent(),
                )

            AudioGeneration.run(gemini)
        }

    @Test
    fun testCodeExecutionSample() =
        runTest {
            val gemini =
                createMockGemini(
                    """
                    {
                      "candidates": [
                        {
                          "content": {
                            "parts": [
                              {
                                "text": "Here is the code."
                              },
                              {
                                "executableCode": {
                                    "language": "PYTHON",
                                    "code": "print('hello')"
                                }
                              },
                              {
                                "codeExecutionResult": {
                                    "outcome": "OUTCOME_OK",
                                    "output": "hello"
                                }
                              }
                            ]
                          }
                        }
                      ]
                    }
                    """.trimIndent(),
                )

            CodeExecutionSample.run(gemini)
        }

    @Test
    fun testCountTokensSample() =
        runTest {
            val gemini =
                createMockGemini(
                    """
                    {
                      "totalTokens": 10
                    }
                    """.trimIndent(),
                )

            CountTokensSample.run(gemini)
        }

    @Test
    fun testEmbedContent() =
        runTest {
            val mockEngine =
                MockEngine { request ->
                    val content =
                        if (request.url.encodedPath.contains("batchEmbedContents")) {
                            """
                {
                  "embeddings": [
                    {
                      "values": [0.1, 0.2, 0.3]
                    }
                  ]
                }
                """
                        } else {
                            """
                {
                  "embedding": {
                    "values": [0.1, 0.2, 0.3]
                  }
                }
                """
                        }
                    respond(
                        content = ByteReadChannel(content.trimIndent()),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            val client = HttpClient(mockEngine)
            val gemini = Gemini(apiKey = "test_key", client = client)

            EmbedContent.run(gemini)
        }

    @Test
    fun testGoogleSearchSample() =
        runTest {
            val gemini =
                createMockGemini(
                    """
                    {
                      "candidates": [
                        {
                          "content": {
                            "parts": [
                              {
                                "text": "Spain won Euro 2024."
                              }
                            ]
                          },
                          "groundingMetadata": {
                            "searchEntryPoint": {
                                "renderedContent": "html"
                            }
                          }
                        }
                      ]
                    }
                    """.trimIndent(),
                )

            GoogleSearchSample.run(gemini)
        }

    @Test
    fun testBatchSample() =
        runTest {
            val mockEngine =
                MockEngine { request ->
                    val responseText =
                        if (request.url.encodedPath.contains("batches")) {
                            if (request.method.value == "POST") {
                                // Create batch response
                                """
                     {
                       "name": "batch/123",
                       "metadata": {
                         "state": "BATCH_STATE_SUCCEEDED"
                       },
                       "response": {
                            "inlinedResponses": {
                                "inlinedResponses": [
                                    {
                                        "response": {
                                            "candidates": []
                                        }
                                    }
                                ]
                            }
                       }
                     }
                     """
                            } else {
                                // List batches response
                                """
                     {
                       "operations": []
                     }
                     """
                            }
                        } else {
                            "{}"
                        }

                    respond(
                        content = ByteReadChannel(responseText.trimIndent()),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            val client = HttpClient(mockEngine)
            val batch = Batch(apiKey = "test_key", client = client)

            BatchSample.run(batch)
        }
}
