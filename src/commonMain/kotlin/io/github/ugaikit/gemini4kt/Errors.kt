package io.github.ugaikit.gemini4kt

import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

private val errorJson = Json { ignoreUnknownKeys = true }

/**
 * Throws a [GeminiException] containing error details decoded from this response's body.
 *
 * Falls back to basic status code and description if parsing fails.
 */
internal suspend fun HttpResponse.throwApiException(): Nothing {
    val errorMsg = bodyAsText()
    try {
        val errorResponse = errorJson.decodeFromString<GeminiErrorResponse>(errorMsg)
        throw GeminiException(errorResponse.error)
    } catch (e: GeminiException) {
        throw e
    } catch (_: SerializationException) {
        throw GeminiException(fallbackError(errorMsg))
    } catch (_: IllegalArgumentException) {
        throw GeminiException(fallbackError(errorMsg))
    }
}

private fun HttpResponse.fallbackError(errorMsg: String): GeminiError =
    GeminiError(
        code = status.value,
        message = summarizeErrorBody(errorMsg, status.description),
        status = status.description.ifBlank { status.value.toString() },
    )

internal fun summarizeErrorBody(
    errorMsg: String,
    fallback: String,
): String {
    val trimmed = errorMsg.trim()
    if (trimmed.isBlank()) {
        return fallback
    }
    val maxChars = 512
    return if (trimmed.length <= maxChars) {
        trimmed
    } else {
        trimmed.take(maxChars) + "..."
    }
}
