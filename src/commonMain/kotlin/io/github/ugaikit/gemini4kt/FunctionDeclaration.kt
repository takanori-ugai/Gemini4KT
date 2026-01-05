package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

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
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class FunctionDeclaration(
    val name: String,
    val description: String,
    val parameters: Schema,
)

/**
 * Represents the function declaration builder.
 */
class FunctionDeclarationBuilder {
    /**
     * Holds the name.
     */
    lateinit var name: String

    /**
     * Holds the description.
     */
    lateinit var description: String

    /**
     * Holds the parameters.
     */
    private var parameters: Schema? = null

    /**
     * Handles parameters.
     *
     * @param init The init.
     */
    fun parameters(init: SchemaBuilder.() -> Unit) {
        parameters = SchemaBuilder().apply(init).build()
    }

    /**
     * Handles build.
     */
    fun build() =
        FunctionDeclaration(
            name,
            description,
            parameters ?: error("Parameters must be initialized"),
        )
}

/**
 * Handles function declaration.
 *
 * @param init The init.
 */
fun functionDeclaration(init: FunctionDeclarationBuilder.() -> Unit): FunctionDeclaration =
    FunctionDeclarationBuilder()
        .apply(init)
        .build()
