package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

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
@Serializable
data class FunctionCallingConfig(
    val mode: Mode,
    val allowedFunctionNames: List<String>,
)
