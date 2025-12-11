package io.github.ugaikit.gemini4kt.filesearch

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FileSearchTest {
    private lateinit var fileSearch: FileSearch
    private val json = Json { ignoreUnknownKeys = true }
    private val bUrl = "https://generativelanguage.googleapis.com/v1beta"

    private fun createFileSearch(handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponse): FileSearch {
        val client = HttpClient(MockEngine) {
            engine {
                addHandler(handler)
            }
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        return FileSearch(apiKey = "test-api-key", client = client)
    }

    @Test
    fun `test createFileSearchStore`() = runTest {
        val request = FileSearchStore(displayName = "Test Store")
        val expectedResponse = FileSearchStore(name = "fileSearchStores/123", displayName = "Test Store")
        val responseString = json.encodeToString(expectedResponse)

        fileSearch = createFileSearch { request ->
            assertEquals(HttpMethod.Post, request.method)
            assertEquals("$bUrl/fileSearchStores", request.url.toString())
            respond(responseString, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        }

        val result = fileSearch.createFileSearchStore(request)

        assertEquals(expectedResponse, result)
    }

    @Test
    fun `test getFileSearchStore`() = runTest {
        val storeName = "fileSearchStores/123"
        val expectedResponse = FileSearchStore(name = storeName, displayName = "Test Store")
        val responseString = json.encodeToString(expectedResponse)

        fileSearch = createFileSearch { request ->
            assertEquals(HttpMethod.Get, request.method)
            assertEquals("$bUrl/$storeName", request.url.toString())
            respond(responseString, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        }

        val result = fileSearch.getFileSearchStore(storeName)

        assertEquals(expectedResponse, result)
    }

    @Test
    fun `test listFileSearchStores`() = runTest {
        val expectedResponse =
            ListFileSearchStoresResponse(
                fileSearchStores = listOf(FileSearchStore(name = "fileSearchStores/123")),
                nextPageToken = "token",
            )
        val responseString = json.encodeToString(expectedResponse)

        fileSearch = createFileSearch { request ->
            assertEquals(HttpMethod.Get, request.method)
            assertEquals("$bUrl/fileSearchStores?pageSize=10", request.url.toString())
            respond(responseString, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        }

        val result = fileSearch.listFileSearchStores(pageSize = 10)

        assertEquals(expectedResponse, result)
    }

    @Test
    fun `test deleteFileSearchStore`() = runTest {
        val storeName = "fileSearchStores/123"

        fileSearch = createFileSearch { request ->
            assertEquals(HttpMethod.Delete, request.method)
            assertEquals("$bUrl/$storeName?force=true", request.url.toString())
            respondOK()
        }

        fileSearch.deleteFileSearchStore(storeName, force = true)
    }

    @Test
    fun `test importFileToFileSearchStore`() = runTest {
        val storeName = "fileSearchStores/123"
        val request = ImportFileRequest(fileName = "files/abc")
        val expectedResponse = Operation(name = "operations/import-op", done = false)
        val responseString = json.encodeToString(expectedResponse)

        fileSearch = createFileSearch { request ->
            assertEquals(HttpMethod.Post, request.method)
            assertEquals("$bUrl/$storeName:importFile", request.url.toString())
            respond(responseString, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        }

        val result = fileSearch.importFileToFileSearchStore(storeName, request)

        assertEquals(expectedResponse, result)
    }

    @Test
    fun `test getFileSearchStoreOperation`() = runTest {
        val opName = "operations/import-op"
        val expectedResponse = Operation(name = opName, done = true)
        val responseString = json.encodeToString(expectedResponse)

        fileSearch = createFileSearch { request ->
            assertEquals(HttpMethod.Get, request.method)
            assertEquals("$bUrl/$opName", request.url.toString())
            respond(responseString, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        }

        val result = fileSearch.getFileSearchStoreOperation(opName)

        assertEquals(expectedResponse, result)
    }
}
