package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

/**
 * Represents the logprobs result.
 *
 * @property topCandidates The top candidates.
 * @property chosenCandidates The chosen candidates.
 * @property logProbabilitySum Optional. The sum of the chosen candidates' log probabilities.
 */
@Serializable
data class LogprobsResult(
    val topCandidates: List<TopCandidates>,
    val chosenCandidates: List<LogprobCandidate>,
    val logProbabilitySum: Double? = null,
)
