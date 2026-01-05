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
import io.ktor.util.cio.readChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException

// Note: FileWrapper is private in jvmMain but since we are copying implementation,
// we need to make sure we don't conflict or we can reuse it if it was shared.
// But it's defined in the same file in jvmMain, so we define it here too.

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
        val baseUrl = "https://generativelanguage.googleapis.com"
        val uploadUrl = getUploadUrl(baseUrl, apiKey, mimeType, displayName, javaFile.length())
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
        val baseUrl = "https://generativelanguage.googleapis.com"
        val uploadUrl =
            getFileSearchStoreUploadUrl(
                baseUrl,
                apiKey,
                fileSearchStoreName,
                mimeType,
                javaFile.length(),
                uploadRequest,
            )
        return uploadFileToSearchStore(uploadUrl, javaFile, mimeType)
    }

    /**
     * Handles get upload url.
     *
     * @param baseUrl The base url.
     * @param apiKey The api key.
     * @param mimeType The mime type.
     * @param displayName The display name.
     * @param fileSize The file size.
     */
    private suspend fun getUploadUrl(
        baseUrl: String,
        apiKey: String,
        mimeType: String,
        displayName: String,
        fileSize: Long,
    ): String =
        withContext(Dispatchers.IO) {
            val response =
                httpClient.post("$baseUrl/upload/v1beta/files") {
                    header("x-goog-api-key", apiKey)
                    header("X-Goog-Upload-Protocol", "resumable")
                    header("X-Goog-Upload-Command", "start")
                    header("X-Goog-Upload-Header-Content-Length", fileSize.toString())
                    header("X-Goog-Upload-Header-Content-Type", mimeType)
                    contentType(ContentType.Application.Json)
                    setBody("""{ "file" : { "displayName" : "$displayName" }}""")
                }

            if (response.status != HttpStatusCode.OK) {
                throw IOException("Failed to get upload URL: ${response.status} ${response.bodyAsText()}")
            }

            response.headers["X-Goog-Upload-URL"]
                ?: throw IOException("Upload URL not found in response headers")
        }

    /**
     * Handles get file search store upload url.
     *
     * @param baseUrl The base url.
     * @param apiKey The api key.
     * @param fileSearchStoreName The file search store name.
     * @param mimeType The mime type.
     * @param fileSize The file size.
     * @param uploadRequest The upload request.
     */
    private suspend fun getFileSearchStoreUploadUrl(
        baseUrl: String,
        apiKey: String,
        fileSearchStoreName: String,
        mimeType: String,
        fileSize: Long,
        uploadRequest: UploadFileSearchStoreRequest,
    ): String =
        withContext(Dispatchers.IO) {
            val response =
                httpClient.post("$baseUrl/upload/v1beta/$fileSearchStoreName:uploadToFileSearchStore") {
                    header("x-goog-api-key", apiKey)
                    header("X-Goog-Upload-Protocol", "resumable")
                    header("X-Goog-Upload-Command", "start")
                    header("X-Goog-Upload-Header-Content-Length", fileSize.toString())
                    header("X-Goog-Upload-Header-Content-Type", mimeType)
                    contentType(ContentType.Application.Json)
                    setBody(json.encodeToString(uploadRequest))
                }

            if (response.status != HttpStatusCode.OK) {
                throw IOException("Failed to get upload URL: ${response.status} ${response.bodyAsText()}")
            }

            response.headers["X-Goog-Upload-URL"]
                ?: throw IOException("Upload URL not found in response headers")
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
            val response =
                httpClient.post(uploadUrl) {
                    header("Content-Length", file.length().toString())
                    header("X-Goog-Upload-Offset", "0")
                    header("X-Goog-Upload-Command", "upload, finalize")
                    contentType(ContentType.parse(mimeType))
                    setBody(file.readChannel())
                }

            if (response.status != HttpStatusCode.OK) {
                throw IOException("Failed to upload file: ${response.status} ${response.bodyAsText()}")
            }

            val responseText = response.bodyAsText()
            json.decodeFromString<FileWrapper>(responseText).file
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
            val response =
                httpClient.post(uploadUrl) {
                    header("Content-Length", file.length().toString())
                    header("X-Goog-Upload-Offset", "0")
                    header("X-Goog-Upload-Command", "upload, finalize")
                    contentType(ContentType.parse(mimeType))
                    setBody(file.readChannel())
                }

            if (response.status != HttpStatusCode.OK) {
                throw IOException("Failed to upload file: ${response.status} ${response.bodyAsText()}")
            }

            val responseText = response.bodyAsText()
            json.decodeFromString<Operation>(responseText)
        }
}
