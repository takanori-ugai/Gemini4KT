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
 * Initial setup payload for a Live API session.
 *
 * This message is sent in the first client frame and configures the model and session behavior.
 *
 * @property model Model resource name, usually in `models/{model}` format.
 * @property generationConfig Optional generation configuration for the session.
 * @property systemInstruction Optional system instruction applied to the session.
 * @property tools Optional tool definitions available to the model.
 * @property realtimeInputConfig Optional realtime input handling configuration.
 * @property sessionResumption Optional configuration for resumable sessions.
 * @property contextWindowCompression Optional context-window compression settings.
 * @property inputAudioTranscription Optional transcription configuration for input audio.
 * @property outputAudioTranscription Optional transcription configuration for output audio.
 * @property proactivity Optional proactive interaction behavior settings.
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
 * Incremental conversation content sent by the client.
 *
 * @property turns Optional turn list to append to the running conversation state.
 * @property turnComplete Optional flag indicating that the current user turn is complete.
 */
@Serializable
data class BidiGenerateContentClientContent(
    val turns: List<Content>? = null,
    val turnComplete: Boolean? = null,
)

/**
 * Realtime input payload sent during a live session.
 *
 * @property media Optional generic media blob.
 * @property mediaChunks Optional chunked media payload.
 * @property audio Optional audio blob.
 * @property video Optional video blob.
 * @property activityStart Optional marker indicating user activity start.
 * @property activityEnd Optional marker indicating user activity end.
 * @property audioStreamEnd Optional marker that audio streaming has ended.
 * @property text Optional text input.
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
 * Tool-call result payload sent by the client.
 *
 * @property functionResponses Optional list of tool/function responses.
 */
@Serializable
data class BidiGenerateContentToolResponse(
    val functionResponses: List<FunctionResponse>? = null,
)

/**
 * Client message envelope for Live API communication.
 *
 * Exactly one field should be set per message.
 *
 * @property setup Optional setup message for the initial handshake.
 * @property clientContent Optional conversational content update.
 * @property realtimeInput Optional realtime media/text input.
 * @property toolResponse Optional response to a model-issued tool call.
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
 * Server message envelope returned by the Live API.
 *
 * @property usageMetadata Optional token and usage metadata.
 * @property setupComplete Optional setup-complete marker for handshake success.
 * @property serverContent Optional model output content.
 * @property toolCall Optional tool call requested by the model.
 * @property toolCallCancellation Optional cancellation request for prior tool calls.
 * @property goAway Optional advisory message indicating session shutdown timing.
 * @property sessionResumptionUpdate Optional update containing session-resumption state.
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
 * Marker payload indicating successful Live API setup.
 */
@Serializable
class BidiGenerateContentSetupComplete

/**
 * Model content update returned during a live generation turn.
 *
 * @property generationComplete Indicates model generation has completed.
 * @property turnComplete Indicates the current response turn has completed.
 * @property interrupted Indicates generation was interrupted.
 * @property groundingMetadata Optional grounding metadata associated with the output.
 * @property inputTranscription Optional transcript for input audio.
 * @property outputTranscription Optional transcript for model output audio.
 * @property urlContextMetadata Optional metadata for URL-context tools.
 * @property modelTurn Optional model turn content.
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
 * Tool call request emitted by the model.
 *
 * @property functionCalls Optional function call list.
 */
@Serializable
data class BidiGenerateContentToolCall(
    val functionCalls: List<FunctionCall>? = null,
)

/**
 * Cancellation request for in-flight tool calls.
 *
 * @property ids Optional list of call IDs to cancel.
 */
@Serializable
data class BidiGenerateContentToolCallCancellation(
    val ids: List<String>? = null,
)

/**
 * Advisory message indicating planned connection shutdown timing.
 *
 * @property timeLeft Remaining time until shutdown, for example `"10s"`.
 */
@Serializable
data class GoAway(
    val timeLeft: String? = null,
)

/**
 * Update payload for session resumption state.
 *
 * @property newHandle New handle that can be used to resume the session.
 * @property resumable Indicates whether the current session is resumable.
 */
@Serializable
data class SessionResumptionUpdate(
    val newHandle: String? = null,
    val resumable: Boolean? = null,
)

// --- Helper Types ---

