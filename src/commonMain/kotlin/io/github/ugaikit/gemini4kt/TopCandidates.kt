package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

/**
 * Represents the top candidates.
 *
 * @property candidates The candidates.
 */
@Serializable
data class TopCandidates(
    val candidates: List<LogprobCandidate>,
)
