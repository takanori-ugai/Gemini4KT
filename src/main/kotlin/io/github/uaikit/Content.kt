package io.github.uaikit

import kotlinx.serialization.Serializable

@Serializable
data class Content(
    val parts: List<io.github.uaikit.Part>,
    val role: String? = null,
)
