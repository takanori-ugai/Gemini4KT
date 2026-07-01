package io.github.ugaikit.gemini4kt

import io.github.ugaikit.gemini4kt.filesearch.Operation
import io.github.ugaikit.gemini4kt.filesearch.UploadFileSearchStoreRequest
import io.ktor.client.HttpClient
import io.ktor.util.cio.readChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@kotlinx.serialization.Serializable
data class FileWrapper(
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
        val uploadUrl =
            withContext(Dispatchers.IO) {
                httpClient.requestResumableUploadUrl(apiKey, mimeType, javaFile.length(), requestBody)
            }
        return uploadFile(uploadUrl, javaFile, mimeType)
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
        val uploadUrl =
            withContext(Dispatchers.IO) {
                httpClient.requestResumableUploadUrl(
                    apiKey,
                    mimeType,
                    javaFile.length(),
                    json.encodeToString(uploadRequest),
                    "upload/v1beta/$fileSearchStoreName:uploadToFileSearchStore",
                )
            }
        return uploadFileToSearchStore(uploadUrl, javaFile, mimeType)
    }

    /**
     * Handles upload file.
     *
     * @param uploadUrl The upload url.
     * @param file The file.
     * @param mimeType The mime type.
     */
    private suspend fun uploadFile(
        uploadUrl: String,
        file: File,
        mimeType: String,
    ): GeminiFile =
        withContext(Dispatchers.IO) {
            val uploadResponse =
                httpClient.performResumableUploadAndDecode<FileWrapper>(
                    uploadUrl,
                    mimeType,
                    file.length(),
                    file.readChannel(),
                    json,
                )
            uploadResponse.file
        }

    /**
     * Handles upload file to search store.
     *
     * @param uploadUrl The upload url.
     * @param file The file.
     * @param mimeType The mime type.
     */
    private suspend fun uploadFileToSearchStore(
        uploadUrl: String,
        file: File,
        mimeType: String,
    ): Operation =
        withContext(Dispatchers.IO) {
            httpClient
                .performResumableUploadAndDecode(
                    uploadUrl,
                    mimeType,
                    file.length(),
                    file.readChannel(),
                    json,
                )
        }
}
