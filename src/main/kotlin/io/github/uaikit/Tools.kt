package io.github.uaikit

import kotlinx.serialization.Serializable

@Serializable
data class Tools(val functionDeclaration: List<io.github.uaikit.FunctionDeclaration>)
