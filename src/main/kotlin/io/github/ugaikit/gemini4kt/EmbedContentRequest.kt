package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

@Serializable
data class EmbedContentRequest(
    val content: io.github.ugaikit.gemini4kt.Content,
    val model: String,
    val taskType: io.github.ugaikit.gemini4kt.TaskType? = null,
    val title: String? = null,
)
