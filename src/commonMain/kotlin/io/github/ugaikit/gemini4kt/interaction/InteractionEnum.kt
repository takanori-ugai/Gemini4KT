package io.github.ugaikit.gemini4kt.interaction

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Lifecycle status values for an interaction.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
enum class InteractionStatus {
    @SerialName("queued")
    QUEUED,

    @SerialName("in_progress")
    IN_PROGRESS,

    @SerialName("requires_action")
    REQUIRES_ACTION,

    @SerialName("completed")
    COMPLETED,

    @SerialName("failed")
    FAILED,

    @SerialName("cancelled")
    CANCELLED,

    @SerialName("requires_continuation")
    REQUIRES_CONTINUATION,

    @SerialName("incomplete")
    INCOMPLETE,

    @SerialName("idle")
    IDLE,
}

/**
 * Reasoning depth presets for model thinking.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
enum class ThinkingLevel {
    @SerialName("minimal")
    MINIMAL,

    @SerialName("low")
    LOW,

    @SerialName("medium")
    MEDIUM,

    @SerialName("high")
    HIGH,

    @SerialName("xhigh")
    XHIGH,
}

/**
 * Controls whether and how model reasoning summaries are returned.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
enum class ThinkingSummaries {
    @SerialName("auto")
    AUTO,

    @SerialName("none")
    NONE,
}

/**
 * Output modality types supported by interaction responses.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
enum class InteractionResponseModality {
    @SerialName("text")
    TEXT,

    @SerialName("image")
    IMAGE,

    @SerialName("audio")
    AUDIO,
}

/**
 * Video timeline processing strategy for interaction video inputs.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
enum class InteractionVideoProcessing {
    @SerialName("static")
    STATIC,

    @SerialName("agentic")
    AGENTIC,
}
