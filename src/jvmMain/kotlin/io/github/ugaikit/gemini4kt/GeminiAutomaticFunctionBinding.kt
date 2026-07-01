package io.github.ugaikit.gemini4kt

import kotlin.reflect.KFunction

/**
 * Executes automatic function calling using direct Kotlin function references.
 *
 * This is available on JVM-based targets where reflection binding is supported.
 */
suspend fun Gemini.generateContent(
    request: GenerateContentRequest,
    vararg functions: KFunction<*>,
    model: String = "gemini-flash-lite-latest",
    maxIterations: Int = 8,
): GenerateContentResponse {
    require(functions.isNotEmpty()) { "At least one function is required." }
    val binding = buildAutomaticFunctionBinding(functions)
    val mergedRequest = request.copy(tools = request.tools + binding.tools)
    return generateContent(
        request = mergedRequest,
        functionHandlers = binding.handlers,
        model = model,
        maxIterations = maxIterations,
    )
}
