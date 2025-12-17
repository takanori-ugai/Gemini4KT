package io.github.ugaikit.gemini4kt

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Exception thrown when the Gemini API returns an error.
 *
 * @property error The details of the error returned by the API.
 */
class GeminiException(
    val error: GeminiError,
) : RuntimeException(error.message)

/**
 * Represents the top-level error response structure from the Gemini API.
 */
@Serializable
@JsExport
data class GeminiErrorResponse(
    val error: GeminiError,
)

/**
 * Represents the detailed error information.
 *
 * @property code The HTTP status code of the error.
 * @property message A human-readable error message.
 * @property status The status string (e.g., "RESOURCE_EXHAUSTED").
 * @property details A list of additional details about the error.
 */
@Serializable
@JsExport
data class GeminiError(
    val code: Int,
    val message: String,
    val status: String,
    val details: Array<GeminiErrorDetail>? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as GeminiError

        if (code != other.code) return false
        if (message != other.message) return false
        if (status != other.status) return false
        if (details != null) {
            if (other.details == null) return false
            if (!details.contentEquals(other.details)) return false
        } else if (other.details != null) {
            return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = code
        result = 31 * result + message.hashCode()
        result = 31 * result + status.hashCode()
        result = 31 * result + (details?.contentHashCode() ?: 0)
        return result
    }
}

/**
 * Represents a detail object within the error response.
 * Fields are nullable as different error types provide different details.
 */
@Serializable
@JsExport
data class GeminiErrorDetail(
    @SerialName("@type") val type: String? = null,
    val reason: String? = null,
    val domain: String? = null,
    val metadata: Map<String, String>? = null,
    val retryDelay: String? = null,
    val links: Array<GeminiErrorLink>? = null,
    val violations: Array<GeminiErrorViolation>? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as GeminiErrorDetail

        if (type != other.type) return false
        if (reason != other.reason) return false
        if (domain != other.domain) return false
        if (metadata != other.metadata) return false
        if (retryDelay != other.retryDelay) return false
        if (links != null) {
            if (other.links == null) return false
            if (!links.contentEquals(other.links)) return false
        } else if (other.links != null) {
            return false
        }
        if (violations != null) {
            if (other.violations == null) return false
            if (!violations.contentEquals(other.violations)) return false
        } else if (other.violations != null) {
            return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = type?.hashCode() ?: 0
        result = 31 * result + (reason?.hashCode() ?: 0)
        result = 31 * result + (domain?.hashCode() ?: 0)
        result = 31 * result + (metadata?.hashCode() ?: 0)
        result = 31 * result + (retryDelay?.hashCode() ?: 0)
        result = 31 * result + (links?.contentHashCode() ?: 0)
        result = 31 * result + (violations?.contentHashCode() ?: 0)
        return result
    }
}

@Serializable
@JsExport
data class GeminiErrorLink(
    val description: String? = null,
    val url: String? = null,
)

@Serializable
@JsExport
data class GeminiErrorViolation(
    val quotaMetric: String? = null,
    val quotaId: String? = null,
    val quotaDimensions: Map<String, String>? = null,
    val description: String? = null,
)
