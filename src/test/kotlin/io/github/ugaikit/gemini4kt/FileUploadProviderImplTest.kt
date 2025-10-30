package io.github.ugaikit.gemini4kt

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.HttpURLConnection

class FileUploadProviderImplTest {

    private lateinit var fileUploadProvider: FileUploadProviderImpl
    private lateinit var conn: HttpURLConnection
    private lateinit var httpConnectionProvider: HttpConnectionProvider

    @BeforeEach
    fun setup() {
        conn = mockk(relaxed = true)
        httpConnectionProvider = mockk()
        every { httpConnectionProvider.getConnection(any()) } returns conn
        fileUploadProvider = FileUploadProviderImpl("test-api-key", httpConnectionProvider)
    }

    @Test
    fun `upload returns file on success`() {
        runBlocking {
            val file = File.createTempFile("test", ".txt")
            file.writeText("test content")
            val mimeType = "text/plain"
            val displayName = "Test File"
            val response = """
                {
                    "file": {
                        "name": "files/test-file",
                        "displayName": "Test File",
                        "uri": "https://example.com/test-file",
                        "mimeType": "text/plain",
                        "createTime": "2024-01-01T00:00:00Z",
                        "updateTime": "2024-01-01T00:00:00Z",
                        "expirationTime": "2024-01-02T00:00:00Z",
                        "sha256Hash": "hash",
                        "sizeBytes": 12
                    }
                }
            """.trimIndent()
            val uploadUrl = "https://example.com/upload"

            every { conn.responseCode } returns 200
            every { conn.headerFields } returns mapOf("x-goog-upload-url" to listOf(uploadUrl))
            every { conn.outputStream } returns ByteArrayOutputStream()
            every { conn.inputStream } returns ByteArrayInputStream(response.toByteArray())

            val result = fileUploadProvider.upload(file, mimeType, displayName)

            assertEquals("files/test-file", result.name)
            file.delete()
        }
    }
}
