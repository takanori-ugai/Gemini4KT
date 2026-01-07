package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.GeminiAI
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test

class InteractionSamplesTest {
    private fun createGeminiAI(handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData): GeminiAI {
        val client =
            HttpClient(MockEngine) {
                engine {
                    addHandler(handler)
                }
                install(ContentNegotiation) {
                    json(
                        Json {
                            ignoreUnknownKeys = true
                            encodeDefaults = false
                            explicitNulls = false
                        },
                    )
                }
            }
        return GeminiAI(client = client, apiKey = "test-key")
    }

    @Test
    fun testRunSimple() =
        runTest {
            val responseJson =
                """{"id": "v1_1", "status": "completed", "outputs": [{"type":"text", "text":"Hello"}]}"""
            val ai =
                createGeminiAI {
                    respond(
                        responseJson,
                        HttpStatusCode.OK,
                        headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            InteractionSamples.runSimple(ai)
        }

    @Test
    fun testRunMultiTurn() =
        runTest {
            val responseJson = """{"id": "v1_2", "status": "completed", "outputs": [{"type":"text", "text":"Paris"}]}"""
            val ai =
                createGeminiAI {
                    respond(responseJson, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
                }
            InteractionSamples.runMultiTurn(ai)
        }

    @Test
    fun testRunImageInput() =
        runTest {
            val responseJson = """{"id": "v1_3", "status": "completed", "outputs": [{"type":"text", "text":"Robot"}]}"""
            val ai =
                createGeminiAI {
                    respond(responseJson, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
                }
            InteractionSamples.runImageInput(ai) { "base64image" }
        }

    @Test
    fun testRunFunctionCalling() =
        runTest {
            val responseJson =
                """{"id": "v1_4", "status": "requires_action", "outputs": [{"type":"function_call"}]}"""
            val ai =
                createGeminiAI {
                    respond(
                        responseJson,
                        HttpStatusCode.OK,
                        headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            InteractionSamples.runFunctionCalling(ai)
        }

    @Test
    fun testRunDeepResearch() =
        runTest {
            val responseJson =
                """{"id": "v1_5", "status": "completed", "outputs": [{"type":"text", "text":"Report"}]}"""
            val ai =
                createGeminiAI {
                    respond(
                        responseJson,
                        HttpStatusCode.OK,
                        headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            InteractionSamples.runDeepResearch(ai)
        }
}
