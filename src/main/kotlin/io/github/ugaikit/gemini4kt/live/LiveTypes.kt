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
    val tools: List<Tool>? = null,
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

@Serializable
class BidiGenerateContentSetupComplete

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

@Serializable
data class BidiGenerateContentToolCall(
    val functionCalls: List<FunctionCall>? = null,
)

@Serializable
data class BidiGenerateContentToolCallCancellation(
    val ids: List<String>? = null,
)

@Serializable
data class GoAway(
    val timeLeft: String? = null, // Using String for Duration, e.g., "10s"
)

@Serializable
data class SessionResumptionUpdate(
    val newHandle: String? = null,
    val resumable: Boolean? = null,
)

// --- Helper Types ---

@Serializable
data class RealtimeInputConfig(
    val automaticActivityDetection: AutomaticActivityDetection? = null,
    val activityHandling: ActivityHandling? = null,
    val turnCoverage: TurnCoverage? = null,
)

@Serializable
data class AutomaticActivityDetection(
    val disabled: Boolean? = null,
    val startOfSpeechSensitivity: StartSensitivity? = null,
    val prefixPaddingMs: Int? = null,
    val endOfSpeechSensitivity: EndSensitivity? = null,
    val silenceDurationMs: Int? = null,
)

@Serializable
enum class ActivityHandling {
    ACTIVITY_HANDLING_UNSPECIFIED,
    START_OF_ACTIVITY_INTERRUPTS,
    NO_INTERRUPTION,
}

@Serializable
enum class TurnCoverage {
    TURN_COVERAGE_UNSPECIFIED,
    TURN_INCLUDES_ONLY_ACTIVITY,
    TURN_INCLUDES_ALL_INPUT,
}

@Serializable
enum class StartSensitivity {
    START_SENSITIVITY_UNSPECIFIED,
    START_SENSITIVITY_HIGH,
    START_SENSITIVITY_LOW,
}

@Serializable
enum class EndSensitivity {
    END_SENSITIVITY_UNSPECIFIED,
    END_SENSITIVITY_HIGH,
    END_SENSITIVITY_LOW,
}

@Serializable
data class Blob(
    val mimeType: String,
    val data: String, // Base64 encoded bytes
)

@Serializable
class ActivityStart

@Serializable
class ActivityEnd

@Serializable
data class SessionResumptionConfig(
    val handle: String? = null,
)

@Serializable
data class ContextWindowCompressionConfig(
    val slidingWindow: SlidingWindow? = null,
    val triggerTokens: Long? = null,
)

@Serializable
data class SlidingWindow(
    val targetTokens: Long? = null,
)

@Serializable
class AudioTranscriptionConfig

@Serializable
data class ProactivityConfig(
    val proactiveAudio: Boolean? = null,
)

@Serializable
data class BidiGenerateContentTranscription(
    val text: String? = null,
)

@Serializable
data class LiveConnectConfig(
    val responseModalities: List<Modality>? = null,
    val speechConfig: SpeechConfig? = null,
    val systemInstruction: Content? = null,
    val tools: List<Tool>? = null,
    val generationConfig: GenerationConfig? = null,
    val enableAffectiveDialog: Boolean? = null, // Helper for API v1alpha if needed, but not in main Setup struct
)
