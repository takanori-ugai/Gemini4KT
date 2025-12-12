package io.github.ugaikit.gemini4kt

import kotlin.reflect.KFunction

actual fun buildFunctionDeclaration(function: KFunction<*>): FunctionDeclaration = throw UnsupportedOperationException("buildFunctionDeclaration is not supported in native")
