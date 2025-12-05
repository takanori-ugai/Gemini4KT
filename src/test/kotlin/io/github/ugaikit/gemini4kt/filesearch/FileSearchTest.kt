package io.github.ugaikit.gemini4kt.filesearch

import io.github.ugaikit.gemini4kt.HttpConnectionProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.net.HttpURLConnection
import java.net.URL

class FileSearchTest {

    private lateinit var fileSearch: FileSearch
    private lateinit var mockConnection: HttpURLConnection
    private lateinit var mockConnectionProvider: HttpConnectionProvider
    private val json = Json { ignoreUnknownKeys = true }

    @BeforeEach
    fun setup() {
        mockConnection = mockk(relaxed = true)
        mockConnectionProvider = mockk()
        every { mockConnectionProvider.getConnection(any()) } returns mockConnection
        fileSearch = FileSearch(apiKey = "test-api-key", httpConnectionProvider = mockConnectionProvider)
    }

    @Test
    fun `test createFileSearchStore`() {
        val request = FileSearchStore(displayName = "Test Store")
        val expectedResponse = FileSearchStore(name = "fileSearchStores/123", displayName = "Test Store")
        val responseString = json.encodeToString(expectedResponse)

        every { mockConnection.responseCode } returns 200
        every { mockConnection.inputStream } returns ByteArrayInputStream(responseString.toByteArray())

        val result = fileSearch.createFileSearchStore(request)

        assertEquals(expectedResponse, result)
        verify { mockConnectionProvider.getConnection(URL("https://generativelanguage.googleapis.com/v1beta/fileSearchStores")) }
    }

    @Test
    fun `test getFileSearchStore`() {
        val storeName = "fileSearchStores/123"
        val expectedResponse = FileSearchStore(name = storeName, displayName = "Test Store")
        val responseString = json.encodeToString(expectedResponse)

        every { mockConnection.responseCode } returns 200
        every { mockConnection.inputStream } returns ByteArrayInputStream(responseString.toByteArray())

        val result = fileSearch.getFileSearchStore(storeName)

        assertEquals(expectedResponse, result)
        verify { mockConnectionProvider.getConnection(URL("https://generativelanguage.googleapis.com/v1beta/$storeName")) }
    }

    @Test
    fun `test listFileSearchStores`() {
        val expectedResponse = ListFileSearchStoresResponse(
            fileSearchStores = listOf(FileSearchStore(name = "fileSearchStores/123")),
            nextPageToken = "token"
        )
        val responseString = json.encodeToString(expectedResponse)

        every { mockConnection.responseCode } returns 200
        every { mockConnection.inputStream } returns ByteArrayInputStream(responseString.toByteArray())

        val result = fileSearch.listFileSearchStores(pageSize = 10)

        assertEquals(expectedResponse, result)
        verify { mockConnectionProvider.getConnection(URL("https://generativelanguage.googleapis.com/v1beta/fileSearchStores?pageSize=10")) }
    }

    @Test
    fun `test deleteFileSearchStore`() {
        val storeName = "fileSearchStores/123"
        every { mockConnection.responseCode } returns 200

        fileSearch.deleteFileSearchStore(storeName, force = true)

        verify { mockConnectionProvider.getConnection(URL("https://generativelanguage.googleapis.com/v1beta/$storeName?force=true")) }
        verify { mockConnection.requestMethod = "DELETE" }
    }

    @Test
    fun `test importFileToFileSearchStore`() {
        val storeName = "fileSearchStores/123"
        val request = ImportFileRequest(fileName = "files/abc")
        val expectedResponse = Operation(name = "operations/import-op", done = false)
        val responseString = json.encodeToString(expectedResponse)

        every { mockConnection.responseCode } returns 200
        every { mockConnection.inputStream } returns ByteArrayInputStream(responseString.toByteArray())

        val result = fileSearch.importFileToFileSearchStore(storeName, request)

        assertEquals(expectedResponse, result)
        verify { mockConnectionProvider.getConnection(URL("https://generativelanguage.googleapis.com/v1beta/$storeName:importFile")) }
    }

    @Test
    fun `test getFileSearchStoreOperation`() {
        val opName = "operations/import-op"
        val expectedResponse = Operation(name = opName, done = true)
        val responseString = json.encodeToString(expectedResponse)

        every { mockConnection.responseCode } returns 200
        every { mockConnection.inputStream } returns ByteArrayInputStream(responseString.toByteArray())

        val result = fileSearch.getFileSearchStoreOperation(opName)

        assertEquals(expectedResponse, result)
        verify { mockConnectionProvider.getConnection(URL("https://generativelanguage.googleapis.com/v1beta/$opName")) }
    }
}
