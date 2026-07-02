package io.github.ugaikit.gemini4kt

import io.github.ugaikit.gemini4kt.filesearch.Operation
import io.github.ugaikit.gemini4kt.filesearch.UploadFileSearchStoreRequest
import io.ktor.client.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

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
        val javaFile = File(file.toString())
        val requestBody = json.encodeToString(UploadFileRequest(UploadFileRequestFile(displayName)))
        val (fileSize, fileContent) =
            withContext(Dispatchers.IO) {
                val size = javaFile.length()
                requireUploadFileSizeWithinLimit(file.toString(), size, maxUploadFileSizeBytes)
                size to javaFile.readBytes()
            }
        return withContext(Dispatchers.IO) {
            httpClient
                .performResumableUploadAndDecode<FileWrapper>(
                    apiKey = apiKey,
                    mimeType = mimeType,
                    fileSize = fileSize,
                    startBody = requestBody,
                    uploadBody = fileContent,
                    json = json,
                ).file
        }
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
        val javaFile = File(file.toString())
        val requestBody = json.encodeToString(uploadRequest)
        val (fileSize, fileContent) =
            withContext(Dispatchers.IO) {
                val size = javaFile.length()
                requireUploadFileSizeWithinLimit(file.toString(), size, maxUploadFileSizeBytes)
                size to javaFile.readBytes()
            }
        return withContext(Dispatchers.IO) {
            httpClient
                .performResumableUploadAndDecode<Operation>(
                    apiKey = apiKey,
                    mimeType = mimeType,
                    fileSize = fileSize,
                    startBody = requestBody,
                    uploadBody = fileContent,
                    json = json,
                    endpointPathSegments =
                        listOf("upload", "v1beta", "fileSearchStores") +
                            normalizeResourcePathSegments(fileSearchStoreName, "fileSearchStores"),
                    endpointSuffix = ":uploadToFileSearchStore",
                )
        }
    }

    actual fun close() {
        if (ownsHttpClient) {
            httpClient.close()
        }
    }
}
