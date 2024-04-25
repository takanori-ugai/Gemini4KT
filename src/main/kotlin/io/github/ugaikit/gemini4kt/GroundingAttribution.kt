package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

@Serializable
data class GroundingAttribution(
    val sourceId: AttributionSourceId,
    val content: Content,
)
