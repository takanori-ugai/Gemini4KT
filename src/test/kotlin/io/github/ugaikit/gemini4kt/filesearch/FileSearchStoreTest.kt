package io.github.ugaikit.gemini4kt.filesearch

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FileSearchStoreTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `test FileSearchStore serialization`() {
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

    @Test
    fun `test ImportFileRequest serialization`() {
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

    @Test
    fun `test Operation serialization`() {
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

    @Test
    fun `test UploadFileSearchStoreRequest serialization`() {
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
