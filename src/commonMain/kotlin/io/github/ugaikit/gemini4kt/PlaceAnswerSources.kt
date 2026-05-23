package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Collection of review snippets providing answers about a place in Google Maps.
 *
 * @property reviewSnippets Snippets of reviews.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class PlaceAnswerSources(
    val reviewSnippets: List<ReviewSnippet> = emptyList(),
)
