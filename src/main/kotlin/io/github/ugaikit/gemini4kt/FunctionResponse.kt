package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

@Serializable
data class FunctionResponse(
    val name: String,
    val args: List<String>,
)
