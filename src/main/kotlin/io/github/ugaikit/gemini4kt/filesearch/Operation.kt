package io.github.ugaikit.gemini4kt.filesearch

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * This resource represents a long-running operation that is the result of a network API call.
 *
 * @property name The server-assigned name.
 * @property metadata Service-specific metadata associated with the operation.
 * @property done If the value is false, it means the operation is still in progress.
 * @property error The error result of the operation in case of failure or cancellation.
 * @property response The normal, successful response of the operation.
 */
@Serializable
data class Operation(
    val name: String? = null,
    val metadata: JsonObject? = null,
    val done: Boolean? = null,
    val error: Status? = null,
    val response: JsonObject? = null,
)
