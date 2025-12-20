package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Represents a call to a function, specifying the function's name and the
 * arguments to be passed to it.
 *
 * @property name The name of the function being called. This should match the
 * function's identifier within its defining context or library.
 * @property args A list of strings representing the arguments to be passed to the
 * function. Each string in the list corresponds to an individual argument, and
 * the order of the strings represents the order in which arguments are passed.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class FunctionCall(
    val name: String,
    val args: Map<String, JsonElement>,
)

/**
 * Represents the function call builder.
 */
class FunctionCallBuilder {
    /**
     * Holds the name.
     */
    var name: String = ""

    /**
     * Holds the args.
     */
    private val args: MutableMap<String, JsonElement> = mutableMapOf()

    /**
     * Handles arg.
     *
     * @param key The key.
     * @param value The value.
     */
    fun arg(
        key: String,
        value: JsonElement,
    ) {
        args[key] = value
    }

    /**
     * Handles build.
     */
    fun build() = FunctionCall(name, args)
}

/**
 * Handles function call.
 *
 * @param init The init.
 */
fun functionCall(init: FunctionCallBuilder.() -> Unit): FunctionCall = FunctionCallBuilder().apply(init).build()
