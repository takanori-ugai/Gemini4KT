package io.github.ugaikit.gemini4kt.batch

import io.github.ugaikit.gemini4kt.GenerateContentResponse
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Represents a single inline response in a batch job result.
 *
 * @property response The response object (e.g., GenerateContentResponse).
 *                    It is a JsonElement to allow flexible parsing or mapping.
 * @property error Error details if the specific request failed.
 * @property metadata An optional key to identify the request.
 */
@Serializable
data class BatchInlineResponse(
    val response: GenerateContentResponse? = null,
    val error: JsonElement? = null,
    val metadata: ResponseMetadata? = null,
)

/**
 * Optional metadata to identify the request.
 *
 * @property key The key to identify the request.
 */
@Serializable
data class ResponseMetadata(
    val key: String? = null,
)
