package io.github.ugaikit.gemini4kt

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a tool that encapsulates function declarations, providing a structured
 * way to access the functionalities declared.
 *
 * @property functionDeclarations A [FunctionDeclaration] object containing the
 * details of the functions declared by this tool.
 * @property googleSearch A map representing a google search tool.
 */
@Serializable
data class Tool(
    val functionDeclarations: List<FunctionDeclaration>? = null,
    @SerialName("google_search")
    val googleSearch: Map<String, String>? = null,
)
