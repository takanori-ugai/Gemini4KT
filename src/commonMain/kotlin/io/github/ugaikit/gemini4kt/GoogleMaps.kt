package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * The GoogleMaps Tool that provides geospatial context for the user's query.
 *
 * @property enableWidget Whether to return a widget context token in the GroundingMetadata of the response.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class GoogleMaps(
    val enableWidget: Boolean? = null,
)
