package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Represents the lat lng.
 *
 * @property latitude The latitude.
 * @property longitude The longitude.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class LatLng(
    val latitude: Double,
    val longitude: Double,
)
