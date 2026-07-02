package io.github.ugaikit.gemini4kt

import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.contentType
import kotlinx.serialization.json.Json

internal const val GEMINI_UPLOAD_BASE_URL = "https://generativelanguage.googleapis.com"

private const val RESUMABLE_UPLOAD_PROTOCOL = "resumable"
private const val RESUMABLE_UPLOAD_START_COMMAND = "start"
private const val RESUMABLE_UPLOAD_FINALIZE_COMMAND = "upload, finalize"
private val RESUMABLE_UPLOAD_START_PATH_SEGMENTS = listOf("upload", "v1beta", "files")

@kotlinx.serialization.Serializable
internal data class FileWrapper(
    val file: GeminiFile,
)

internal suspend fun HttpClient.requestResumableUploadUrl(
    apiKey: String,
    mimeType: String,
    fileSize: Long,
    bodyContent: String,
    endpointPathSegments: List<String> = RESUMABLE_UPLOAD_START_PATH_SEGMENTS,
    endpointSuffix: String? = null,
): String {
    val endpoint = buildUrl(GEMINI_UPLOAD_BASE_URL, endpointPathSegments) + endpointSuffix.orEmpty()
    val response =
        post(endpoint) {
            header("x-goog-api-key", apiKey)
            header("X-Goog-Upload-Protocol", RESUMABLE_UPLOAD_PROTOCOL)
            header("X-Goog-Upload-Command", RESUMABLE_UPLOAD_START_COMMAND)
            header("X-Goog-Upload-Header-Content-Length", fileSize.toString())
            header("X-Goog-Upload-Header-Content-Type", mimeType)
            contentType(ContentType.Application.Json)
            setBody(TextContent(bodyContent, ContentType.Application.Json))
        }

    if (response.status != HttpStatusCode.OK) {
        throw kotlinx.io.IOException(
            "Failed to get upload URL: ${response.status} ${summarizeResponseBody(response.bodyAsText())}",
        )
    }

    return response.headers["X-Goog-Upload-URL"]
        ?: throw kotlinx.io.IOException("Upload URL not found in response headers")
}

internal suspend fun HttpClient.performResumableUpload(
    uploadUrl: String,
    mimeType: String,
    fileSize: Long,
    body: ByteArray,
): String {
    val bodySize = body.size.toLong()
    require(fileSize == bodySize) {
        "fileSize ($fileSize) must match upload body size ($bodySize)."
    }
    val response =
        post(uploadUrl) {
            header("Content-Length", bodySize.toString())
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
    body: ByteArray,
    json: Json,
): T = json.decodeFromString(performResumableUpload(uploadUrl, mimeType, fileSize, body))

/**
 * Performs the full resumable upload flow: request an upload URL, upload the payload, and decode
 * the final response.
 *
 * @param apiKey API key used for the resumable upload request.
 * @param mimeType MIME type of the uploaded file.
 * @param fileSize Size of the uploaded file in bytes.
 * @param startBody JSON body sent when requesting the resumable upload URL.
 * @param uploadBody Raw file bytes to upload.
 * @param json JSON serializer used to decode the final response.
 * @param endpoint Optional upload endpoint path segment.
 * @return Decoded response payload of type [T].
 */
internal suspend inline fun <reified T> HttpClient.performResumableUpload(
    apiKey: String,
    mimeType: String,
    fileSize: Long,
    startBody: String,
    uploadBody: ByteArray,
    json: Json,
    endpointPathSegments: List<String> = RESUMABLE_UPLOAD_START_PATH_SEGMENTS,
    endpointSuffix: String? = null,
): T {
    require(fileSize == uploadBody.size.toLong()) {
        "fileSize ($fileSize) must match upload body size (${uploadBody.size})."
    }
    val uploadUrl =
        requestResumableUploadUrl(
            apiKey = apiKey,
            mimeType = mimeType,
            fileSize = fileSize,
            bodyContent = startBody,
            endpointPathSegments = endpointPathSegments,
            endpointSuffix = endpointSuffix,
        )
    return performResumableUploadAndDecode(uploadUrl, mimeType, fileSize, uploadBody, json)
}

/**
 * Performs the full resumable upload flow and decodes the final response directly.
 *
 * @param apiKey API key used for the resumable upload request.
 * @param mimeType MIME type of the uploaded file.
 * @param fileSize Size of the uploaded file in bytes.
 * @param startBody JSON body sent when requesting the resumable upload URL.
 * @param uploadBody Raw file bytes to upload.
 * @param json JSON serializer used to decode the final response.
 * @param endpoint Optional upload endpoint path segment.
 * @return Decoded response payload of type [T].
 */
internal suspend inline fun <reified T> HttpClient.performResumableUploadAndDecode(
    apiKey: String,
    mimeType: String,
    fileSize: Long,
    startBody: String,
    uploadBody: ByteArray,
    json: Json,
    endpointPathSegments: List<String> = RESUMABLE_UPLOAD_START_PATH_SEGMENTS,
    endpointSuffix: String? = null,
): T {
    require(fileSize == uploadBody.size.toLong()) {
        "fileSize ($fileSize) must match upload body size (${uploadBody.size})."
    }
    val uploadUrl =
        requestResumableUploadUrl(
            apiKey = apiKey,
            mimeType = mimeType,
            fileSize = fileSize,
            bodyContent = startBody,
            endpointPathSegments = endpointPathSegments,
            endpointSuffix = endpointSuffix,
        )
    return performResumableUploadAndDecode(uploadUrl, mimeType, fileSize, uploadBody, json)
}

private fun summarizeResponseBody(body: String): String {
    val trimmed = body.trim()
    if (trimmed.isBlank()) {
        return ""
    }
    val maxChars = 512
    return if (trimmed.length <= maxChars) {
        trimmed
    } else {
        trimmed.take(maxChars) + "..."
    }
}
