package io.github.ugaikit.gemini4kt

import io.github.ugaikit.gemini4kt.filesearch.Operation
import io.github.ugaikit.gemini4kt.filesearch.UploadFileSearchStoreRequest
import io.ktor.client.HttpClient
import kotlinx.io.IOException
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Represents the file upload provider.
 *
 * @property apiKey The api key.
 * @property client The client.
 * @property json The json.
 */
actual class FileUploadProvider actual constructor(
    private val apiKey: String,
    private val client: HttpClient?,
    private val json: Json,
    private val maxUploadFileSizeBytes: Long,
) {
    init {
        require(apiKey.isNotBlank()) { "apiKey must not be blank." }
        require(maxUploadFileSizeBytes > 0) { "maxUploadFileSizeBytes must be greater than 0." }
    }

    /**
     * Holds the http client.
     */
    private val httpClient = client ?: createHttpClient(json)

    /**
     * Tracks whether this instance owns the HTTP client.
     */
    private val ownsHttpClient = client == null

    /**
     * Holds the fs.
     */
    private val fs = SystemFileSystem

    /**
     * Handles upload.
     *
     * @param file The file.
     * @param mimeType The mime type.
     * @param displayName The display name.
     */
    actual suspend fun upload(
        file: Path,
        mimeType: String,
        displayName: String,
    ): GeminiFile {
        val metadata = fs.metadataOrNull(file) ?: throw IOException("File not found: $file")
        val fileSize = metadata.size
        val requestBody = json.encodeToString(UploadFileRequest(UploadFileRequestFile(displayName)))
        requireUploadFileSizeWithinLimit(file.toString(), fileSize, maxUploadFileSizeBytes)
        val content =
            fs.source(file).buffered().use { source ->
                source.readByteArray()
            }
        return httpClient
            .performResumableUploadAndDecode<FileWrapper>(
                apiKey = apiKey,
                mimeType = mimeType,
                fileSize = fileSize,
                startBody = requestBody,
                uploadBody = content,
                json = json,
            ).file
    }

    /**
     * Handles upload to file search store.
     *
     * @param fileSearchStoreName The file search store name.
     * @param file The file.
     * @param mimeType The mime type.
     * @param uploadRequest The upload request.
     */
    actual suspend fun uploadToFileSearchStore(
        fileSearchStoreName: String,
        file: Path,
        mimeType: String,
        uploadRequest: UploadFileSearchStoreRequest,
    ): Operation {
        val metadata = fs.metadataOrNull(file) ?: throw IOException("File not found: $file")
        val fileSize = metadata.size
        val requestBody = json.encodeToString(uploadRequest)
        requireUploadFileSizeWithinLimit(file.toString(), fileSize, maxUploadFileSizeBytes)
        val content =
            fs.source(file).buffered().use { source ->
                source.readByteArray()
            }
        return httpClient
            .performResumableUploadAndDecode<Operation>(
                apiKey = apiKey,
                mimeType = mimeType,
                fileSize = fileSize,
                startBody = requestBody,
                uploadBody = content,
                json = json,
                endpoint = "upload/v1beta/$fileSearchStoreName:uploadToFileSearchStore",
            )
    }

    /**
     * Closes the internally owned HTTP client, if any.
     */
    actual fun close() {
        if (ownsHttpClient) {
            httpClient.close()
        }
    }
}
