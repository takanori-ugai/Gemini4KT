package io.github.ugaikit

import kotlinx.serialization.Serializable

@Serializable
data class Tools(val functionDeclaration: List<io.github.ugaikit.FunctionDeclaration>)
