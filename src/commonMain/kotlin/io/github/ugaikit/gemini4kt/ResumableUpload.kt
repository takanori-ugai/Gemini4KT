package io.github.ugaikit.gemini4kt

import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.json.Json

internal const val GEMINI_UPLOAD_BASE_URL = "https://generativelanguage.googleapis.com"

private const val RESUMABLE_UPLOAD_PROTOCOL = "resumable"
private const val RESUMABLE_UPLOAD_START_COMMAND = "start"
private const val RESUMABLE_UPLOAD_FINALIZE_COMMAND = "upload, finalize"
private const val RESUMABLE_UPLOAD_START_PATH = "upload/v1beta/files"

internal suspend fun HttpClient.requestResumableUploadUrl(
    apiKey: String,
    mimeType: String,
    fileSize: Long,
    bodyContent: String,
    endpoint: String = RESUMABLE_UPLOAD_START_PATH,
): String {
    val response =
        post("$GEMINI_UPLOAD_BASE_URL/$endpoint") {
            header("x-goog-api-key", apiKey)
            header("X-Goog-Upload-Protocol", RESUMABLE_UPLOAD_PROTOCOL)
            header("X-Goog-Upload-Command", RESUMABLE_UPLOAD_START_COMMAND)
            header("X-Goog-Upload-Header-Content-Length", fileSize.toString())
            header("X-Goog-Upload-Header-Content-Type", mimeType)
            contentType(ContentType.Application.Json)
            setBody(bodyContent)
        }

    if (response.status != HttpStatusCode.OK) {
        throw kotlinx.io.IOException("Failed to get upload URL: ${response.status} ${response.bodyAsText()}")
    }

    return response.headers["X-Goog-Upload-URL"]
        ?: throw kotlinx.io.IOException("Upload URL not found in response headers")
}

internal suspend fun HttpClient.performResumableUpload(
    uploadUrl: String,
    mimeType: String,
    fileSize: Long,
    body: Any,
): String {
    val response =
        post(uploadUrl) {
            header("Content-Length", fileSize.toString())
            header("X-Goog-Upload-Offset", "0")
            header("X-Goog-Upload-Command", RESUMABLE_UPLOAD_FINALIZE_COMMAND)
            contentType(ContentType.parse(mimeType))
            setBody(body)
        }

    if (response.status != HttpStatusCode.OK) {
        throw kotlinx.io.IOException("Failed to upload file: ${response.status} ${response.bodyAsText()}")
    }

    return response.bodyAsText()
}

internal suspend inline fun <reified T> HttpClient.performResumableUploadAndDecode(
    uploadUrl: String,
    mimeType: String,
    fileSize: Long,
    body: Any,
    json: Json,
): T = json.decodeFromString(performResumableUpload(uploadUrl, mimeType, fileSize, body))
