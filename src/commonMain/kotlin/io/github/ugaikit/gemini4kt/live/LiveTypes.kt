package io.github.ugaikit.gemini4kt.live

import io.github.ugaikit.gemini4kt.Content
import io.github.ugaikit.gemini4kt.FunctionCall
import io.github.ugaikit.gemini4kt.FunctionResponse
import io.github.ugaikit.gemini4kt.GenerationConfig
import io.github.ugaikit.gemini4kt.GroundingMetadata
import io.github.ugaikit.gemini4kt.Modality
import io.github.ugaikit.gemini4kt.SpeechConfig
import io.github.ugaikit.gemini4kt.Tool
import io.github.ugaikit.gemini4kt.UrlContextMetadata
import io.github.ugaikit.gemini4kt.UsageMetadata
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// --- Client Messages ---

/**
 * Message to be sent in the first (and only in the first) BidiGenerateContentClientMessage.
 * Contains configuration that will apply for the duration of the streaming RPC.
 */
@Serializable
data class BidiGenerateContentSetup(
    val model: String,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null,
    val tools: Array<Tool>? = null,
    val realtimeInputConfig: RealtimeInputConfig? = null,
    val sessionResumption: SessionResumptionConfig? = null,
    val contextWindowCompression: ContextWindowCompressionConfig? = null,
    val inputAudioTranscription: AudioTranscriptionConfig? = null,
    val outputAudioTranscription: AudioTranscriptionConfig? = null,
    val proactivity: ProactivityConfig? = null,
)

/**
 * Incremental update of the current conversation delivered from the client.
 */
@Serializable
data class BidiGenerateContentClientContent(
    val turns: List<Content>? = null,
    val turnComplete: Boolean? = null,
)

/**
 * User input that is sent in real time.
 */
@Serializable
data class BidiGenerateContentRealtimeInput(
    @SerialName("media")
    val media: Blob? = null,
    val mediaChunks: List<Blob>? = null,
    val audio: Blob? = null,
    val video: Blob? = null,
    val activityStart: ActivityStart? = null,
    val activityEnd: ActivityEnd? = null,
    val audioStreamEnd: Boolean? = null,
    val text: String? = null,
)

/**
 * Client generated response to a ToolCall received from the server.
 */
@Serializable
data class BidiGenerateContentToolResponse(
    val functionResponses: List<FunctionResponse>? = null,
)

/**
 * Wrapper for all client messages.
 * The JSON object must have exactly one of the fields.
 */
@Serializable
data class BidiGenerateContentClientMessage(
    val setup: BidiGenerateContentSetup? = null,
    val clientContent: BidiGenerateContentClientContent? = null,
    val realtimeInput: BidiGenerateContentRealtimeInput? = null,
    val toolResponse: BidiGenerateContentToolResponse? = null,
)

// --- Server Messages ---

/**
 * Response message for the BidiGenerateContent call.
 */
@Serializable
data class BidiGenerateContentServerMessage(
    val usageMetadata: UsageMetadata? = null,
    val setupComplete: BidiGenerateContentSetupComplete? = null,
    val serverContent: BidiGenerateContentServerContent? = null,
    val toolCall: BidiGenerateContentToolCall? = null,
    val toolCallCancellation: BidiGenerateContentToolCallCancellation? = null,
    val goAway: GoAway? = null,
    val sessionResumptionUpdate: SessionResumptionUpdate? = null,
)

/**
 * Represents the bidi generate content setup complete.
 */
@Serializable
class BidiGenerateContentSetupComplete

/**
 * Represents the bidi generate content server content.
 *
 * @property generationComplete The generation complete.
 * @property turnComplete The turn complete.
 * @property interrupted The interrupted.
 * @property groundingMetadata The grounding metadata.
 * @property inputTranscription The input transcription.
 * @property outputTranscription The output transcription.
 * @property urlContextMetadata The url context metadata.
 * @property modelTurn The model turn.
 */
@Serializable
data class BidiGenerateContentServerContent(
    val generationComplete: Boolean? = null,
    val turnComplete: Boolean? = null,
    val interrupted: Boolean? = null,
    val groundingMetadata: GroundingMetadata? = null,
    val inputTranscription: BidiGenerateContentTranscription? = null,
    val outputTranscription: BidiGenerateContentTranscription? = null,
    val urlContextMetadata: UrlContextMetadata? = null,
    val modelTurn: Content? = null,
)

/**
 * Represents the bidi generate content tool call.
 *
 * @property functionCalls The function calls.
 */
@Serializable
data class BidiGenerateContentToolCall(
    val functionCalls: List<FunctionCall>? = null,
)

/**
 * Represents the bidi generate content tool call cancellation.
 *
 * @property ids The ids.
 */
@Serializable
data class BidiGenerateContentToolCallCancellation(
    val ids: List<String>? = null,
)

/**
 * Represents the go away.
 *
 * @property timeLeft The time left.
 */
