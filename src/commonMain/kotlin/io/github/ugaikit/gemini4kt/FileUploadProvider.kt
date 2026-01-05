package io.github.ugaikit.gemini4kt

import io.github.ugaikit.gemini4kt.filesearch.Operation
import io.github.ugaikit.gemini4kt.filesearch.UploadFileSearchStoreRequest
import io.ktor.client.HttpClient
import kotlinx.io.files.Path
import kotlinx.serialization.json.Json

/**
 * Represents the file upload provider.
 */
expect class FileUploadProvider(
    apiKey: String,
    client: HttpClient? = null,
    json: Json = Json { ignoreUnknownKeys = true },
) {
    /**
     * Handles upload.
     *
     * @param file The file.
     * @param mimeType The mime type.
     * @param displayName The display name.
     */
    suspend fun upload(
        file: Path,
        mimeType: String,
        displayName: String,
    ): GeminiFile

    /**
     * Handles upload to file search store.
     *
     * @param fileSearchStoreName The file search store name.
     * @param file The file.
     * @param mimeType The mime type.
     * @param uploadRequest The upload request.
     */
    suspend fun uploadToFileSearchStore(
        fileSearchStoreName: String,
        file: Path,
        mimeType: String,
        uploadRequest: UploadFileSearchStoreRequest,
    ): Operation
}
