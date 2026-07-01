package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Configures how function calls are handled within a certain context, specifying
 * the mode of operation and restrictions on which functions can be called.
 *
 * @property mode The [Mode] of operation for function calling, determining how
 * function calls are processed or restricted. This could dictate whether
 * automatic, manual, or no function calls are allowed.
 * @property allowedFunctionNames A list of strings representing the names of
 * functions that are permitted to be called. This serves as a whitelist,
 * ensuring only specified functions can be executed, enhancing security and
 * control over the execution environment.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class FunctionCallingConfig(
    val mode: Mode,
    val allowedFunctionNames: Array<String>,
)

/**
 * Represents the function calling config builder.
 */
class FunctionCallingConfigBuilder {
    /**
     * Holds the mode.
     */
    var mode: Mode = Mode.AUTO

    /**
     * Holds the allowed function names.
     */
    private val allowedFunctionNames: MutableList<String> = mutableListOf()

    /**
     * Handles allow function.
     *
     * @param name The name.
     */
    fun allowFunction(name: String) {
        allowedFunctionNames.add(name)
    }

    /**
     * Handles build.
     */
    fun build(): FunctionCallingConfig {
        val allowedNames = allowedFunctionNames.toTypedArray()
        when (mode) {
            Mode.ANY, Mode.VALIDATED ->
                require(allowedNames.isNotEmpty()) {
                    "allowedFunctionNames must not be empty when mode is $mode."
                }
            Mode.AUTO, Mode.NONE, Mode.MODE_UNSPECIFIED ->
                require(allowedNames.isEmpty()) {
                    "allowedFunctionNames must be empty when mode is $mode."
                }
        }
        return FunctionCallingConfig(mode, allowedNames)
    }
}

/**
 * Handles function calling config.
 *
 * @param init The init.
 */
fun functionCallingConfig(init: FunctionCallingConfigBuilder.() -> Unit): FunctionCallingConfig =
    FunctionCallingConfigBuilder()
        .apply(init)
        .build()
