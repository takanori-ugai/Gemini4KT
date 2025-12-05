package io.github.ugaikit.gemini4kt.filesearch

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * The Status type defines a logical error model that is suitable for different programming environments, including REST APIs and RPC APIs.
 *
 * @property code The status code, which should be an enum value of google.rpc.Code.
 * @property message A developer-facing error message, which should be in English.
 * @property details A list of messages that carry the error details.
 */
@Serializable
data class Status(
    val code: Int? = null,
    val message: String? = null,
    val details: List<JsonObject>? = null,
)
