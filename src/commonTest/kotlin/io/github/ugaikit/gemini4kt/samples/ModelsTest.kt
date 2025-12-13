package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.Gemini
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class ModelsTest {
    @Test
    fun testListModels() = runTest {
        val mockResponse = """
            {
              "models": [
                {
                  "name": "models/gemini-pro",
                  "version": "001",
                  "displayName": "Gemini Pro",
                  "description": "The best model for scaling across a wide range of tasks",
                  "inputTokenLimit": 30720,
                  "outputTokenLimit": 2048,
                  "supportedGenerationMethods": ["generateContent", "countTokens"],
                  "temperature": 0.9,
                  "topP": 1.0,
                  "topK": 1
                }
              ]
            }
        """.trimIndent()

        val mockEngine = MockEngine { request ->
            respond(
                content = ByteReadChannel(mockResponse),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val httpClient = HttpClient(mockEngine)
        val gemini = Gemini("fake_api_key", client = httpClient)

        Models.listModels(gemini)
    }
}
