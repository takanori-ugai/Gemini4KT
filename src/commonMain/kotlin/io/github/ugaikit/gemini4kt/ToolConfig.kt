package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Represents the configuration of tools for the model.
 *
 * @property functionCallingConfig Configuration for function calling.
 * @property retrievalConfig Configuration for retrieval.
 * @property includeServerSideToolInvocations True if the model should return server side tool invocations.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class ToolConfig(
    val functionCallingConfig: FunctionCallingConfig? = null,
    val retrievalConfig: RetrievalConfig? = null,
    val includeServerSideToolInvocations: Boolean? = null,
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
     * Holds the include server side tool invocations flag.
     */
    var includeServerSideToolInvocations: Boolean? = null

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
            functionCallingConfig = functionCallingConfig,
            retrievalConfig = retrievalConfig,
            includeServerSideToolInvocations = includeServerSideToolInvocations,
        )
}

/**
 * Handles tool config.
 *
 * @param init The init.
 */
fun toolConfig(init: ToolConfigBuilder.() -> Unit): ToolConfig = ToolConfigBuilder().apply(init).build()
