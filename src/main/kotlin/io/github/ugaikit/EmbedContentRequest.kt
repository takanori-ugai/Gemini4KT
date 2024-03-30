package io.github.ugaikit

import kotlinx.serialization.Serializable

@Serializable
data class EmbedContentRequest(
    val content: io.github.ugaikit.Content,
    val model: String,
    val taskType: io.github.ugaikit.TaskType? = null,
    val title: String? = null,
)
