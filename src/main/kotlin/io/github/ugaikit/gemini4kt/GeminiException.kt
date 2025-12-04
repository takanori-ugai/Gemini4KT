package io.github.ugaikit.gemini4kt

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Exception thrown when the Gemini API returns an error.
 *
 * @property error The details of the error returned by the API.
 */
class GeminiException(
    val error: GeminiError
) : RuntimeException(error.message)

/**
 * Represents the top-level error response structure from the Gemini API.
 */
@Serializable
data class GeminiErrorResponse(
    val error: GeminiError
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
data class GeminiError(
    val code: Int,
    val message: String,
    val status: String,
    val details: List<GeminiErrorDetail>? = null
)

/**
 * Represents a detail object within the error response.
 * Fields are nullable as different error types provide different details.
 */
@Serializable
data class GeminiErrorDetail(
    @SerialName("@type") val type: String? = null,
    val reason: String? = null,
    val domain: String? = null,
    val metadata: Map<String, String>? = null,
    val retryDelay: String? = null,
    val links: List<GeminiErrorLink>? = null,
    val violations: List<GeminiErrorViolation>? = null
)

@Serializable
data class GeminiErrorLink(
    val description: String? = null,
    val url: String? = null
)

@Serializable
data class GeminiErrorViolation(
    val quotaMetric: String? = null,
    val quotaId: String? = null,
    val quotaDimensions: Map<String, String>? = null,
    val description: String? = null
)
