package io.github.ugaikit.gemini4kt.interaction

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/** Optional Omni task override. Omit [task] to let the model infer the task from the prompt. */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class InteractionVideoConfig(
    val task: InteractionVideoTask? = null,
)

/** Video generation, editing, and extension tasks accepted by the Interactions API. */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
enum class InteractionVideoTask {
    @SerialName("text_to_video")
    TEXT_TO_VIDEO,

    @SerialName("image_to_video")
    IMAGE_TO_VIDEO,

    @SerialName("reference_to_video")
    REFERENCE_TO_VIDEO,

    @SerialName("edit")
    EDIT,

    @SerialName("extend")
    EXTEND,
}
