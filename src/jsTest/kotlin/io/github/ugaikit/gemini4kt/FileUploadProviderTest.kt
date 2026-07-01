package io.github.ugaikit.gemini4kt

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlinx.io.files.Path
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Represents the file upload provider test.
 */
class FileUploadProviderTest {
    /**
     * Holds the fs.
     */
    private val fs: dynamic =
        try {
            js("require('fs')")
        } catch (e: dynamic) {
            // fs is not available in non-Node.js environments
            null
        }

    /**
     * Holds the path.
     */
    private val path: dynamic =
        try {
            js("require('path')")
        } catch (e: dynamic) {
            // path is not available in non-Node.js environments
            null
        }

    /**
     * Holds the os.
     */
    private val os: dynamic =
        try {
            js("require('os')")
        } catch (e: dynamic) {
            // os is not available in non-Node.js environments
            null
        }

    /**
     * Holds the json.
     */
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Tests test upload.
     */
    @Test
    fun testUpload() =
        runTest {
            if (fs == null) {
                println("Skipping testUpload because fs is not available")
                return@runTest
            }

            // Create a temporary file
            val tempDir = os.tmpdir() as String
            val tempFile = path.join(tempDir, "test_file.txt") as String
            fs.writeFileSync(tempFile, "Hello World")

            val mockEngine =
                MockEngine { request ->
                    val url = request.url.toString()
                    if (
                        url.contains("upload/v1beta/files") &&
                        request.headers["X-Goog-Upload-Command"] == "start"
                    ) {
                        respond(
                            content = "",
                            status = HttpStatusCode.OK,
                            headers =
                                headersOf(
                                    "X-Goog-Upload-URL" to listOf("https://upload.example.com/upload"),
                                ),
                        )
                    } else if (url == "https://upload.example.com/upload") {
                        respond(
                            content = """{ "file": {
                        "name": "files/123",
                        "displayName": "test_file.txt",
                        "mimeType": "text/plain",
                        "uri": "https://example.com/file",
                        "createTime": "2023-10-27T10:00:00Z",
                        "updateTime": "2023-10-27T10:00:00Z",
                        "expirationTime": "2023-10-28T10:00:00Z",
                        "sha256Hash": "hash123",
                        "sizeBytes": 1024
                    } }""",
                            status = HttpStatusCode.OK,
                            headers =
                                headersOf(
                                    HttpHeaders.ContentType to
                                        listOf(ContentType.Application.Json.toString()),
                                ),
                        )
                    } else {
                        respond("Error", HttpStatusCode.BadRequest)
                    }
                }

            val client =
                HttpClient(mockEngine) {
                    install(ContentNegotiation) {
                        json(json)
                    }
                }

            val provider =
                FileUploadProvider(
                    apiKey = "test_key",
                    client = client,
                    json = json,
                )

            try {
                val result = provider.upload(Path(tempFile), "text/plain", "test_file.txt")
                assertEquals("files/123", result.name)
                assertEquals("test_file.txt", result.displayName)
                assertEquals("text/plain", result.mimeType)
            } finally {
                try {
                    fs.unlinkSync(tempFile)
                } catch (e: dynamic) {
                    // Ignore cleanup errors, as failing to delete a temp file should not fail the test
                }
            }
        }

    /**
     * Tests upload fails outside Node.js environments.
     */
    @Test
    fun testUploadFailsOutsideNodeEnvironment() =
        runTest {
            val mockClient =
                HttpClient(MockEngine { respond("unused", HttpStatusCode.OK) }) {
                    install(ContentNegotiation) {
                        json(json)
                    }
                }
            val provider =
                FileUploadProvider(
                    apiKey = "test_key",
                    client = mockClient,
                    json = json,
                )

            val globalThis: dynamic = js("globalThis")
            val originalProcess: dynamic = globalThis.process
            try {
                globalThis.process = null

                val exception =
                    assertFailsWith<IOException> {
                        provider.upload(Path("/tmp/does-not-matter"), "text/plain", "test_file.txt")
                    }

                assertTrue(exception.message?.contains("Node.js environment") == true)
            } finally {
                globalThis.process = originalProcess
            }
        }
}
