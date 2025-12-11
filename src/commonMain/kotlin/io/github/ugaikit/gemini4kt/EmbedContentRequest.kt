package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

/**
 * Represents a request to embed content using a specified model, optionally
 * including additional parameters such as task type and title.
 *
 * @property content The [Content] to be embedded. This encapsulates the actual
 * data or text that needs to be processed by the embedding model.
 * @property model A string specifying the model to use for embedding. This
 * identifies which pre-trained model or algorithm should process the content.
 * @property taskType An optional [TaskType] indicating the type of embedding task
 * being requested. This can influence how the model processes the content.
 * @property title An optional string providing a title or name for the embedding
 * task. This can be useful for identifying the request or its purpose in logs or
 * results.
 */
@Serializable
data class EmbedContentRequest(
    val content: Content,
    val model: String,
    val taskType: TaskType? = null,
    val title: String? = null,
)
