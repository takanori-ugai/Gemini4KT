package io.github.ugaikit.gemini4kt

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

class FileUploadProviderImplTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `upload returns file on success`() =
        runBlocking {
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

            val mockEngine =
                MockEngine { request ->
                    if (request.url.toString().contains("/upload/v1beta/files")) {
                        respond(
                            content = "",
                            status = HttpStatusCode.OK,
                            headers =
                                headersOf(
                                    Pair(HttpHeaders.ContentType, listOf("application/json")),
                                    Pair("X-Goog-Upload-URL", listOf(uploadUrl)),
                                ),
                        )
                    } else if (request.url.toString() == uploadUrl) {
                        respond(
                            content = response,
                            status = HttpStatusCode.OK,
                            headers = headersOf(HttpHeaders.ContentType, "application/json"),
                        )
                    } else {
                        respondBadRequest()
                    }
                }
            val client =
                HttpClient(mockEngine) {
                    install(ContentNegotiation) {
                        json(json)
                    }
                }
            val fileUploadProvider = FileUploadProviderImpl("test-api-key", client)

            val result = fileUploadProvider.upload(file, mimeType, displayName)

            assertEquals("files/test-file", result.name)
            file.delete()
        }
}
