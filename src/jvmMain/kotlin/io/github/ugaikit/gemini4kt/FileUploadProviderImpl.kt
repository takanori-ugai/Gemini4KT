package io.github.ugaikit.gemini4kt

import io.github.ugaikit.gemini4kt.filesearch.Operation
import io.github.ugaikit.gemini4kt.filesearch.UploadFileSearchStoreRequest
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.cio.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException

@Serializable
data class FileWrapper(
    val file: GeminiFile,
)

class FileUploadProviderImpl(
    private val apiKey: String,
    private val client: HttpClient? = null,
    private val json: Json = Json { ignoreUnknownKeys = true },
) : FileUploadProvider {
    private val httpClient = client ?: createHttpClient(json)

    override suspend fun upload(
        file: File,
        mimeType: String,
        displayName: String,
    ): GeminiFile {
        val baseUrl = "https://generativelanguage.googleapis.com"
        val uploadUrl = getUploadUrl(baseUrl, apiKey, mimeType, displayName, file.length())
        return uploadFile(uploadUrl, file, mimeType)
    }

    override suspend fun uploadToFileSearchStore(
        fileSearchStoreName: String,
        file: File,
        mimeType: String,
        uploadRequest: UploadFileSearchStoreRequest,
    ): Operation {
        val baseUrl = "https://generativelanguage.googleapis.com"
        val uploadUrl = getFileSearchStoreUploadUrl(baseUrl, apiKey, fileSearchStoreName, mimeType, file.length(), uploadRequest)
        return uploadFileToSearchStore(uploadUrl, file, mimeType)
    }

    private suspend fun getUploadUrl(
        baseUrl: String,
        apiKey: String,
        mimeType: String,
        displayName: String,
        fileSize: Long,
    ): String =
        withContext(Dispatchers.IO) {
            val response = httpClient.post("$baseUrl/upload/v1beta/files") {
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

    private suspend fun getFileSearchStoreUploadUrl(
        baseUrl: String,
        apiKey: String,
        fileSearchStoreName: String,
        mimeType: String,
        fileSize: Long,
        uploadRequest: UploadFileSearchStoreRequest,
    ): String =
        withContext(Dispatchers.IO) {
            val response = httpClient.post("$baseUrl/upload/v1beta/$fileSearchStoreName:uploadToFileSearchStore") {
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

    private suspend fun uploadFile(
        uploadUrl: String,
        file: File,
        mimeType: String,
    ): GeminiFile =
        withContext(Dispatchers.IO) {
            val response = httpClient.post(uploadUrl) {
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

    private suspend fun uploadFileToSearchStore(
        uploadUrl: String,
        file: File,
        mimeType: String,
    ): Operation =
        withContext(Dispatchers.IO) {
            val response = httpClient.post(uploadUrl) {
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
