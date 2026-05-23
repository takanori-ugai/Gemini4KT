package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * The status of the underlying model.
 *
 * @property modelStage The stage of the underlying model.
 * @property retirementTime The time at which the model will be retired.
 * @property message A message explaining the model status.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class ModelStatus(
    val modelStage: ModelStage? = null,
    val retirementTime: String? = null,
    val message: String? = null,
)
