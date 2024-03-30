package io.github.ugaikit

import kotlinx.serialization.Serializable

@Serializable
data class Candidate(
    val content: io.github.ugaikit.Content,
    val finishReason: String,
    val index: Int,
    val safetyRatings: List<io.github.ugaikit.SafetyRating>,
)
