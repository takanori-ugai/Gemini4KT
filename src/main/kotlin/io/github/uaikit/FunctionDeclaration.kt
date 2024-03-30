package io.github.uaikit

import kotlinx.serialization.Serializable

@Serializable
data class FunctionDeclaration(val name: String, val description: String, val parameters: io.github.uaikit.Schema)
