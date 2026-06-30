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
)

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
    private var excludedPredefinedFunctions: MutableList<String> = mutableListOf()

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
