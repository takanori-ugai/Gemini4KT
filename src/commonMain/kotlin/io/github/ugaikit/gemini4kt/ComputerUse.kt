package io.github.ugaikit.gemini4kt

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
    val excludedPredefinedFunctions: Array<String>? = null,
)
