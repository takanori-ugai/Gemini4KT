package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

/**
 * Represents a tool that encapsulates function declarations, providing a structured
 * way to access the functionalities declared.
 *
 * @property functionDeclarations A [FunctionDeclaration] object containing the
 * details of the functions declared by this tool.
 */
@Serializable
data class Tool(
    val functionDeclarations: List<FunctionDeclaration>,
)
