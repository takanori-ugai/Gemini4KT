package io.github.ugaikit.gemini4kt.filesearch

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FileSearchTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `test createFileSearchStore`() =
        runBlocking {
            val request = FileSearchStore(displayName = "Test Store")
            val expectedResponse = FileSearchStore(name = "fileSearchStores/123", displayName = "Test Store")
            val responseString = json.encodeToString(expectedResponse)

            val mockEngine =
                MockEngine { requestData ->
                    assertEquals("https://generativelanguage.googleapis.com/v1beta/fileSearchStores", requestData.url.toString())
                    assertEquals(HttpMethod.Post, requestData.method)
                    respond(
                        content = responseString,
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
            val fileSearch = FileSearch(apiKey = "test-api-key", client = client)

            val result = fileSearch.createFileSearchStore(request)

            assertEquals(expectedResponse, result)
        }

    @Test
    fun `test getFileSearchStore`() =
        runBlocking {
            val storeName = "fileSearchStores/123"
            val expectedResponse = FileSearchStore(name = storeName, displayName = "Test Store")
            val responseString = json.encodeToString(expectedResponse)

            val mockEngine =
                MockEngine { requestData ->
                    assertEquals("https://generativelanguage.googleapis.com/v1beta/$storeName", requestData.url.toString())
                    assertEquals(HttpMethod.Get, requestData.method)
                    respond(
                        content = responseString,
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
            val fileSearch = FileSearch(apiKey = "test-api-key", client = client)

            val result = fileSearch.getFileSearchStore(storeName)

            assertEquals(expectedResponse, result)
        }

    @Test
    fun `test listFileSearchStores`() =
        runBlocking {
            val expectedResponse =
                ListFileSearchStoresResponse(
                    fileSearchStores = listOf(FileSearchStore(name = "fileSearchStores/123")),
                    nextPageToken = "token",
                )
            val responseString = json.encodeToString(expectedResponse)

            val mockEngine =
                MockEngine { requestData ->
                    assertEquals("https://generativelanguage.googleapis.com/v1beta/fileSearchStores?pageSize=10", requestData.url.toString())
                    assertEquals(HttpMethod.Get, requestData.method)
                    respond(
                        content = responseString,
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
            val fileSearch = FileSearch(apiKey = "test-api-key", client = client)

            val result = fileSearch.listFileSearchStores(pageSize = 10)

            assertEquals(expectedResponse, result)
        }

    @Test
    fun `test deleteFileSearchStore`() =
        runBlocking {
            val storeName = "fileSearchStores/123"

            val mockEngine =
                MockEngine { requestData ->
                    assertEquals("https://generativelanguage.googleapis.com/v1beta/$storeName?force=true", requestData.url.toString())
                    assertEquals(HttpMethod.Delete, requestData.method)
                    respond(
                        content = "",
                        status = HttpStatusCode.OK,
                    )
                }
            val client = HttpClient(mockEngine)
            val fileSearch = FileSearch(apiKey = "test-api-key", client = client)

            fileSearch.deleteFileSearchStore(storeName, force = true)
        }

    @Test
    fun `test importFileToFileSearchStore`() =
        runBlocking {
            val storeName = "fileSearchStores/123"
            val request = ImportFileRequest(fileName = "files/abc")
            val expectedResponse = Operation(name = "operations/import-op", done = false)
            val responseString = json.encodeToString(expectedResponse)

            val mockEngine =
                MockEngine { requestData ->
                    assertEquals("https://generativelanguage.googleapis.com/v1beta/$storeName:importFile", requestData.url.toString())
                    assertEquals(HttpMethod.Post, requestData.method)
                    respond(
                        content = responseString,
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
            val fileSearch = FileSearch(apiKey = "test-api-key", client = client)

            val result = fileSearch.importFileToFileSearchStore(storeName, request)

            assertEquals(expectedResponse, result)
        }

    @Test
    fun `test getFileSearchStoreOperation`() =
        runBlocking {
            val opName = "operations/import-op"
            val expectedResponse = Operation(name = opName, done = true)
            val responseString = json.encodeToString(expectedResponse)

            val mockEngine =
                MockEngine { requestData ->
                    assertEquals("https://generativelanguage.googleapis.com/v1beta/$opName", requestData.url.toString())
                    assertEquals(HttpMethod.Get, requestData.method)
                    respond(
                        content = responseString,
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
            val fileSearch = FileSearch(apiKey = "test-api-key", client = client)

            val result = fileSearch.getFileSearchStoreOperation(opName)

            assertEquals(expectedResponse, result)
        }
}
