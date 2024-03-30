package io.github.uaikit

import kotlinx.serialization.Serializable

@Serializable
data class ModelCollection(
    val models: List<Model>,
)
