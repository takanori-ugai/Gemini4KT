package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

@Serializable
data class Candidate(
    val content: io.github.ugaikit.gemini4kt.Content,
    val finishReason: String,
    val index: Int,
    val safetyRatings: List<io.github.ugaikit.gemini4kt.SafetyRating>,
)
