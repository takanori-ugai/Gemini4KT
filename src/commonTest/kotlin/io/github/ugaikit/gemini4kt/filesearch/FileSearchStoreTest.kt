package io.github.ugaikit.gemini4kt.filesearch

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Represents the file search store test.
 */
class FileSearchStoreTest {
    /**
     * Holds the json.
     */
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Tests test file search store serialization.
     */
    @Test
    fun testFileSearchStoreSerialization() {
        val store =
            FileSearchStore(
                name = "fileSearchStores/test-store",
                displayName = "Test Store",
                activeDocumentsCount = "10",
            )
        val encoded = json.encodeToString(store)
        val decoded = json.decodeFromString<FileSearchStore>(encoded)

        assertEquals(store, decoded)
    }

    /**
     * Tests test import file request serialization.
     */
    @Test
    fun testImportFileRequestSerialization() {
        val request =
            ImportFileRequest(
                fileName = "files/test-file",
                customMetadata = listOf(CustomMetadata(key = "author", stringValue = "me")),
                chunkingConfig = ChunkingConfig(chunkSize = 100, chunkOverlap = 10),
            )
        val encoded = json.encodeToString(request)
        val decoded = json.decodeFromString<ImportFileRequest>(encoded)

        assertEquals(request, decoded)
    }

    /**
     * Tests test operation serialization.
     */
    @Test
    fun testOperationSerialization() {
        val operation =
            Operation(
                name = "operations/op-123",
                done = false,
                metadata = JsonObject(mapOf("progress" to JsonPrimitive("50%"))),
            )
        val encoded = json.encodeToString(operation)
        val decoded = json.decodeFromString<Operation>(encoded)

        assertEquals(operation, decoded)
    }

    /**
     * Tests test upload file search store request serialization.
     */
    @Test
    fun testUploadFileSearchStoreRequestSerialization() {
        val request =
            UploadFileSearchStoreRequest(
                displayName = "Uploaded Doc",
                mimeType = "text/plain",
            )
        val encoded = json.encodeToString(request)
        val decoded = json.decodeFromString<UploadFileSearchStoreRequest>(encoded)

        assertEquals(request, decoded)
    }
}
