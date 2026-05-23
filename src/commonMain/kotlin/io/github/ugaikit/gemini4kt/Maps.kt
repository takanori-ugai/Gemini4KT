package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * A grounding chunk from Google Maps.
 *
 * @property uri URI reference of the place.
 * @property title Title of the place.
 * @property text Text description of the place answer.
 * @property placeId The ID of the place, in places/{placeId} format.
 * @property placeAnswerSources Sources that provide answers about features of the place.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class Maps(
    val uri: String? = null,
    val title: String? = null,
    val text: String? = null,
    val placeId: String? = null,
    val placeAnswerSources: PlaceAnswerSources? = null,
)
