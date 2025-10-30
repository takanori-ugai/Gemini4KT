package io.github.ugaikit.gemini4kt

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection

class GeminiTest {
    private lateinit var httpConnectionProvider: HttpConnectionProvider
    private lateinit var conn: HttpURLConnection
    private lateinit var gemini: Gemini

    @BeforeEach
    fun setup() {
        httpConnectionProvider = mockk()
        conn = mockk(relaxed = true)
        gemini = Gemini(apiKey = "test-api-key", httpConnectionProvider = httpConnectionProvider)
    }

    @Test
    fun `getContent with inputJson returns content on success`() {
        val response = """{"key":"value"}"""
        every { httpConnectionProvider.getConnection(any()) } returns conn
        every { conn.responseCode } returns 200
        every { conn.inputStream } returns ByteArrayInputStream(response.toByteArray())
        every { conn.outputStream } returns ByteArrayOutputStream()

        val result = gemini.getContent("http://localhost", "{}")

        assertEquals(response, result)
        verify { conn.requestMethod = "POST" }
    }

    @Test
    fun `getContent without inputJson returns content on success`() {
        val response = """{"key":"value"}"""
        every { httpConnectionProvider.getConnection(any()) } returns conn
        every { conn.responseCode } returns 200
        every { conn.inputStream } returns ByteArrayInputStream(response.toByteArray())

        val result = gemini.getContent("http://localhost")

        assertEquals(response, result)
        verify { conn.requestMethod = "GET" }
    }

    @Test
    fun `getContent returns empty json on error`() {
        every { httpConnectionProvider.getConnection(any()) } returns conn
        every { conn.responseCode } returns 400
        every { conn.errorStream } returns ByteArrayInputStream("Error".toByteArray())

        val result = gemini.getContent("http://localhost")

        assertEquals("{}", result)
    }

    @Test
    fun `getContent returns empty string on IOException`() {
        every { httpConnectionProvider.getConnection(any()) } throws IOException()

        val result = gemini.getContent("http://localhost")

        assertEquals("", result)
    }

    @Test
    fun `deleteContent succeeds with 200 response`() {
        every { httpConnectionProvider.getConnection(any()) } returns conn
        every { conn.responseCode } returns 200

        gemini.deleteContent("http://localhost")

        verify { conn.requestMethod = "DELETE" }
    }

    @Test
    fun `deleteContent handles error response`() {
        every { httpConnectionProvider.getConnection(any()) } returns conn
        every { conn.responseCode } returns 400
        every { conn.errorStream } returns ByteArrayInputStream("Error".toByteArray())

        gemini.deleteContent("http://localhost")

        verify { conn.requestMethod = "DELETE" }
    }

    @Test
    fun `deleteContent handles IOException`() {
        every { httpConnectionProvider.getConnection(any()) } throws IOException()

        gemini.deleteContent("http://localhost")

        // No exception should be thrown
    }

    @Test
    fun `generateContent calls getContent with correct parameters`() {
        val geminiSpy = spyk(gemini)
        val request = GenerateContentRequest(contents = emptyList())
        val responseJson = """{"candidates": []}"""
        every { geminiSpy.getContent(any(), any()) } returns responseJson

        val response = geminiSpy.generateContent(request)

        assertNotNull(response)
        verify {
            geminiSpy.getContent(
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=test-api-key",
                Json.encodeToString(request),
            )
        }
    }

    @Test
    fun `createCachedContent calls getContent with correct parameters`() {
        val geminiSpy = spyk(gemini)
        val request = CachedContent(contents = emptyList())
        val responseJson = """{"name": "cachedContent-123"}"""
        every { geminiSpy.getContent(any(), any()) } returns responseJson

        val response = geminiSpy.createCachedContent(request)

        assertEquals("cachedContent-123", response.name)
        verify {
            geminiSpy.getContent(
                "https://generativelanguage.googleapis.com/v1beta/cachedContents?key=test-api-key",
                Json.encodeToString(request),
            )
        }
    }

