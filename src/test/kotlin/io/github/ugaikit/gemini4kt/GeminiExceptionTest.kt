package io.github.ugaikit.gemini4kt

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection

class GeminiExceptionTest {
    private lateinit var httpConnectionProvider: HttpConnectionProvider
    private lateinit var conn: HttpURLConnection
    private lateinit var gemini: Gemini
    private lateinit var fileUploadProvider: FileUploadProvider
    private val apiKey = "test-api-key"

    @BeforeEach
    fun setup() {
        httpConnectionProvider = mockk()
        conn = mockk(relaxed = true)
        fileUploadProvider = mockk()
        gemini =
            Gemini(
                apiKey = apiKey,
                httpConnectionProvider = httpConnectionProvider,
                fileUploadProvider = fileUploadProvider,
            )
    }

    @Test
    fun `getContent throws GeminiException on error`() {
        val errorJson = """{ "error": { "code": 429, "message": "Quota exceeded" } }"""
        every { httpConnectionProvider.getConnection(any()) } returns conn
        every { conn.responseCode } returns 429
        every { conn.errorStream } returns ByteArrayInputStream(errorJson.toByteArray())
        every { conn.outputStream } returns ByteArrayOutputStream()

        val exception = assertThrows(GeminiException::class.java) {
            gemini.getContent("http://localhost", "{}")
        }
        assertEquals(errorJson, exception.message)
    }

    @Test
    fun `deleteContent throws GeminiException on error`() {
        val errorJson = """{ "error": { "code": 404, "message": "Not Found" } }"""
        every { httpConnectionProvider.getConnection(any()) } returns conn
        every { conn.responseCode } returns 404
        every { conn.errorStream } returns ByteArrayInputStream(errorJson.toByteArray())

        val exception = assertThrows(GeminiException::class.java) {
            gemini.deleteContent("http://localhost")
        }
        assertEquals(errorJson, exception.message)
    }
}
