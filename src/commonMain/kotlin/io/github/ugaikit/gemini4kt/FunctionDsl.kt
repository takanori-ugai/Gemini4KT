package io.github.ugaikit.gemini4kt

import kotlin.reflect.KFunction

@Target(AnnotationTarget.FUNCTION)
annotation class GeminiFunction(
    val description: String,
)

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class GeminiParameter(
    val description: String,
)

expect fun buildFunctionDeclaration(function: KFunction<*>): FunctionDeclaration
