package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

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
@Serializable
data class FunctionCall(
    val name: String,
    val args: Map<String, String>,
)
