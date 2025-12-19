package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class RetrievalConfig(
    val latLng: LatLng? = null,
    val languageCode: String? = null,
)

class RetrievalConfigBuilder {
    var latLng: LatLng? = null
    var languageCode: String? = null

    fun build(): RetrievalConfig =
        RetrievalConfig(
            latLng = latLng,
            languageCode = languageCode,
        )
}

fun retrievalConfig(init: RetrievalConfigBuilder.() -> Unit): RetrievalConfig = RetrievalConfigBuilder().apply(init).build()
