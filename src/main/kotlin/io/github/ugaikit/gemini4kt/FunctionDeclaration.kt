package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

/**
 * Describes a function, including its name, a description of its purpose or
 * behavior, and a schema defining its parameters.
 *
 * @property name The unique identifier or name of the function. This is used to
 * invoke or reference the function within a system.
 * @property description A human-readable explanation of what the function does,
 * including any important details about its behavior or effects.
 * @property parameters A [Schema] object that describes the structure and types
 * of the parameters that the function accepts. This ensures that callers can
 * construct valid calls to the function by adhering to this schema.
 */
@Serializable
data class FunctionDeclaration(
    val name: String,
    val description: String,
    val parameters: Schema,
)

class FunctionDeclarationBuilder {
    var name: String = ""
    var description: String = ""
    private var parameters: Schema? = null

    fun parameters(init: SchemaBuilder.() -> Unit) {
        parameters = SchemaBuilder().apply(init).build()
    }

    fun build() = FunctionDeclaration(name, description, parameters ?: throw IllegalStateException("Parameters must be initialized"))
}

fun functionDeclaration(init: FunctionDeclarationBuilder.() -> Unit): FunctionDeclaration = FunctionDeclarationBuilder().apply(init).build()
