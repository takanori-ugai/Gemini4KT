package io.github.ugaikit.gemini4kt

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class GeminiTest {
    private val apiKey = "test-api-key"
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `getContent without inputJson returns content on success`() =
        runBlocking {
            val response = """{"key":"value"}"""
            val mockEngine =
                MockEngine { request ->
                    respond(
                        content = response,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            val client =
                HttpClient(mockEngine) {
                    install(ContentNegotiation) {
                        json(json)
                    }
                }
            val gemini = Gemini(apiKey, client)

            val result = gemini.getContent("http://localhost")

            assertEquals(response, result)
        }

    @Test
    fun `getContent returns empty json on error`() =
        runBlocking {
            val mockEngine =
                MockEngine { request ->
                    respond(
                        content = "Error",
                        status = HttpStatusCode.BadRequest,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            val client =
                HttpClient(mockEngine) {
                    install(ContentNegotiation) {
                        json(json)
                    }
                }
            val gemini = Gemini(apiKey, client)

            val result = gemini.getContent("http://localhost")

            assertEquals("{}", result)
        }

    @Test
    fun `deleteContent succeeds with 200 response`() =
        runBlocking {
            val mockEngine =
                MockEngine { request ->
                    respond(
                        content = "",
                        status = HttpStatusCode.OK,
                    )
                }
            val client = HttpClient(mockEngine)
            val gemini = Gemini(apiKey, client)

            gemini.deleteContent("http://localhost")
        }

    @Test
    fun `deleteContent handles error response`() =
        runBlocking {
            val mockEngine =
                MockEngine { request ->
                    respond(
                        content = "Error",
                        status = HttpStatusCode.BadRequest,
                    )
                }
            val client = HttpClient(mockEngine)
            val gemini = Gemini(apiKey, client)

            gemini.deleteContent("http://localhost")
        }

    @Test
    fun `generateContent calls correct endpoint and returns response`() =
        runBlocking {
            val request = GenerateContentRequest(contents = emptyList())
            val responseJson = """{"candidates": []}"""

            val mockEngine =
                MockEngine { requestData ->
                    assertEquals("https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent", requestData.url.toString())
                    assertEquals(HttpMethod.Post, requestData.method)
                    assertEquals("application/json", requestData.body.contentType.toString())

                    respond(
                        content = responseJson,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            val client =
                HttpClient(mockEngine) {
                    install(ContentNegotiation) {
                        json(json)
                    }
                }
            val gemini = Gemini(apiKey, client)

            val response = gemini.generateContent(request)

            assertNotNull(response)
        }

    @Test
    fun `createCachedContent calls correct endpoint`() =
        runBlocking {
            val request = CachedContent(contents = emptyList())
            val responseJson = """{"name": "cachedContent-123", "model": "gemini-pro"}"""

            val mockEngine =
                MockEngine { requestData ->
                    assertEquals("https://generativelanguage.googleapis.com/v1beta/cachedContents", requestData.url.toString())
                    respond(
                        content = responseJson,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            val client =
                HttpClient(mockEngine) {
                    install(ContentNegotiation) {
                        json(json)
                    }
                }
            val gemini = Gemini(apiKey, client)

            val response = gemini.createCachedContent(request)

            assertEquals("cachedContent-123", response.name)
        }

    @Test
    fun `listCachedContent calls correct endpoint`() =
        runBlocking {
            val responseJson = """{"cachedContents": []}"""

            val mockEngine =
                MockEngine { requestData ->
                    assertEquals("https://generativelanguage.googleapis.com/v1beta/cachedContents?pageSize=1000", requestData.url.toString())
                    respond(
                        content = responseJson,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            val client =
                HttpClient(mockEngine) {
                    install(ContentNegotiation) {
                        json(json)
                    }
                }
            val gemini = Gemini(apiKey, client)

            val response = gemini.listCachedContent()

            assertNotNull(response)
        }

    @Test
    fun `getCachedContent calls correct endpoint`() =
        runBlocking {
            val name = "cachedContent-123"
            val responseJson = """{"name": "cachedContent-123", "model": "gemini-pro"}"""

            val mockEngine =
                MockEngine { requestData ->
                    assertEquals("https://generativelanguage.googleapis.com/v1beta/$name", requestData.url.toString())
                    respond(
                        content = responseJson,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            val client =
                HttpClient(mockEngine) {
                    install(ContentNegotiation) {
                        json(json)
                    }
                }
            val gemini = Gemini(apiKey, client)

            val response = gemini.getCachedContent(name)

            assertEquals(name, response.name)
        }

    @Test
    fun `deleteCachedContent calls correct endpoint`() =
        runBlocking {
            val name = "cachedContent-123"

            val mockEngine =
                MockEngine { requestData ->
                    assertEquals("https://generativelanguage.googleapis.com/v1beta/$name", requestData.url.toString())
                    assertEquals(HttpMethod.Delete, requestData.method)
                    respond(
                        content = "",
                        status = HttpStatusCode.OK,
                    )
                }
            val client = HttpClient(mockEngine)
            val gemini = Gemini(apiKey, client)

            gemini.deleteCachedContent(name)
        }

    @Test
    fun `countTokens calls correct endpoint`() =
        runBlocking {
            val request = CountTokensRequest(contents = emptyList())
            val responseJson = """{"totalTokens": 10}"""

            val mockEngine =
                MockEngine { requestData ->
                    assertEquals("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-lite:countTokens", requestData.url.toString())
                    respond(
                        content = responseJson,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            val client =
                HttpClient(mockEngine) {
                    install(ContentNegotiation) {
                        json(json)
                    }
                }
            val gemini = Gemini(apiKey, client)

            val response = gemini.countTokens(request)

            assertEquals(10, response.totalTokens)
        }

    @Test
    fun `batchEmbedContents calls correct endpoint`() =
        runBlocking {
            val request = BatchEmbedRequest(requests = emptyList())
            val responseJson = """{"embeddings": []}"""

            val mockEngine =
                MockEngine { requestData ->
                    assertEquals("https://generativelanguage.googleapis.com/v1beta/models/embedding-001:batchEmbedContents", requestData.url.toString())
                    respond(
                        content = responseJson,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            val client =
                HttpClient(mockEngine) {
                    install(ContentNegotiation) {
                        json(json)
                    }
                }
            val gemini = Gemini(apiKey, client)

            val response = gemini.batchEmbedContents(request)

            assertNotNull(response)
        }

    @Test
    fun `embedContent calls correct endpoint`() =
        runBlocking {
            val request = EmbedContentRequest(content = Content(parts = emptyList()), model = "models/embedding-001")
            val responseJson = """{"embedding": {"values": [1.0, 2.0, 3.0]}}"""

            val mockEngine =
                MockEngine { requestData ->
                    assertEquals("https://generativelanguage.googleapis.com/v1beta/models/embedding-001:embedContent", requestData.url.toString())
                    respond(
                        content = responseJson,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            val client =
                HttpClient(mockEngine) {
                    install(ContentNegotiation) {
                        json(json)
                    }
                }
            val gemini = Gemini(apiKey, client)

            val response = gemini.embedContent(request)

            assertNotNull(response)
        }

    @Test
    fun `getModels calls correct endpoint`() =
        runBlocking {
            val responseJson = """{"models": []}"""

            val mockEngine =
                MockEngine { requestData ->
                    assertEquals("https://generativelanguage.googleapis.com/v1beta/models", requestData.url.toString())
                    respond(
                        content = responseJson,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            val client =
                HttpClient(mockEngine) {
                    install(ContentNegotiation) {
                        json(json)
                    }
                }
            val gemini = Gemini(apiKey, client)

            val response = gemini.getModels()

            assertNotNull(response)
        }
}
