package io.github.ugaikit.gemini4kt

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Computer Use tool type.
 *
 * @property environment The environment being operated.
 * @property excludedPredefinedFunctions Predefined functions to explicitly exclude.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class ComputerUse(
    val environment: Environment? = null,
    @SerialName("excluded_predefined_functions")
    val excludedPredefinedFunctions: Array<String>? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ComputerUse

        if (environment != other.environment) return false
        if (!excludedPredefinedFunctions.contentEqualsNullable(other.excludedPredefinedFunctions)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = environment?.hashCode() ?: 0
        result = 31 * result + (excludedPredefinedFunctions?.contentHashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "ComputerUse(environment=$environment, " +
            "excludedPredefinedFunctions=${excludedPredefinedFunctions?.contentToString()})"
}

/**
 * Represents the computer use builder.
 */
class ComputerUseBuilder {
    /**
     * Holds the environment.
     */
    var environment: Environment? = null

    /**
     * Holds the excluded predefined functions.
     */
    private val excludedPredefinedFunctions: MutableList<String> = mutableListOf()

    /**
     * Handles excluded predefined function.
     *
     * @param name The predefined function name to exclude.
     */
    fun excludedPredefinedFunction(name: String) {
        excludedPredefinedFunctions.add(name)
    }

    /**
     * Handles excluded predefined functions.
     *
     * @param names The predefined function names to exclude.
     */
    fun excludedPredefinedFunctions(vararg names: String) {
        excludedPredefinedFunctions.addAll(names)
    }

    /**
     * Handles build.
     */
    fun build() =
        ComputerUse(
            environment = environment,
            excludedPredefinedFunctions =
                if (excludedPredefinedFunctions.isEmpty()) {
                    null
                } else {
                    excludedPredefinedFunctions.toTypedArray()
                },
        )
}

/**
 * Handles computer use config.
 *
 * @param init The init.
 */
fun computerUse(init: ComputerUseBuilder.() -> Unit): ComputerUse = ComputerUseBuilder().apply(init).build()
