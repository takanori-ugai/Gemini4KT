package io.github.ugaikit.gemini4kt

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

class FileUploadProviderImplTest {
    private lateinit var fileUploadProvider: FileUploadProviderImpl

    private funcreateFileUploadProvider(handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponse): FileUploadProviderImpl {
        val client = HttpClient(MockEngine) {
            engine {
                addHandler(handler)
            }
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        return FileUploadProviderImpl(apiKey = "test-api-key", client = client)
    }

    @Test
    fun `upload returns file on success`() = runTest {
        val file = File.createTempFile("test", ".txt")
        file.writeText("test content")
        val mimeType = "text/plain"
        val displayName = "Test File"
        val response =
            """
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

        fileUploadProvider = createFileUploadProvider { request ->
            if (request.url.toString().contains("upload/v1beta/files")) {
                // Initial request to get upload URL
                assertEquals(HttpMethod.Post, request.method)
                respond(
                    content = "",
                    status = HttpStatusCode.OK,
                    headers = headersOf("X-Goog-Upload-URL", uploadUrl)
                )
            } else if (request.url.toString() == uploadUrl) {
                // Actual upload request
                assertEquals(HttpMethod.Post, request.method)
                respond(
                    content = response,
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            } else {
                error("Unexpected request: ${request.url}")
            }
        }

        val result = fileUploadProvider.upload(file, mimeType, displayName)

        assertEquals("files/test-file", result.name)
        file.delete()
    }
}
