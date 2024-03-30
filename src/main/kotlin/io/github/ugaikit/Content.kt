package io.github.ugaikit

import kotlinx.serialization.Serializable

@Serializable
data class Content(
    val parts: List<io.github.ugaikit.Part>,
    val role: String? = null,
)
