package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * A list of string values.
 *
 * @property values The string values of the list.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class StringList(
    val values: List<String> = emptyList(),
)
