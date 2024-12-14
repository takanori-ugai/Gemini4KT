package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

@Serializable
data class LogprobsResult(
    val topCandidates: List<TopCandidates>,
    val chosenCandidates: List<Candidate>,
)
