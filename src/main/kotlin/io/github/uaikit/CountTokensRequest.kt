package io.github.uaikit

import kotlinx.serialization.Serializable

@Serializable
data class CountTokensRequest(
    val contents: List<io.github.uaikit.Content>,
)
