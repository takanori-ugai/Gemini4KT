package io.github.uaikit

import kotlinx.serialization.Serializable

@Serializable
data class EmbedResponse(val embedding: io.github.uaikit.Values)
