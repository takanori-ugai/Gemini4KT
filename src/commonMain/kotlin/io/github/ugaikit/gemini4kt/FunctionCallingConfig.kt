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
@JsExport
data class FunctionCallingConfig(
    val mode: Mode,
    val allowedFunctionNames: Array<String>,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as FunctionCallingConfig

        if (mode != other.mode) return false
        if (!allowedFunctionNames.contentEquals(other.allowedFunctionNames)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = mode.hashCode()
        result = 31 * result + allowedFunctionNames.contentHashCode()
        return result
    }
}

class FunctionCallingConfigBuilder {
    var mode: Mode = Mode.AUTO
    private val allowedFunctionNames: MutableList<String> = mutableListOf()

    fun allowFunction(name: String) {
        allowedFunctionNames.add(name)
    }

    fun build() = FunctionCallingConfig(mode, allowedFunctionNames.toTypedArray())
}

fun functionCallingConfig(init: FunctionCallingConfigBuilder.() -> Unit): FunctionCallingConfig = FunctionCallingConfigBuilder().apply(init).build()
