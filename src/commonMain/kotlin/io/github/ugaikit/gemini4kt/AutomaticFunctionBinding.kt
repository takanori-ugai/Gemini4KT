package io.github.ugaikit.gemini4kt

import kotlin.reflect.KFunction

/**
 * Prepared automatic function-calling metadata for a set of Kotlin functions.
 *
 * @property tools Generated tool declarations for the provided functions.
 * @property handlers Runtime handlers that execute the provided functions.
 */
data class AutomaticFunctionBinding(
    val tools: Array<Tool>,
    val handlers: Map<String, suspend (FunctionCall) -> FunctionResponse>,
)

/**
 * Builds runtime function-calling bindings for Kotlin function references.
 */
expect fun buildAutomaticFunctionBinding(functions: Array<out KFunction<*>>): AutomaticFunctionBinding
