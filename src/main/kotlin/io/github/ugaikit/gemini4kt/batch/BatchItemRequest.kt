package io.github.ugaikit.gemini4kt.batch

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Represents a single request item in a batch job.
 *
 * @property request The actual request object (e.g., GenerateContentRequest).
 *                   It is a JsonElement to support different request types flexibly,
 *                   but typically it will be serialized from GenerateContentRequest.
 * @property metadata Metadata associated with the request, such as a user-defined key.
 */
@Serializable
data class BatchItemRequest(
    val request: JsonElement,
    val metadata: Map<String, String>? = null,
)
