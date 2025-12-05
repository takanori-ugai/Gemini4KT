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
 * @property codeExecution A [CodeExecution] object representing a code execution tool.
 * @property urlContext A [UrlContext] object representing a url context tool.
 */
@Serializable
data class Tool(
    val functionDeclarations: List<FunctionDeclaration>? = null,
    @SerialName("google_search")
    val googleSearch: GoogleSearch? = null,
    @SerialName("code_execution")
    val codeExecution: CodeExecution? = null,
    @SerialName("url_context")
    val urlContext: UrlContext? = null,
)

class ToolBuilder {
    private val functionDeclarations: MutableList<FunctionDeclaration> = mutableListOf()
    private var googleSearch: GoogleSearch? = null
    private var codeExecution: CodeExecution? = null
    private var urlContext: UrlContext? = null

    fun functionDeclaration(init: FunctionDeclarationBuilder.() -> Unit) {
        functionDeclarations.add(FunctionDeclarationBuilder().apply(init).build())
    }

    fun googleSearch() {
        this.googleSearch = GoogleSearch()
    }

    fun codeExecution() {
        this.codeExecution = CodeExecution()
    }

    fun urlContext() {
        this.urlContext = UrlContext()
    }

    fun build() =
        Tool(
            functionDeclarations = functionDeclarations,
            googleSearch = googleSearch,
            codeExecution = codeExecution,
            urlContext = urlContext,
        )
}

fun tool(init: ToolBuilder.() -> Unit): Tool = ToolBuilder().apply(init).build()
