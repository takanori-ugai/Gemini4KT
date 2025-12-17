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
 * @property fileSearch A [FileSearchTool] object representing a file search tool.
 */
@Serializable
@JsExport
data class Tool(
    val functionDeclarations: Array<FunctionDeclaration>? = null,
    @SerialName("google_search")
    val googleSearch: GoogleSearch? = null,
    @SerialName("code_execution")
    val codeExecution: CodeExecution? = null,
    @SerialName("url_context")
    val urlContext: UrlContext? = null,
    @SerialName("file_search")
    val fileSearch: FileSearchTool? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Tool

        if (functionDeclarations != null) {
            if (other.functionDeclarations == null) return false
            if (!functionDeclarations.contentEquals(other.functionDeclarations)) return false
        } else if (other.functionDeclarations != null) {
            return false
        }
        if (googleSearch != other.googleSearch) return false
        if (codeExecution != other.codeExecution) return false
        if (urlContext != other.urlContext) return false
        if (fileSearch != other.fileSearch) return false

        return true
    }

    override fun hashCode(): Int {
        var result = functionDeclarations?.contentHashCode() ?: 0
        result = 31 * result + (googleSearch?.hashCode() ?: 0)
        result = 31 * result + (codeExecution?.hashCode() ?: 0)
        result = 31 * result + (urlContext?.hashCode() ?: 0)
        result = 31 * result + (fileSearch?.hashCode() ?: 0)
        return result
    }
}

class ToolBuilder {
    private val functionDeclarations: MutableList<FunctionDeclaration> = mutableListOf()
    private var googleSearch: GoogleSearch? = null
    private var codeExecution: CodeExecution? = null
    private var urlContext: UrlContext? = null
    private var fileSearch: FileSearchTool? = null

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

    fun fileSearch(init: FileSearchToolBuilder.() -> Unit) {
        this.fileSearch = FileSearchToolBuilder().apply(init).build()
    }

    fun build() =
        Tool(
            functionDeclarations = if (functionDeclarations.isEmpty()) null else functionDeclarations.toTypedArray(),
            googleSearch = googleSearch,
            codeExecution = codeExecution,
            urlContext = urlContext,
            fileSearch = fileSearch,
        )
}

class FileSearchToolBuilder {
    private var fileSearchStoreNames: MutableList<String> = mutableListOf()
    private var metadataFilter: String? = null

    fun fileSearchStoreName(name: String) {
        fileSearchStoreNames.add(name)
    }

    fun metadataFilter(filter: String) {
        this.metadataFilter = filter
    }

    fun build() =
        FileSearchTool(
            fileSearchStoreNames = fileSearchStoreNames,
            metadataFilter = metadataFilter,
        )
}

fun tool(init: ToolBuilder.() -> Unit): Tool = ToolBuilder().apply(init).build()
