package io.github.ugaikit.gemini4kt.batch

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Represents a single request within a batch.
 *
 * @property request The actual request payload (e.g., GenerateContentRequest).
 *                   It is a JsonElement to allow flexible serialization.
 * @property metadata Optional metadata to identify the request.
 */
@Serializable
data class BatchItemRequest(
    val request: JsonElement,
    val metadata: ResponseMetadata? = null,
)
