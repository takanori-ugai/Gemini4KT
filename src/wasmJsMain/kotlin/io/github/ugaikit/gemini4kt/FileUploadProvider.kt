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
import kotlin.js.JsAny

@Serializable
private data class FileWrapper(
    val file: GeminiFile,
)

@kotlin.js.JsModule("fs")
external object NodeFs {
    fun statSync(path: String): NodeStats

    fun readFileSync(path: String): Uint8Array
}

external interface NodeStats : JsAny {
    val size: Double
}

external class Uint8Array : JsAny {
    val length: Int

    operator fun get(index: Int): Byte
}

actual class FileUploadProvider actual constructor(
    private val apiKey: String,
    private val client: HttpClient?,
    private val json: Json,
) {
    private val httpClient = client ?: createHttpClient(json)

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

    private fun getFileSize(path: String): Long {
        try {
            val stats = NodeFs.statSync(path)
            return stats.size.toLong()
        } catch (e: dynamic) {
            throw IOException("Failed to get file size for $path", e)
        }
    }

    private fun readFile(path: String): ByteArray {
        try {
            val uint8Array = NodeFs.readFileSync(path)
            val length = uint8Array.length
            val byteArray = ByteArray(length)
            for (i in 0 until length) {
                byteArray[i] = uint8Array[i]
            }
            return byteArray
        } catch (e: dynamic) {
            throw IOException("Failed to read file $path", e)
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
