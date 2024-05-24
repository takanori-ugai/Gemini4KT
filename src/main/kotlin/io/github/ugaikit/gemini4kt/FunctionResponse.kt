package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

/**
 * Represents the response from a function call, including the function's name
 * and the arguments or values returned by the function.
 *
 * @property name The name of the function that was called. This helps in
 * identifying which function the response corresponds to, especially when
 * multiple function calls are made asynchronously.
 * @property args A list of strings representing the values or arguments returned
 * by the function. These are typically the result of the function's execution,
 * formatted as strings for uniformity and ease of handling.
 */
@Serializable
data class FunctionResponse(
    val name: String,
    val args: List<String> = emptyList(),
    val response: Map<String, String>,
)
