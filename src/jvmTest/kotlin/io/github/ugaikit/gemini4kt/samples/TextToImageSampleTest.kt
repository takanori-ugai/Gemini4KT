package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.GeminiAI
import io.github.ugaikit.gemini4kt.TestHttpClientTracker
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Base64
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TextToImageSampleTest {
    private val clientTracker = TestHttpClientTracker()

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
        return GeminiAI(client = clientTracker.track(client), apiKey = "test-key")
    }

    @AfterEach
    fun closeClients() {
        clientTracker.closeAll()
    }

    @Test
    fun runSavesGeneratedImageToDisk() =
        runTest {
            val expectedBytes = "generated-image-bytes".toByteArray()
            val base64Image = Base64.getEncoder().encodeToString(expectedBytes)
            val outputPath = Files.createTempFile("gemini-image", ".png").toString()

            val ai =
                createGeminiAI { request ->
                    assertEquals(HttpMethod.Post, request.method)
                    assertTrue(request.url.toString().endsWith("/v1beta/interactions"))
                    assertEquals("test-key", request.headers["x-goog-api-key"])

                    val body = (request.body as TextContent).text
                    assertTrue(body.contains("\"model\":\"gemini-3.1-flash-lite-image\""))
                    assertTrue(body.contains("Create a picture of a nano banana dish in a fancy restaurant with a Gemini theme"))

                    respond(
                        """{"id":"v1_img","status":"completed","outputs":[{"type":"image","data":"$base64Image","mime_type":"image/png"}]}""",
                        HttpStatusCode.OK,
                        headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }

            TextToImageSample.run(
                client = ai,
                outputPath = outputPath,
            )

            assertContentEquals(expectedBytes, Files.readAllBytes(Paths.get(outputPath)))
        }
}
