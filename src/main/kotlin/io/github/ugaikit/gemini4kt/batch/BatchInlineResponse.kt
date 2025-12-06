package io.github.ugaikit.gemini4kt.batch

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Represents a single inline response in a batch job result.
 *
 * @property response The response object (e.g., GenerateContentResponse).
 *                    It is a JsonElement to allow flexible parsing or mapping.
 * @property error Error details if the specific request failed.
 */
@Serializable
data class BatchInlineResponse(
    val response: JsonElement? = null,
    val error: JsonElement? = null, // Using JsonElement for error as structure might vary
)