/**
 * Realtime input behavior configuration.
 *
 * @property automaticActivityDetection Optional automatic activity detection settings.
 * @property activityHandling Optional activity interrupt behavior.
 * @property turnCoverage Optional rule for what input contributes to a turn.
 */
@Serializable
data class RealtimeInputConfig(
    val automaticActivityDetection: AutomaticActivityDetection? = null,
    val activityHandling: ActivityHandling? = null,
    val turnCoverage: TurnCoverage? = null,
)

/**
 * Automatic speech-activity detection settings.
 *
 * @property disabled Disables automatic activity detection when true.
 * @property startOfSpeechSensitivity Start-of-speech detection sensitivity.
 * @property prefixPaddingMs Milliseconds of pre-roll audio to retain.
 * @property endOfSpeechSensitivity End-of-speech detection sensitivity.
 * @property silenceDurationMs Silence duration threshold in milliseconds.
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
 * Defines how detected user activity affects model generation.
 */
@Serializable
enum class ActivityHandling {
    ACTIVITY_HANDLING_UNSPECIFIED,
    START_OF_ACTIVITY_INTERRUPTS,
    NO_INTERRUPTION,
}

/**
 * Defines which inputs are included in a completed turn.
 */
@Serializable
enum class TurnCoverage {
    TURN_COVERAGE_UNSPECIFIED,
    TURN_INCLUDES_ONLY_ACTIVITY,
    TURN_INCLUDES_ALL_INPUT,
}

/**
 * Start-of-speech detection sensitivity presets.
 */
@Serializable
enum class StartSensitivity {
    START_SENSITIVITY_UNSPECIFIED,
    START_SENSITIVITY_HIGH,
    START_SENSITIVITY_LOW,
}

/**
 * End-of-speech detection sensitivity presets.
 */
@Serializable
enum class EndSensitivity {
    END_SENSITIVITY_UNSPECIFIED,
    END_SENSITIVITY_HIGH,
    END_SENSITIVITY_LOW,
}

/**
 * Binary payload encoded for JSON transport.
 *
 * @property mimeType MIME type for the payload.
 * @property data Base64-encoded payload bytes.
 */
@Serializable
data class Blob(
    val mimeType: String,
    val data: String,
)

/**
 * Marker payload indicating start of user activity.
 */
@Serializable
class ActivityStart

/**
 * Marker payload indicating end of user activity.
 */
@Serializable
class ActivityEnd

/**
 * Session resumption configuration used in setup.
 *
 * @property handle Optional prior handle to resume from.
 */
@Serializable
data class SessionResumptionConfig(
    val handle: String? = null,
)

/**
 * Context-window compression configuration.
 *
 * @property slidingWindow Optional sliding-window token target.
 * @property triggerTokens Optional token threshold that triggers compression.
 */
@Serializable
data class ContextWindowCompressionConfig(
    val slidingWindow: SlidingWindow? = null,
    val triggerTokens: Long? = null,
)

/**
 * Sliding-window compression settings.
 *
 * @property targetTokens Target token count after compression.
 */
@Serializable
data class SlidingWindow(
    val targetTokens: Long? = null,
)

/**
 * Marker configuration enabling audio transcription.
 */
@Serializable
class AudioTranscriptionConfig

/**
 * Proactivity configuration for live interactions.
 *
 * @property proactiveAudio Enables proactive audio behavior when true.
 */
@Serializable
data class ProactivityConfig(
    val proactiveAudio: Boolean? = null,
)

/**
 * Transcription payload for input or output audio.
 *
 * @property text Transcribed text.
 */
@Serializable
data class BidiGenerateContentTranscription(
    val text: String? = null,
)

/**
 * Client-side defaults used when establishing a `GeminiLive` connection.
 *
 * @property responseModalities Optional response modality preferences.
 * @property speechConfig Optional speech generation configuration.
 * @property systemInstruction Optional system instruction.
 * @property tools Optional tools to register for model use.
 * @property generationConfig Optional generation configuration.
 * @property enableAffectiveDialog Optional helper flag for API variants that support it.
 */
@Serializable
data class LiveConnectConfig(
    val responseModalities: Array<Modality>? = null,
    val speechConfig: SpeechConfig? = null,
    val systemInstruction: Content? = null,
    val tools: Array<Tool>? = null,
    val generationConfig: GenerationConfig? = null,
    val enableAffectiveDialog: Boolean? = null,
)
