package io.github.ugaikit

import kotlinx.serialization.Serializable

@Serializable
data class CountTokensRequest(
    val contents: List<io.github.ugaikit.Content>,
)
