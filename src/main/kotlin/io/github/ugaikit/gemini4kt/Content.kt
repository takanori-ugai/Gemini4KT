package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

@Serializable
data class Content(
    val parts: List<io.github.ugaikit.gemini4kt.Part>,
    val role: String? = null,
)
