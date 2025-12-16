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
import kotlinx.io.files.Path
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.io.IOException
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.Uint8Array

@Serializable
private data class FileWrapper(
    val file: GeminiFile,
)

@Serializable
private data class FileUploadRequest(
    val file: FileUploadRequestData
)

@Serializable
private data class FileUploadRequestData(
    val displayName: String
)

external interface Stats {
    val size: Double
}

external class Buffer : Uint8Array

@JsModule("fs")
@JsNonModule
external object fs {
    fun statSync(path: String): Stats
    fun readFileSync(path: String): Buffer
}

actual class FileUploadProvider actual constructor(
    private val apiKey: String,
    private val client: HttpClient?,
    private val json: Json,
) {
    private val httpClient = client ?: createHttpClient(json)
    private val baseUrl = "https://generativelanguage.googleapis.com"

    actual suspend fun upload(
        file: Path,
        mimeType: String,
        displayName: String,
    ): GeminiFile {
        val pathStr = file.toString()
        val stats = fs.statSync(pathStr)
        val size = stats.size.toLong()

        val uploadUrl = getUploadUrl(mimeType, displayName, size)

        val content = fs.readFileSync(pathStr)
        val byteArray = Int8Array(content.buffer, content.byteOffset, content.length).unsafeCast<ByteArray>()

        return uploadFile(uploadUrl, byteArray, mimeType, size)
    }

    actual suspend fun uploadToFileSearchStore(
        fileSearchStoreName: String,
        file: Path,
        mimeType: String,
        uploadRequest: UploadFileSearchStoreRequest,
    ): Operation {
        val pathStr = file.toString()
        val stats = fs.statSync(pathStr)
        val size = stats.size.toLong()

        val uploadUrl = getFileSearchStoreUploadUrl(fileSearchStoreName, mimeType, size, uploadRequest)

        val content = fs.readFileSync(pathStr)
        val byteArray = Int8Array(content.buffer, content.byteOffset, content.length).unsafeCast<ByteArray>()

        return uploadFileToSearchStore(uploadUrl, byteArray, mimeType, size)
    }

    private suspend fun getUploadUrl(
        mimeType: String,
        displayName: String,
        fileSize: Long,
    ): String {
        val requestBody = FileUploadRequest(FileUploadRequestData(displayName))
        val response =
            httpClient.post("$baseUrl/upload/v1beta/files") {
                header("x-goog-api-key", apiKey)
                header("X-Goog-Upload-Protocol", "resumable")
                header("X-Goog-Upload-Command", "start")
                header("X-Goog-Upload-Header-Content-Length", fileSize.toString())
                header("X-Goog-Upload-Header-Content-Type", mimeType)
                contentType(ContentType.Application.Json)
                setBody(json.encodeToString(requestBody))
            }

        if (response.status != HttpStatusCode.OK) {
            throw IOException("Failed to get upload URL: ${response.status} ${response.bodyAsText()}")
        }

        return response.headers["X-Goog-Upload-URL"]
            ?: throw IOException("Upload URL not found in response headers")
    }

    private suspend fun getFileSearchStoreUploadUrl(
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

    private suspend fun uploadFile(
        uploadUrl: String,
        fileContent: ByteArray,
        mimeType: String,
        fileSize: Long,
    ): GeminiFile {
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

    private suspend fun uploadFileToSearchStore(
        uploadUrl: String,
        fileContent: ByteArray,
        mimeType: String,
        fileSize: Long,
    ): Operation {
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
