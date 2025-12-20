package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Represents a tool that encapsulates function declarations, providing a structured
 * way to access the functionalities declared.
 *
 * @property functionDeclarations A [FunctionDeclaration] object containing the
 * details of the functions declared by this tool.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class ToolConfig(
    val functionCallingConfig: FunctionCallingConfig,
    val retrievalConfig: RetrievalConfig? = null,
)

/**
 * Represents the tool config builder.
 */
class ToolConfigBuilder {
    /**
     * Holds the function calling config.
     */
    private var functionCallingConfig: FunctionCallingConfig? = null

    /**
     * Holds the retrieval config.
     */
    var retrievalConfig: RetrievalConfig? = null

    /**
     * Handles function calling config.
     *
     * @param init The init.
     */
    fun functionCallingConfig(init: FunctionCallingConfigBuilder.() -> Unit) {
        functionCallingConfig = FunctionCallingConfigBuilder().apply(init).build()
    }

    /**
     * Handles build.
     */
    fun build() =
        ToolConfig(
            functionCallingConfig = functionCallingConfig ?: error("FunctionCallingConfig must be initialized"),
            retrievalConfig = retrievalConfig,
        )
}

/**
 * Handles tool config.
 *
 * @param init The init.
 */
fun toolConfig(init: ToolConfigBuilder.() -> Unit): ToolConfig = ToolConfigBuilder().apply(init).build()
