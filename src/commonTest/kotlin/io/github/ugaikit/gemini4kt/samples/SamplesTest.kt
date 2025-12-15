package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.Gemini
import io.github.ugaikit.gemini4kt.batch.Batch
import io.github.ugaikit.gemini4kt.filesearch.FileSearch
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

    private fun createMockFileSearch(responseText: String): FileSearch {
        val mockEngine =
            MockEngine { _ ->
                respond(
                    content = ByteReadChannel(responseText),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }
        val client = HttpClient(mockEngine)
        return FileSearch(apiKey = "test_key", client = client)
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

    @Test
    fun testFunctionExample1() =
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
                                "text": "The Barbie movie is showing at AMC."
                              }
                            ]
                          }
                        }
                      ]
                    }
                    """.trimIndent(),
                )

            FunctionExample1.run(gemini)
        }

    @Test
    fun testFunctionExample2() =
        runTest {
            var callCount = 0
            val mockEngine =
                MockEngine { _ ->
                    callCount++
                    val responseText =
                        if (callCount == 1) {
                            """
                {
                  "candidates": [
                    {
                      "content": {
                        "parts": [
                          {
                            "functionCall": {
                                "name": "find_weather",
                                "args": {
                                    "location": "Boston, MA"
                                }
                            }
                          }
                        ]
                      }
                    }
                  ]
                }
                """
                        } else {
                            """
                {
                  "candidates": [
                    {
                      "content": {
                        "parts": [
                          {
                            "text": "The weather in Boston is sunny."
                          }
                        ]
                      }
                    }
                  ]
                }
                """
                        }
                    respond(
                        content = ByteReadChannel(responseText.trimIndent()),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }

            val client = HttpClient(mockEngine)
            val gemini = Gemini(apiKey = "test_key", client = client)

            FunctionExample2.run(gemini)
        }

    @Test
    fun testFunctionExample3() =
        runTest {
            if (!supportsReflection) return@runTest

            var callCount = 0
            val mockEngine =
                MockEngine { _ ->
                    callCount++
                    val responseText =
                        if (callCount == 1) {
                            """
                {
                  "candidates": [
                    {
                      "content": {
                        "parts": [
                          {
                            "functionCall": {
                                "name": "add",
                                "args": {
                                    "a": 123,
                                    "b": 456
                                }
                            }
                          }
                        ]
                      }
                    }
                  ]
                }
                """
                        } else {
                            """
                {
                  "candidates": [
                    {
                      "content": {
                        "parts": [
                          {
                            "text": "The sum is 579."
                          }
                        ]
                      }
                    }
                  ]
                }
                """
                        }
                    respond(
                        content = ByteReadChannel(responseText.trimIndent()),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }

            val client = HttpClient(mockEngine)
            val gemini = Gemini(apiKey = "test_key", client = client)

            FunctionExample3.run(gemini)
        }

    @Test
    fun testStreamGenerateContentSample() =
        runTest {
            val mockEngine =
                MockEngine { _ ->
                    respond(
                        content =
                            ByteReadChannel(
                                """
                                data: {"candidates": [{"content": {"parts": [{"text": "Part 1"}]}}]}

                                data: {"candidates": [{"content": {"parts": [{"text": "Part 2"}]}}]}

                                """.trimIndent(),
                            ),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "text/event-stream"),
                    )
                }
            val client = HttpClient(mockEngine)
            val gemini = Gemini(apiKey = "test_key", client = client)

            StreamGenerateContentSample.run(gemini)
        }

    @Test
    fun testUrlContextSample() =
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
                                "text": "URL content extracted."
                              }
                            ]
                          },
                          "urlContextMetadata": {
                            "urlMetadata": [
                                {
                                    "retrievedUrl": "https://www.google.com",
                                    "urlRetrievalStatus": "OK"
                                }
                            ]
                          }
                        }
                      ]
                    }
                    """.trimIndent(),
                )

            UrlContextSample.run(gemini)
        }

    @Test
    fun testInputWithImage() =
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
                                "text": "This is a picture of something."
                              }
                            ]
                          }
                        }
                      ]
                    }
                    """.trimIndent(),
                )

            InputWithImage.run(args = emptyArray(), gemini = gemini, imageProvider = { "base64image" })
        }

    @Test
    fun testCache() =
        runTest {
            val mockEngine =
                MockEngine { request ->
                    val responseText =
                        if (request.url.encodedPath.contains("cachedContents")) {
                            if (request.method.value == "POST") {
                                // Create
                                """
                    {
                      "name": "cachedContents/123",
                      "model": "models/gemini-2.5-flash-lite",
                      "createTime": "2024-01-01T00:00:00Z",
                      "updateTime": "2024-01-01T00:00:00Z",
                      "expireTime": "2024-01-02T00:00:00Z"
                    }
                    """
                            } else if (request.method.value == "GET") {
                                if (request.url.encodedPath.endsWith("cachedContents")) {
                                    // List
                                    """
                         {
                           "cachedContents": []
                         }
                         """
                                } else {
                                    // Get
                                    """
                        {
                          "name": "cachedContents/123",
                          "model": "models/gemini-2.5-flash-lite"
                        }
                        """
                                }
                            } else if (request.method.value == "DELETE") {
                                "{}"
                            } else {
                                "{}"
                            }
                        } else if (request.url.encodedPath.contains("generateContent")) {
                            """
                {
                  "candidates": [
                    {
                      "content": {
                        "parts": [
                          {
                            "text": "Summary of cached content."
                          }
                        ]
                      }
                    }
                  ]
                }
                """
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
            val gemini = Gemini(apiKey = "test_key", client = client)

            Cache.run(gemini)
        }
}
