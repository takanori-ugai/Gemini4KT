package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

@Serializable
data class LatLng(
    val latitude: Double,
    val longitude: Double,
)
