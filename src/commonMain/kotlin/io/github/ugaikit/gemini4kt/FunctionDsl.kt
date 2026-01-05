package io.github.ugaikit.gemini4kt

import kotlin.reflect.KFunction

/**
 * Represents the gemini function.
 *
 * @property description The description.
 */
@Target(AnnotationTarget.FUNCTION)
annotation class GeminiFunction(
    val description: String,
)

/**
 * Represents the gemini parameter.
 *
 * @property description The description.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class GeminiParameter(
    val description: String,
)

/**
 * Handles build function declaration.
 *
 * @param function The function.
 */
expect fun buildFunctionDeclaration(function: KFunction<*>): FunctionDeclaration
