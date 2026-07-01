package io.github.ugaikit.gemini4kt

import io.github.ugaikit.gemini4kt.filesearch.Operation
import io.github.ugaikit.gemini4kt.filesearch.UploadFileSearchStoreRequest
import io.ktor.client.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Represents the file wrapper.
 *
 * @property file The file.
 */
@Serializable
private data class FileWrapper(
    val file: GeminiFile,
)

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
) {
    /**
     * Holds the http client.
     */
    private val httpClient = client ?: createHttpClient(json)

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
        val uploadUrl =
            getInitialUploadUrl(
                mimeType,
                fileSize,
                "upload/v1beta/files",
                json.encodeToString(UploadFileRequest(UploadFileRequestFile(displayName))),
            )
        return uploadFile(uploadUrl, file, mimeType, fileSize)
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
        val uploadUrl =
            withContext(Dispatchers.IO) {
                httpClient.requestResumableUploadUrl(
                    apiKey,
                    mimeType,
                    fileSize,
                    json.encodeToString(uploadRequest),
                    "upload/v1beta/$fileSearchStoreName:uploadToFileSearchStore",
                )
            }
        return uploadFileToSearchStore(uploadUrl, file, mimeType, fileSize)
    }

    /**
     * Handles upload file.
     *
     * @param uploadUrl The upload url.
     * @param file The file.
     * @param mimeType The mime type.
     * @param fileSize The file size.
     */
    private suspend fun uploadFile(
        uploadUrl: String,
        file: Path,
        mimeType: String,
        fileSize: Long,
    ): GeminiFile {
        val content =
            withContext(Dispatchers.IO) {
                fs.source(file).buffered().readByteArray()
            }
        val uploadResponse =
            withContext(Dispatchers.IO) {
                httpClient.performResumableUploadAndDecode<FileWrapper>(
                    uploadUrl,
                    mimeType,
                    fileSize,
                    content,
                    json,
                )
            }
        return uploadResponse.file
    }

    /**
     * Handles upload file to search store.
     *
     * @param uploadUrl The upload url.
     * @param file The file.
     * @param mimeType The mime type.
     * @param fileSize The file size.
     */
    private suspend fun uploadFileToSearchStore(
        uploadUrl: String,
        file: Path,
        mimeType: String,
        fileSize: Long,
    ): Operation {
        val content =
            withContext(Dispatchers.IO) {
                fs.source(file).buffered().readByteArray()
            }
        return withContext(Dispatchers.IO) {
            httpClient
                .performResumableUploadAndDecode(
                    uploadUrl,
                    mimeType,
                    fileSize,
                    content,
                    json,
                )
        }
    }
}
