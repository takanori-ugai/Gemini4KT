package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Represents a candidate token and its probability score in logprobs results.
 *
 * @property token The candidate's token string value.
 * @property tokenId The candidate's token id value.
 * @property logProbability The candidate's log probability.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class LogprobCandidate(
    val token: String? = null,
    val tokenId: Int? = null,
    val logProbability: Double? = null,
)
