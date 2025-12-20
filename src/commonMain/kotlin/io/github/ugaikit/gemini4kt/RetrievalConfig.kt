package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Represents the retrieval config.
 *
 * @property latLng The lat lng.
 * @property languageCode The language code.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class RetrievalConfig(
    val latLng: LatLng? = null,
    val languageCode: String? = null,
)

/**
 * Represents the retrieval config builder.
 */
class RetrievalConfigBuilder {
    /**
     * Holds the lat lng.
     */
    var latLng: LatLng? = null

    /**
     * Holds the language code.
     */
    var languageCode: String? = null

    /**
     * Handles build.
     */
    fun build(): RetrievalConfig =
        RetrievalConfig(
            latLng = latLng,
            languageCode = languageCode,
        )
}

/**
 * Handles retrieval config.
 *
 * @param init The init.
 */
fun retrievalConfig(init: RetrievalConfigBuilder.() -> Unit): RetrievalConfig = RetrievalConfigBuilder().apply(init).build()
