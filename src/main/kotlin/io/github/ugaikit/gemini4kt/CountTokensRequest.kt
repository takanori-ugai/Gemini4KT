package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

@Serializable
data class CountTokensRequest(
    val contents: List<io.github.ugaikit.gemini4kt.Content>,
)
