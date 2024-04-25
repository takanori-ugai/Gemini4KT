package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

/**
 * Represents the content composed of multiple parts, optionally associated with a specific role.
 *
 * @property parts A list of [io.github.ugaikit.gemini4kt.Part] objects, representing the individual components
 * or sections of the content.
 * @property role An optional string indicating the role or function of this content within a larger context.
 * It can be `null` if the role is not specified or not applicable.
 */
@Serializable
data class Content(
    val parts: List<Part>,
    val role: String? = null,
)
