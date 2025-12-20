package io.github.ugaikit.gemini4kt

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

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
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
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
)

/**
 * Represents the tool builder.
 */
class ToolBuilder {
    /**
     * Holds the function declarations.
     */
    private val functionDeclarations: MutableList<FunctionDeclaration> = mutableListOf()

    /**
     * Holds the google search.
     */
    private var googleSearch: GoogleSearch? = null

    /**
     * Holds the code execution.
     */
    private var codeExecution: CodeExecution? = null

    /**
     * Holds the url context.
     */
    private var urlContext: UrlContext? = null

    /**
     * Holds the file search.
     */
    private var fileSearch: FileSearchTool? = null

    /**
     * Handles function declaration.
     *
     * @param init The init.
     */
    fun functionDeclaration(init: FunctionDeclarationBuilder.() -> Unit) {
        functionDeclarations.add(FunctionDeclarationBuilder().apply(init).build())
    }

    /**
     * Handles google search.
     */
    fun googleSearch() {
        this.googleSearch = GoogleSearch()
    }

    /**
     * Handles code execution.
     */
    fun codeExecution() {
        this.codeExecution = CodeExecution()
    }

    /**
     * Handles url context.
     */
    fun urlContext() {
        this.urlContext = UrlContext()
    }

    /**
     * Handles file search.
     *
     * @param init The init.
     */
    fun fileSearch(init: FileSearchToolBuilder.() -> Unit) {
        this.fileSearch = FileSearchToolBuilder().apply(init).build()
    }

    /**
     * Handles build.
     */
    fun build() =
        Tool(
            functionDeclarations = functionDeclarations.toTypedArray(),
            googleSearch = googleSearch,
            codeExecution = codeExecution,
            urlContext = urlContext,
            fileSearch = fileSearch,
        )
}

/**
 * Represents the file search tool builder.
 */
class FileSearchToolBuilder {
    /**
     * Holds the file search store names.
     */
    private var fileSearchStoreNames: MutableList<String> = mutableListOf()

    /**
     * Holds the metadata filter.
     */
    private var metadataFilter: String? = null

    /**
     * Handles file search store name.
     *
     * @param name The name.
     */
    fun fileSearchStoreName(name: String) {
        fileSearchStoreNames.add(name)
    }

    /**
     * Handles metadata filter.
     *
     * @param filter The filter.
     */
    fun metadataFilter(filter: String) {
        this.metadataFilter = filter
    }

    /**
     * Handles build.
     */
    fun build() =
        FileSearchTool(
            fileSearchStoreNames = if (fileSearchStoreNames.isEmpty()) null else fileSearchStoreNames.toTypedArray(),
            metadataFilter = metadataFilter,
        )
}

/**
 * Handles tool.
 *
 * @param init The init.
 */
fun tool(init: ToolBuilder.() -> Unit): Tool = ToolBuilder().apply(init).build()
