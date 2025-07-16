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

class ToolBuilder {
    private val functionDeclarations: MutableList<FunctionDeclaration> = mutableListOf()

    fun functionDeclaration(init: FunctionDeclarationBuilder.() -> Unit) {
        functionDeclarations.add(FunctionDeclarationBuilder().apply(init).build())
    }

    fun build() = Tool(functionDeclarations)
}

fun tool(init: ToolBuilder.() -> Unit): Tool = ToolBuilder().apply(init).build()
