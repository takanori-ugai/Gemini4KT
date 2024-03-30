package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

@Serializable
data class EmbedResponse(val embedding: io.github.ugaikit.gemini4kt.Values)
