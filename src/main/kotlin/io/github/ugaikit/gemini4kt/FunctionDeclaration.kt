package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

@Serializable
data class FunctionDeclaration(val name: String, val description: String, val parameters: Schema)
