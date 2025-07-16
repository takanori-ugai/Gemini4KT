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
data class ToolConfig(
    val functionCallingConfig: FunctionCallingConfig,
)

class ToolConfigBuilder {
    private var functionCallingConfig: FunctionCallingConfig? = null

    fun functionCallingConfig(init: FunctionCallingConfigBuilder.() -> Unit) {
        functionCallingConfig = FunctionCallingConfigBuilder().apply(init).build()
    }

    fun build() = ToolConfig(functionCallingConfig ?: throw IllegalStateException("FunctionCallingConfig must be initialized"))
}

fun toolConfig(init: ToolConfigBuilder.() -> Unit): ToolConfig = ToolConfigBuilder().apply(init).build()
