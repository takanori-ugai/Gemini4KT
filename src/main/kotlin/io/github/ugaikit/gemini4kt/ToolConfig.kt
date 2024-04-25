package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

@Serializable
data class ToolConfig(val functionCallingConfig: FunctionCallingConfig)
