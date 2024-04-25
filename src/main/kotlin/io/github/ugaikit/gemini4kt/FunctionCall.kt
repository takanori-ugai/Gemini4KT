package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

@Serializable
data class FunctionCall(
    val name: String,
    val args: List<String>,
)
