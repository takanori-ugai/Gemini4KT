package io.github.ugaikit.gemini4kt

import io.github.ugaikit.gemini4kt.filesearch.Operation
import io.github.ugaikit.gemini4kt.filesearch.UploadFileSearchStoreRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
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
     * Holds the base url.
     */
    private val baseUrl = "https://generativelanguage.googleapis.com"

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
            getInitialUploadUrl(
                mimeType,
                fileSize,
                "upload/v1beta/$fileSearchStoreName:uploadToFileSearchStore",
                json.encodeToString(uploadRequest),
            )
        return uploadFileToSearchStore(uploadUrl, file, mimeType, fileSize)
    }

    /**
     * Handles get initial upload url.
     *
     * @param mimeType The mime type.
     * @param fileSize The file size.
     * @param endpoint The endpoint.
     * @param bodyContent The body content.
     */
    private suspend fun getInitialUploadUrl(
        mimeType: String,
        fileSize: Long,
        endpoint: String,
        bodyContent: String,
    ): String =
        withContext(Dispatchers.IO) {
            val response =
                httpClient.post("$baseUrl/$endpoint") {
                    header("x-goog-api-key", apiKey)
                    header("X-Goog-Upload-Protocol", "resumable")
                    header("X-Goog-Upload-Command", "start")
                    header("X-Goog-Upload-Header-Content-Length", fileSize.toString())
                    header("X-Goog-Upload-Header-Content-Type", mimeType)
                    contentType(ContentType.Application.Json)
                    setBody(bodyContent)
                }

            if (response.status != HttpStatusCode.OK) {
                throw IOException("Failed to get upload URL: ${response.status} ${response.bodyAsText()}")
            }

            response.headers["X-Goog-Upload-URL"]
                ?: throw IOException("Upload URL not found in response headers")
        }

    /**
     * Handles perform upload.
     *
     * @param uploadUrl The upload url.
     * @param file The file.
     * @param mimeType The mime type.
     * @param fileSize The file size.
     */
    private suspend fun performUpload(
        uploadUrl: String,
        file: Path,
        mimeType: String,
        fileSize: Long,
    ): String =
        withContext(Dispatchers.IO) {
            val content = fs.source(file).buffered().readByteArray()
            val response =
                httpClient.post(uploadUrl) {
                    header("Content-Length", fileSize.toString())
                    header("X-Goog-Upload-Offset", "0")
                    header("X-Goog-Upload-Command", "upload, finalize")
                    contentType(ContentType.parse(mimeType))
                    setBody(content)
                }

            if (response.status != HttpStatusCode.OK) {
                throw IOException("Failed to upload file: ${response.status} ${response.bodyAsText()}")
            }

            response.bodyAsText()
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
        val responseText = performUpload(uploadUrl, file, mimeType, fileSize)
        return json.decodeFromString<FileWrapper>(responseText).file
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
        val responseText = performUpload(uploadUrl, file, mimeType, fileSize)
        return json.decodeFromString<Operation>(responseText)
    }
}
