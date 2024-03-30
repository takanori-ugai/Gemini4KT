package io.github.uaikit

import kotlinx.serialization.Serializable

@Serializable
data class EmbedContentRequest(
    val content: io.github.uaikit.Content,
    val model: String,
    val taskType: io.github.uaikit.TaskType? = null,
    val title: String? = null,
)
