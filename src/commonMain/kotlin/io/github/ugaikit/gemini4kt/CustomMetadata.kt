package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * User provided metadata about the GroundingFact.
 *
 * @property key The key of the metadata.
 * @property stringValue The string value of the metadata.
 * @property stringListValue A list of string values for the metadata.
 * @property numericValue The numeric value of the metadata.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class CustomMetadata(
    val key: String,
    val stringValue: String? = null,
    val stringListValue: StringList? = null,
    val numericValue: Double? = null,
)
