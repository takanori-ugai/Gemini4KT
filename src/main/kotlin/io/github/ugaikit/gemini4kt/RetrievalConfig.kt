package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

@Serializable
data class RetrievalConfig(
    val latLng: LatLng? = null,
    val languageCode: String? = null,
)
