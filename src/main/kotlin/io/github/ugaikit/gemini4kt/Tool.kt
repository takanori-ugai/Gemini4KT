package io.github.ugaikit.gemini4kt

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a tool that encapsulates function declarations, providing a structured
 * way to access the functionalities declared.
 *
 * @property functionDeclarations A [FunctionDeclaration] object containing the
 * details of the functions declared by this tool.
 * @property googleSearch A [GoogleSearch] object representing a google search tool.
 */
@Serializable
data class Tool(
    val functionDeclarations: List<FunctionDeclaration>? = null,
    @SerialName("google_search")
    val googleSearch: GoogleSearch? = null,
)

class ToolBuilder {
    private val functionDeclarations: MutableList<FunctionDeclaration> = mutableListOf()
    private var googleSearch: GoogleSearch? = null

    fun functionDeclaration(init: FunctionDeclarationBuilder.() -> Unit) {
        functionDeclarations.add(FunctionDeclarationBuilder().apply(init).build())
    }

    fun googleSearch() {
        this.googleSearch = GoogleSearch()
    }

    fun build() =
        Tool(
            functionDeclarations = functionDeclarations,
            googleSearch = googleSearch,
        )
}

fun tool(init: ToolBuilder.() -> Unit): Tool = ToolBuilder().apply(init).build()
