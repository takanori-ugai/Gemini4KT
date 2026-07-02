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
    maxUploadFileSizeBytes: Long = DEFAULT_MAX_UPLOAD_FILE_SIZE_BYTES,
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

    /**
     * Releases the internally owned HTTP client, if one was created.
     */
    fun close()
}

internal const val DEFAULT_MAX_UPLOAD_FILE_SIZE_BYTES: Long = 50L * 1024L * 1024L

internal fun requireUploadFileSizeWithinLimit(
    path: String,
    fileSize: Long,
    maxUploadFileSizeBytes: Long,
) {
    require(fileSize <= maxUploadFileSizeBytes) {
        "File $path is $fileSize bytes, which exceeds the upload limit of $maxUploadFileSizeBytes bytes."
    }
}
