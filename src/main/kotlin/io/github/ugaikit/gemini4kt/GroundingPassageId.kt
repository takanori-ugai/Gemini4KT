package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

@Serializable
data class GroundingPassageId(
    val passageId: String,
    val partIndex: Int,
)
