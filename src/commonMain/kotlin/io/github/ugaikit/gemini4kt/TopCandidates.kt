package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

@Serializable
data class TopCandidates(
    val candidates: List<Candidate>,
)