    @Test
    fun `listCachedContent calls getContent with correct parameters`() {
        val geminiSpy = spyk(gemini)
        val responseJson = """{"cachedContents": []}"""
        every { geminiSpy.getContent(any(), isNull()) } returns responseJson

        val response = geminiSpy.listCachedContent()

        assertNotNull(response)
        verify {
            geminiSpy.getContent(
                "https://generativelanguage.googleapis.com/v1beta/cachedContents?pageSize=1000&key=test-api-key",
                null,
            )
        }
    }

    @Test
    fun `getCachedContent calls getContent with correct parameters`() {
        val geminiSpy = spyk(gemini)
        val name = "cachedContent-123"
        val responseJson = """{"name": "cachedContent-123"}"""
        every { geminiSpy.getContent(any(), any()) } returns responseJson

        val response = geminiSpy.getCachedContent(name)

        assertEquals(name, response.name)
        verify {
            geminiSpy.getContent(
                "https://generativelanguage.googleapis.com/v1beta/cachedContent-123?key=test-api-key",
                null,
            )
        }
    }

    @Test
    fun `deleteCachedContent calls deleteContent with correct parameters`() {
        val geminiSpy = spyk(gemini)
        val name = "cachedContent-123"
        every { geminiSpy.deleteContent(any()) } returns Unit

        geminiSpy.deleteCachedContent(name)

        verify {
            geminiSpy.deleteContent(
                "https://generativelanguage.googleapis.com/v1beta/cachedContent-123?key=test-api-key",
            )
        }
    }

    @Test
    fun `countTokens calls getContent with correct parameters`() {
        val geminiSpy = spyk(gemini)
        val request = CountTokensRequest(contents = emptyList())
        val responseJson = """{"totalTokens": 10}"""
        every { geminiSpy.getContent(any(), any()) } returns responseJson

        val response = geminiSpy.countTokens(request)

        assertEquals(10, response.totalTokens)
        verify {
            geminiSpy.getContent(
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-lite:countTokens?key=test-api-key",
                Json.encodeToString(request),
            )
        }
    }

    @Test
    fun `batchEmbedContents calls getContent with correct parameters`() {
        val geminiSpy = spyk(gemini)
        val request = BatchEmbedRequest(requests = emptyList())
        val responseJson = """{"embeddings": []}"""
        every { geminiSpy.getContent(any(), any()) } returns responseJson

        val response = geminiSpy.batchEmbedContents(request)

        assertNotNull(response)
        verify {
            geminiSpy.getContent(
                "https://generativelanguage.googleapis.com/v1beta/models/embedding-001:batchEmbedContents?key=test-api-key",
                Json.encodeToString(request),
            )
        }
    }

    @Test
    fun `embedContent calls getContent with correct parameters`() {
        val geminiSpy = spyk(gemini)
        val request = EmbedContentRequest(content = Content(parts = emptyList()), model = "models/embedding-001")
        val responseJson = """{"embedding": {"values": [1.0, 2.0, 3.0]}}"""
        every { geminiSpy.getContent(any(), any()) } returns responseJson

        val response = geminiSpy.embedContent(request)

        assertNotNull(response)
        verify {
            geminiSpy.getContent(
                "https://generativelanguage.googleapis.com/v1beta/models/embedding-001:embedContent?key=test-api-key",
                Json.encodeToString(request),
            )
        }
    }

    @Test
    fun `getModels calls getContent with correct parameters`() {
        val geminiSpy = spyk(gemini)
        val responseJson = """{"models": []}"""
        every { geminiSpy.getContent(any(), isNull()) } returns responseJson

        val response = geminiSpy.getModels()

        assertNotNull(response)
        verify {
            geminiSpy.getContent(
                "https://generativelanguage.googleapis.com/v1beta/models?key=test-api-key",
                null,
            )
        }
    }
}
