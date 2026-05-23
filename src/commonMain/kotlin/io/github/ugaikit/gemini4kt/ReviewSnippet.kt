package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Encapsulates a snippet of a user review.
 *
 * @property reviewId The ID of the review snippet.
 * @property googleMapsUri A link that corresponds to the user review on Google Maps.
 * @property title Title of the review.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class ReviewSnippet(
    val reviewId: String? = null,
    val googleMapsUri: String? = null,
    val title: String? = null,
)
