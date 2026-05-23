package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.Gemini
import io.github.ugaikit.gemini4kt.TestUtils.captureStdout
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Validates FunctionExample4 direct function-binding sample.
 */
class FunctionExample4Test {
    @Test
    fun runUsesAutomaticFunctionCallingWithDirectBindings() =
        runTest {
            var callCount = 0
            val mockEngine =
                MockEngine { _ ->
                    callCount++
                    val responseText =
                        when (callCount) {
                            1 ->
                                """
                                {
                                  "candidates": [
                                    {
                                      "content": {
                                        "parts": [
                                          {
                                            "functionCall": {
                                              "name": "powerDiscoBall",
                                              "args": { "power": true }
                                            }
                                          }
                                        ]
                                      }
                                    }
                                  ]
                                }
                                """
                            2 ->
                                """
                                {
                                  "candidates": [
                                    {
                                      "content": {
                                        "parts": [
                                          {
                                            "functionCall": {
                                              "name": "startMusic",
                                              "args": { "energetic": true, "loud": true }
                                            }
                                          }
                                        ]
                                      }
                                    }
                                  ]
                                }
                                """
                            3 ->
                                """
                                {
                                  "candidates": [
                                    {
                                      "content": {
                                        "parts": [
                                          {
                                            "functionCall": {
                                              "name": "dimLights",
                                              "args": { "brightness": 0.5 }
                                            }
                                          }
                                        ]
                                      }
                                    }
                                  ]
                                }
                                """
                            else ->
                                """
                                {
                                  "candidates": [
                                    {
                                      "content": {
                                        "parts": [
                                          {
                                            "text": "I've turned on the disco ball, started loud energetic music, and dimmed the lights to 50%."
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

            val gemini = Gemini(apiKey = "test_key", client = HttpClient(mockEngine))
            val output = captureStdout { FunctionExample4.run(gemini) }

            assertEquals(4, callCount)
            assertTrue(output.contains("Final response:"))
            assertTrue(output.contains("disco ball"))
        }
}