@Serializable
data class GoAway(
    /**
     * Holds the time left.
     */
    val timeLeft: String? = null, // Using String for Duration, e.g., "10s"
)

/**
 * Represents the session resumption update.
 *
 * @property newHandle The new handle.
 * @property resumable The resumable.
 */
@Serializable
data class SessionResumptionUpdate(
    val newHandle: String? = null,
    val resumable: Boolean? = null,
)

// --- Helper Types ---

/**
 * Represents the realtime input config.
 *
 * @property automaticActivityDetection The automatic activity detection.
 * @property activityHandling The activity handling.
 * @property turnCoverage The turn coverage.
 */
@Serializable
data class RealtimeInputConfig(
    val automaticActivityDetection: AutomaticActivityDetection? = null,
    val activityHandling: ActivityHandling? = null,
    val turnCoverage: TurnCoverage? = null,
)

/**
 * Represents the automatic activity detection.
 *
 * @property disabled The disabled.
 * @property startOfSpeechSensitivity The start of speech sensitivity.
 * @property prefixPaddingMs The prefix padding ms.
 * @property endOfSpeechSensitivity The end of speech sensitivity.
 * @property silenceDurationMs The silence duration ms.
 */
@Serializable
data class AutomaticActivityDetection(
    val disabled: Boolean? = null,
    val startOfSpeechSensitivity: StartSensitivity? = null,
    val prefixPaddingMs: Int? = null,
    val endOfSpeechSensitivity: EndSensitivity? = null,
    val silenceDurationMs: Int? = null,
)

/**
 * Represents the activity handling.
 */
@Serializable
enum class ActivityHandling {
    ACTIVITY_HANDLING_UNSPECIFIED,
    START_OF_ACTIVITY_INTERRUPTS,
    NO_INTERRUPTION,
}

/**
 * Represents the turn coverage.
 */
@Serializable
enum class TurnCoverage {
    TURN_COVERAGE_UNSPECIFIED,
    TURN_INCLUDES_ONLY_ACTIVITY,
    TURN_INCLUDES_ALL_INPUT,
}

/**
 * Represents the start sensitivity.
 */
@Serializable
enum class StartSensitivity {
    START_SENSITIVITY_UNSPECIFIED,
    START_SENSITIVITY_HIGH,
    START_SENSITIVITY_LOW,
}

/**
 * Represents the end sensitivity.
 */
@Serializable
enum class EndSensitivity {
    END_SENSITIVITY_UNSPECIFIED,
    END_SENSITIVITY_HIGH,
    END_SENSITIVITY_LOW,
}

/**
 * Represents the blob.
 *
 * @property mimeType The mime type.
 * @property data The data.
 */
@Serializable
data class Blob(
    val mimeType: String,
    /**
     * Holds the data.
     */
    val data: String, // Base64 encoded bytes
)

/**
 * Represents the activity start.
 */
@Serializable
class ActivityStart

/**
 * Represents the activity end.
 */
@Serializable
class ActivityEnd

/**
 * Represents the session resumption config.
 *
 * @property handle The handle.
 */
@Serializable
data class SessionResumptionConfig(
    val handle: String? = null,
)

/**
 * Represents the context window compression config.
 *
 * @property slidingWindow The sliding window.
 * @property triggerTokens The trigger tokens.
 */
@Serializable
data class ContextWindowCompressionConfig(
    val slidingWindow: SlidingWindow? = null,
    val triggerTokens: Long? = null,
)

/**
 * Represents the sliding window.
 *
 * @property targetTokens The target tokens.
 */
@Serializable
data class SlidingWindow(
    val targetTokens: Long? = null,
)

/**
 * Represents the audio transcription config.
 */
@Serializable
class AudioTranscriptionConfig

/**
 * Represents the proactivity config.
 *
 * @property proactiveAudio The proactive audio.
 */
@Serializable
data class ProactivityConfig(
    val proactiveAudio: Boolean? = null,
)

/**
 * Represents the bidi generate content transcription.
 *
 * @property text The text.
 */
@Serializable
data class BidiGenerateContentTranscription(
    val text: String? = null,
)

/**
 * Represents the live connect config.
 *
 * @property responseModalities The response modalities.
 * @property speechConfig The speech config.
 * @property systemInstruction The system instruction.
 * @property tools The tools.
 * @property generationConfig The generation config.
 * @property enableAffectiveDialog The enable affective dialog.
 */
@Serializable
data class LiveConnectConfig(
    val responseModalities: Array<Modality>? = null,
    val speechConfig: SpeechConfig? = null,
    val systemInstruction: Content? = null,
    val tools: Array<Tool>? = null,
    val generationConfig: GenerationConfig? = null,
    /**
     * Holds the enable affective dialog.
     */
    val enableAffectiveDialog: Boolean? = null, // Helper for API v1alpha if needed, but not in main Setup struct
)
