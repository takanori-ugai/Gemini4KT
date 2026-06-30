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
 * @property googleSearchRetrieval A [GoogleSearchRetrieval] object representing Google Search grounding.
 * @property computerUse A [ComputerUse] object representing computer use capability.
 * @property mcpServers An array of [McpServer] configurations.
 * @property googleMaps A [GoogleMaps] tool configuration.
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
    val googleSearchRetrieval: GoogleSearchRetrieval? = null,
    @SerialName("computer_use")
    val computerUse: ComputerUse? = null,
    val mcpServers: Array<McpServer>? = null,
    val googleMaps: GoogleMaps? = null,
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
     * Holds the google search retrieval.
     */
    private var googleSearchRetrieval: GoogleSearchRetrieval? = null

    /**
     * Holds the computer use config.
     */
    private var computerUse: ComputerUse? = null

    /**
     * Holds the mcp servers list.
     */
    private val mcpServers: MutableList<McpServer> = mutableListOf()

    /**
     * Holds the google maps config.
     */
    private var googleMaps: GoogleMaps? = null

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
    fun googleSearch(init: (GoogleSearchBuilder.() -> Unit)? = null) {
        this.googleSearch = if (init != null) GoogleSearchBuilder().apply(init).build() else GoogleSearch()
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
     * Handles google search retrieval.
     *
     * @param retrieval Grounding retrieval config.
     */
    fun googleSearchRetrieval(retrieval: GoogleSearchRetrieval) {
        this.googleSearchRetrieval = retrieval
    }

    /**
     * Handles computer use.
     *
     * @param computerUse The computer use config.
     */
    fun computerUse(computerUse: ComputerUse) {
        this.computerUse = computerUse
    }

    /**
     * Handles computer use.
     *
     * @param init The init.
     */
    fun computerUse(init: ComputerUseBuilder.() -> Unit) {
        this.computerUse = ComputerUseBuilder().apply(init).build()
    }

    /**
     * Handles adding an MCP server.
     *
     * @param mcpServer The mcp server.
     */
    fun mcpServer(mcpServer: McpServer) {
        mcpServers.add(mcpServer)
    }

    /**
     * Handles google maps.
     *
     * @param googleMaps The google maps config.
     */
    fun googleMaps(googleMaps: GoogleMaps) {
        this.googleMaps = googleMaps
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
            googleSearchRetrieval = googleSearchRetrieval,
            computerUse = computerUse,
            mcpServers = if (mcpServers.isEmpty()) null else mcpServers.toTypedArray(),
            googleMaps = googleMaps,
        ).also {
            val activeToolCount =
                listOfNotNull(
                    if (functionDeclarations.isEmpty()) null else functionDeclarations,
                    googleSearch,
                    codeExecution,
                    urlContext,
                    fileSearch,
                    googleSearchRetrieval,
                    computerUse,
                    if (mcpServers.isEmpty()) null else mcpServers,
                    googleMaps,
                ).size
            require(activeToolCount <= 1) {
                "Tool builders support only one primary tool configuration at a time."
            }
        }
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
