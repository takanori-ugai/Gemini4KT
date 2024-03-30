package io.github.uaikit

import kotlinx.serialization.Serializable

@Serializable
data class Candidate(
    val content: io.github.uaikit.Content,
    val finishReason: String,
    val index: Int,
    val safetyRatings: List<io.github.uaikit.SafetyRating>,
)
