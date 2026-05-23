package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Represents a time interval.
 *
 * @property startTime Inclusive start of the interval.
 * @property endTime Exclusive end of the interval.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class Interval(
    val startTime: String? = null,
    val endTime: String? = null,
)
