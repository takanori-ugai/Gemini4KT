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
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.khronos.webgl.Int8Array
import org.khronos.webgl.Uint8Array

@Serializable
private data class FileWrapper(
    val file: GeminiFile,
)

actual class FileUploadProvider actual constructor(
    private val apiKey: String,
    private val client: HttpClient?,
    private val json: Json,
) {
    private val httpClient = client ?: createHttpClient(json)

    // Lazy load fs to avoid issues if not in Node (though the target is Node)
    private val fs: dynamic by lazy {
        try {
            js("require('fs')")
        } catch (e: dynamic) {
            throw IOException("Module 'fs' not found. File upload is only supported in Node.js environment.")
        }
    }

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

    actual suspend fun uploadToFileSearchStore(
        fileSearchStoreName: String,
        file: Path,
        mimeType: String,
        uploadRequest: UploadFileSearchStoreRequest,
    ): Operation {
        val pathStr = file.toString()
        val fileSize = getFileSize(pathStr)
        val baseUrl = "https://generativelanguage.googleapis.com"
        val uploadUrl = getFileSearchStoreUploadUrl(baseUrl, apiKey, fileSearchStoreName, mimeType, fileSize, uploadRequest)
        return uploadFileToSearchStore(uploadUrl, pathStr, mimeType, fileSize)
    }

    private fun getFileSize(path: String): Long {
        try {
            val stats = fs.statSync(path)
            // stats.size is a Number
            return (stats.size as Number).toLong()
        } catch (e: dynamic) {
            throw IOException("Failed to get file size for $path: $e")
        }
    }

    private fun readFile(path: String): ByteArray {
        try {
            val buffer = fs.readFileSync(path)
            // Convert Node Buffer to ByteArray (Int8Array)
            // Buffer is a Uint8Array in modern Node.js
            val uint8Array = buffer.unsafeCast<Uint8Array>()
            val int8Array = Int8Array(uint8Array.buffer, uint8Array.byteOffset, uint8Array.length)
            return int8Array.unsafeCast<ByteArray>()
        } catch (e: dynamic) {
            throw IOException("Failed to read file $path: $e")
        }
    }

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
                setBody("""{ "file" : { "displayName" : "$displayName" }}""")
            }

        if (response.status != HttpStatusCode.OK) {
            throw IOException("Failed to get upload URL: ${response.status} ${response.bodyAsText()}")
        }

        return response.headers["X-Goog-Upload-URL"]
            ?: throw IOException("Upload URL not found in response headers")
    }

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
