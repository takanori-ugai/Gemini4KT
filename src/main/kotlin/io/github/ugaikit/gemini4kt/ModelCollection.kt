package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

@Serializable
data class ModelCollection(
    val models: List<Model>,
)
