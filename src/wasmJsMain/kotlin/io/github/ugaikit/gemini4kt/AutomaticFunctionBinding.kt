package io.github.ugaikit.gemini4kt

import kotlin.reflect.KFunction

actual fun buildAutomaticFunctionBinding(functions: Array<out KFunction<*>>): AutomaticFunctionBinding =
    throw UnsupportedOperationException(
        "Direct Kotlin function binding is not supported in wasm.",
    )
