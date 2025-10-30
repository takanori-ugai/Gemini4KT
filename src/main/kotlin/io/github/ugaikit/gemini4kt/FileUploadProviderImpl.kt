package io.github.ugaikit.gemini4kt

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

@Serializable
data class FileWrapper(val file: GeminiFile)
class FileUploadProviderImpl(
    private val apiKey: String,
    private val httpConnectionProvider: HttpConnectionProvider = DefaultHttpConnectionProvider(),
    private val json: Json = Json { ignoreUnknownKeys = true }
) : FileUploadProvider {

    override suspend fun upload(file: File, mimeType: String, displayName: String): GeminiFile {
        val baseUrl = "https://generativelanguage.googleapis.com"
        val uploadUrl = getUploadUrl(baseUrl, apiKey, mimeType, displayName, file.length())
        return uploadFile(uploadUrl, file, mimeType)
    }

    private suspend fun getUploadUrl(
        baseUrl: String,
        apiKey: String,
        mimeType: String,
        displayName: String,
        fileSize: Long
    ): String {
        return withContext(Dispatchers.IO) {
            val url = URL("$baseUrl/upload/v1beta/files?key=$apiKey")
            val connection = httpConnectionProvider.getConnection(url)
            connection.requestMethod = "POST"
            connection.addRequestProperty("X-Goog-Upload-Protocol", "resumable")
            connection.addRequestProperty("X-Goog-Upload-Command", "start")
            connection.addRequestProperty("X-Goog-Upload-Header-Content-Length", fileSize.toString())
            connection.addRequestProperty("X-Goog-Upload-Header-Content-Type", mimeType)
            connection.addRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            val fileWrapper = FileWrapper(GeminiFile(displayName = displayName, name = "", uri = "", mimeType = "", createTime = "", updateTime = "", expirationTime = "", sha256Hash = "", sizeBytes = 0))
            val requestBody = json.encodeToString(fileWrapper)
            connection.outputStream.write(requestBody.toByteArray())

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw IOException("Failed to get upload URL: ${connection.responseCode} ${connection.responseMessage}")
            }

            connection.headerFields["x-goog-upload-url"]?.get(0)
                ?: throw IOException("Upload URL not found in response headers")
        }
    }

    private suspend fun uploadFile(uploadUrl: String, file: File, mimeType: String): GeminiFile {
        return withContext(Dispatchers.IO) {
            val url = URL(uploadUrl)
            val connection = httpConnectionProvider.getConnection(url)
            connection.requestMethod = "POST"
            connection.addRequestProperty("Content-Length", file.length().toString())
            connection.addRequestProperty("X-Goog-Upload-Offset", "0")
            connection.addRequestProperty("X-Goog-Upload-Command", "upload, finalize")
            connection.addRequestProperty("Content-Type", mimeType)
            connection.doOutput = true

            val outputStream: OutputStream = connection.outputStream
            val inputStream: InputStream = file.inputStream()
            val buffer = ByteArray(4096)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
            outputStream.flush()
            inputStream.close()
            outputStream.close()

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw IOException("Failed to upload file: ${connection.responseCode} ${connection.responseMessage}")
            }

            val response = connection.inputStream.bufferedReader().use { it.readText() }
            json.decodeFromString<FileWrapper>(response).file
        }
    }
}
