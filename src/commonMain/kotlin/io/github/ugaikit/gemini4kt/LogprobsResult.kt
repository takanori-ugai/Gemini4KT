package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

@Serializable
@JsExport
data class LogprobsResult(
    val topCandidates: Array<TopCandidates>,
    val chosenCandidates: Array<Candidate>,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as LogprobsResult

        if (!topCandidates.contentEquals(other.topCandidates)) return false
        if (!chosenCandidates.contentEquals(other.chosenCandidates)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = topCandidates.contentHashCode()
        result = 31 * result + chosenCandidates.contentHashCode()
        return result
    }
}
