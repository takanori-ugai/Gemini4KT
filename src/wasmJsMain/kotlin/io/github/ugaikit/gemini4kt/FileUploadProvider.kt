package io.github.ugaikit.gemini4kt

import io.github.ugaikit.gemini4kt.filesearch.Operation
import io.github.ugaikit.gemini4kt.filesearch.UploadFileSearchStoreRequest
import io.ktor.client.HttpClient
import kotlinx.io.IOException
import kotlinx.io.files.Path
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Represents the file wrapper.
 *
 * @property file The file.
 */
@kotlinx.serialization.Serializable
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
    init {
        require(apiKey.isNotBlank()) { "apiKey must not be blank." }
    }

    /**
     * Holds the http client.
     */
    private val httpClient = client ?: createHttpClient(json)

    /**
     * Holds the fs module.
     */
    private val fs: dynamic by lazy { loadNodeFs() }

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
        val pathStr = file.toString()
        val fileSize = getFileSize(pathStr)
        val uploadUrl =
            httpClient.requestResumableUploadUrl(
                apiKey,
                mimeType,
                fileSize,
                json.encodeToString(UploadFileRequest(UploadFileRequestFile(displayName))),
            )
        return uploadFile(uploadUrl, pathStr, mimeType, fileSize)
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
        val pathStr = file.toString()
        val fileSize = getFileSize(pathStr)
        val uploadUrl =
            httpClient.requestResumableUploadUrl(
                apiKey,
                mimeType,
                fileSize,
                json.encodeToString(uploadRequest),
                "upload/v1beta/$fileSearchStoreName:uploadToFileSearchStore",
            )
        return uploadFileToSearchStore(uploadUrl, pathStr, mimeType, fileSize)
    }

    /**
     * Loads Node.js fs when available.
     */
    private fun loadNodeFs(): dynamic {
        throw IOException("File upload is only supported in a Node.js environment.")
    }

    /**
     * Handles get file size.
     *
     * @param path The path.
     */
    private fun getFileSize(path: String): Long {
        try {
            val stats = fs.statSync(path)
            return stats.size.toLong()
        } catch (e: dynamic) {
            throw IOException("Failed to get file size for $path", e)
        }
    }

    /**
     * Handles read file.
     *
     * @param path The path.
     */
    private fun readFile(path: String): ByteArray {
        try {
            val buffer = fs.readFileSync(path)
            val length = buffer.length as Int
            return ByteArray(length) { index ->
                (buffer[index] as Int).toByte()
            }
        } catch (e: dynamic) {
            throw IOException("Failed to read file $path", e)
        }
    }

    /**
     * Handles upload file.
     *
     * @param uploadUrl The upload url.
     * @param path The path.
     * @param mimeType The mime type.
     * @param fileSize The file size.
     */
    private suspend fun uploadFile(
        uploadUrl: String,
        path: String,
        mimeType: String,
        fileSize: Long,
    ): GeminiFile {
        val fileContent = readFile(path)
        val uploadResponse =
            httpClient.performResumableUploadAndDecode<FileWrapper>(
                uploadUrl,
                mimeType,
                fileSize,
                fileContent,
                json,
            )
        return uploadResponse.file
    }

    /**
     * Handles upload file to search store.
     *
     * @param uploadUrl The upload url.
     * @param path The path.
     * @param mimeType The mime type.
     * @param fileSize The file size.
     */
    private suspend fun uploadFileToSearchStore(
        uploadUrl: String,
        path: String,
        mimeType: String,
        fileSize: Long,
    ): Operation {
        val fileContent = readFile(path)
        return httpClient
            .performResumableUploadAndDecode(
                uploadUrl,
                mimeType,
                fileSize,
                fileContent,
                json,
            )
    }
}
