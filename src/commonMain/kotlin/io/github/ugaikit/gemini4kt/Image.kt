package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Chunk from image search.
 *
 * @property sourceUri The web page URI for attribution.
 * @property imageUri The image asset URL.
 * @property title The title of the web page that the image is from.
 * @property domain The root domain of the web page that the image is from.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class Image(
    val sourceUri: String? = null,
    val imageUri: String? = null,
    val title: String? = null,
    val domain: String? = null,
)
