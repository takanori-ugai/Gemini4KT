package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

@Serializable
data class Tools(val functionDeclaration: List<io.github.ugaikit.gemini4kt.FunctionDeclaration>)
