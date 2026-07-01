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
import kotlinx.io.IOException
import kotlinx.io.files.Path
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.khronos.webgl.Int8Array
import org.khronos.webgl.Uint8Array

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
     * Holds the fs.
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
        val baseUrl = "https://generativelanguage.googleapis.com"
        val uploadUrl = getUploadUrl(baseUrl, apiKey, mimeType, displayName, fileSize)
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
        val baseUrl = "https://generativelanguage.googleapis.com"
        val uploadUrl =
            getFileSearchStoreUploadUrl(
                baseUrl,
                apiKey,
                fileSearchStoreName,
                mimeType,
                fileSize,
                uploadRequest,
            )
        return uploadFileToSearchStore(uploadUrl, pathStr, mimeType, fileSize)
    }

    /**
     * Handles get file size.
     *
     * @param path The path.
     */
    private fun getFileSize(path: String): Long {
        try {
            val stats = fs.statSync(path)
            return (stats.size as Number).toLong()
        } catch (e: dynamic) {
            throw IOException("Failed to get file size for $path: $e")
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
            val uint8Array = buffer.unsafeCast<Uint8Array>()
            val int8Array = Int8Array(uint8Array.buffer, uint8Array.byteOffset, uint8Array.length)
            return int8Array.unsafeCast<ByteArray>()
        } catch (e: dynamic) {
            throw IOException("Failed to read file $path: $e")
        }
    }

    /**
     * Loads the Node.js fs module when available.
     */
    private fun loadNodeFs(): dynamic {
        val module = js("(typeof require !== 'undefined' && require) ? require('fs') : null")
        if (module == null) {
            throw IOException("File upload is only supported in a Node.js environment.")
        }
        return module
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
    ): String {
        val response =
            httpClient.post("$baseUrl/upload/v1beta/files") {
                header("x-goog-api-key", apiKey)
                header("X-Goog-Upload-Protocol", "resumable")
                header("X-Goog-Upload-Command", "start")
                header("X-Goog-Upload-Header-Content-Length", fileSize.toString())
                header("X-Goog-Upload-Header-Content-Type", mimeType)
                contentType(ContentType.Application.Json)
                setBody(json.encodeToString(UploadFileRequest(UploadFileRequestFile(displayName))))
            }

        if (response.status != HttpStatusCode.OK) {
            throw IOException("Failed to get upload URL: ${response.status} ${response.bodyAsText()}")
        }

        return response.headers["X-Goog-Upload-URL"]
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
    ): String {
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

        return response.headers["X-Goog-Upload-URL"]
            ?: throw IOException("Upload URL not found in response headers")
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

        val response =
            httpClient.post(uploadUrl) {
                header("Content-Length", fileSize.toString())
                header("X-Goog-Upload-Offset", "0")
                header("X-Goog-Upload-Command", "upload, finalize")
                contentType(ContentType.parse(mimeType))
                setBody(fileContent)
            }

        if (response.status != HttpStatusCode.OK) {
            throw IOException("Failed to upload file: ${response.status} ${response.bodyAsText()}")
        }

        val responseText = response.bodyAsText()
        return json.decodeFromString<FileWrapper>(responseText).file
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

        val response =
            httpClient.post(uploadUrl) {
                header("Content-Length", fileSize.toString())
                header("X-Goog-Upload-Offset", "0")
                header("X-Goog-Upload-Command", "upload, finalize")
                contentType(ContentType.parse(mimeType))
                setBody(fileContent)
            }

        if (response.status != HttpStatusCode.OK) {
            throw IOException("Failed to upload file: ${response.status} ${response.bodyAsText()}")
        }

        val responseText = response.bodyAsText()
        return json.decodeFromString<Operation>(responseText)
    }
}
