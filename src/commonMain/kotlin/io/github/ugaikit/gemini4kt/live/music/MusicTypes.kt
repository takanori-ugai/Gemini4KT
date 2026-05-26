package io.github.ugaikit.gemini4kt.live.music

import kotlinx.serialization.Serializable

// --- Client Messages ---

/**
 * Initial setup payload for a Live Music session.
 *
 * @property model Model resource name, usually in `models/{model}` format.
 */
@Serializable
data class LiveMusicClientSetup(
    val model: String? = null,
)

/**
 * Prompt text paired with a relative influence weight.
 *
 * @property text Prompt text used to steer generation.
 * @property weight Relative influence of the prompt.
 */
@Serializable
data class WeightedPrompt(
    val text: String? = null,
    val weight: Double? = null,
)

/**
 * Client content update for live music generation.
 *
 * @property weightedPrompts Optional weighted prompts that guide generation.
 */
@Serializable
data class LiveMusicClientContent(
    val weightedPrompts: List<WeightedPrompt>? = null,
)

/**
 * Music generation configuration.
 *
 * @property temperature Sampling temperature.
 * @property topK Top-k sampling limit.
 * @property seed Optional random seed.
 * @property guidance Guidance strength.
 * @property bpm Target beats per minute.
 * @property density Musical density control.
 * @property brightness Timbre brightness control.
 * @property scale Target musical scale.
 * @property muteBass Mutes bass when true.
 * @property muteDrums Mutes drums when true.
 * @property onlyBassAndDrums Restricts output to bass and drums when true.
 * @property musicGenerationMode Strategy preset for generation quality/diversity.
 */
@Serializable
data class LiveMusicGenerationConfig(
    val temperature: Double? = null,
    val topK: Int? = null,
    val seed: Int? = null,
    val guidance: Double? = null,
    val bpm: Int? = null,
    val density: Double? = null,
    val brightness: Double? = null,
    val scale: Scale? = null,
    val muteBass: Boolean? = null,
    val muteDrums: Boolean? = null,
    val onlyBassAndDrums: Boolean? = null,
    val musicGenerationMode: MusicGenerationMode? = null,
)

/**
 * Client message envelope for Live Music API communication.
 *
 * Exactly one field should be set per message.
 *
 * @property setup Optional setup payload for initial handshake.
 * @property clientContent Optional prompt/content update.
 * @property musicGenerationConfig Optional generation parameter update.
 * @property playbackControl Optional playback control command.
 */
@Serializable
data class LiveMusicClientMessage(
    val setup: LiveMusicClientSetup? = null,
    val clientContent: LiveMusicClientContent? = null,
    val musicGenerationConfig: LiveMusicGenerationConfig? = null,
    val playbackControl: LiveMusicPlaybackControl? = null,
)

// --- Server Messages ---

/**
 * Marker payload indicating successful Live Music setup.
 */
@Serializable
class LiveMusicServerSetupComplete

/**
 * Metadata describing which prompts and config produced an audio chunk.
 *
 * @property clientContent Prompt content active when the chunk was generated.
 * @property musicGenerationConfig Generation configuration active for the chunk.
 */
@Serializable
data class LiveMusicSourceMetadata(
    val clientContent: LiveMusicClientContent? = null,
    val musicGenerationConfig: LiveMusicGenerationConfig? = null,
)

/**
 * Base64-encoded audio output chunk.
 *
 * @property data Base64-encoded audio bytes.
 * @property mimeType MIME type of the audio chunk.
 * @property sourceMetadata Prompt/config metadata for this chunk.
 */
@Serializable
data class AudioChunk(
    val data: String? = null,
    val mimeType: String? = null,
    val sourceMetadata: LiveMusicSourceMetadata? = null,
)

/**
 * Model output content payload for Live Music.
 *
 * @property audioChunks Generated audio chunks.
 */
@Serializable
data class LiveMusicServerContent(
    val audioChunks: List<AudioChunk>? = null,
)

/**
 * Prompt text that was filtered by the service.
 *
 * @property text Prompt text that triggered filtering.
 * @property filteredReason Service-provided reason for filtering.
 */
@Serializable
data class LiveMusicFilteredPrompt(
    val text: String? = null,
    val filteredReason: String? = null,
)

/**
 * Server message envelope returned by the Live Music API.
 *
 * @property setupComplete Optional setup-complete marker.
 * @property serverContent Optional model-generated content.
 * @property filteredPrompt Optional filtered prompt report.
 */
@Serializable
data class LiveMusicServerMessage(
    val setupComplete: LiveMusicServerSetupComplete? = null,
    val serverContent: LiveMusicServerContent? = null,
    val filteredPrompt: LiveMusicFilteredPrompt? = null,
)

// --- Enums ---

/**
 * Supported musical key presets.
 */
@Serializable
enum class Scale {
    SCALE_UNSPECIFIED,
    C_MAJOR_A_MINOR,
    D_FLAT_MAJOR_B_FLAT_MINOR,
    D_MAJOR_B_MINOR,
    E_FLAT_MAJOR_C_MINOR,
    E_MAJOR_D_FLAT_MINOR,
    F_MAJOR_D_MINOR,
    G_FLAT_MAJOR_E_FLAT_MINOR,
    G_MAJOR_E_MINOR,
    A_FLAT_MAJOR_F_MINOR,
    A_MAJOR_G_FLAT_MINOR,
    B_FLAT_MAJOR_G_MINOR,
    B_MAJOR_A_FLAT_MINOR,
}

/**
 * Generation mode presets for the Live Music model.
 */
@Serializable
enum class MusicGenerationMode {
    MUSIC_GENERATION_MODE_UNSPECIFIED,
    QUALITY,
    DIVERSITY,
    VOCALIZATION,
}

/**
 * Playback control commands accepted by the Live Music session.
 */
@Serializable
enum class LiveMusicPlaybackControl {
    PLAYBACK_CONTROL_UNSPECIFIED,
    PLAY,
    PAUSE,
    STOP,
    RESET_CONTEXT,
}
