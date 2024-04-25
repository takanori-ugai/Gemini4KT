package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

@Serializable
data class FunctionCallingConfig(
    val mode: Mode,
    val allowedFunctionNames: List<String>,
)
