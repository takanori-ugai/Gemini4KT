package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Configures the speech generation parameters.
 *
 * @property voiceConfig The configuration for a single voice.
 * @property multiSpeakerVoiceConfig The configuration for multiple speakers.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class SpeechConfig(
    val voiceConfig: VoiceConfig? = null,
    val multiSpeakerVoiceConfig: MultiSpeakerVoiceConfig? = null,
)

/**
 * Configures the voice parameters.
 *
 * @property prebuiltVoiceConfig The configuration for a prebuilt voice.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class VoiceConfig(
    val prebuiltVoiceConfig: PrebuiltVoiceConfig? = null,
)

/**
 * Configures the prebuilt voice parameters.
 *
 * @property voiceName The name of the prebuilt voice (e.g., "Kore", "Puck").
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class PrebuiltVoiceConfig(
    val voiceName: String? = null,
)

/**
 * Configures the multi-speaker voice parameters.
 *
 * @property speakerVoiceConfigs A list of speaker voice configurations.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class MultiSpeakerVoiceConfig(
    val speakerVoiceConfigs: Array<SpeakerVoiceConfig>? = null,
)

/**
 * Configures the voice parameters for a specific speaker.
 *
 * @property speaker The name of the speaker.
 * @property voiceConfig The voice configuration for the speaker.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class SpeakerVoiceConfig(
    val speaker: String? = null,
    val voiceConfig: VoiceConfig? = null,
)

/**
 * Represents the speech config builder.
 */
class SpeechConfigBuilder {
    /**
     * Holds the voice config.
     */
    var voiceConfig: VoiceConfig? = null

    /**
     * Holds the multi speaker voice config.
     */
    var multiSpeakerVoiceConfig: MultiSpeakerVoiceConfig? = null

    /**
     * Handles voice config.
     *
     * @param init The init.
     */
    fun voiceConfig(init: VoiceConfigBuilder.() -> Unit) {
        voiceConfig = VoiceConfigBuilder().apply(init).build()
    }

    /**
     * Handles multi speaker voice config.
     *
     * @param init The init.
     */
    fun multiSpeakerVoiceConfig(init: MultiSpeakerVoiceConfigBuilder.() -> Unit) {
        multiSpeakerVoiceConfig = MultiSpeakerVoiceConfigBuilder().apply(init).build()
    }

    /**
     * Handles build.
     */
    fun build() =
        SpeechConfig(
            voiceConfig = voiceConfig,
            multiSpeakerVoiceConfig = multiSpeakerVoiceConfig,
        ).also {
            require(listOfNotNull(voiceConfig, multiSpeakerVoiceConfig).size == 1) {
                "SpeechConfigBuilder requires exactly one voice configuration."
            }
        }
}

/**
 * Represents the voice config builder.
 */
class VoiceConfigBuilder {
    /**
     * Holds the prebuilt voice config.
     */
    var prebuiltVoiceConfig: PrebuiltVoiceConfig? = null

    /**
     * Handles prebuilt voice config.
     *
     * @param init The init.
     */
    fun prebuiltVoiceConfig(init: PrebuiltVoiceConfigBuilder.() -> Unit) {
        prebuiltVoiceConfig = PrebuiltVoiceConfigBuilder().apply(init).build()
    }

    /**
     * Handles build.
     */
    fun build() =
        VoiceConfig(
            prebuiltVoiceConfig = prebuiltVoiceConfig,
        ).also {
            require(prebuiltVoiceConfig != null) {
                "VoiceConfigBuilder requires prebuiltVoiceConfig."
            }
        }
}

/**
 * Represents the prebuilt voice config builder.
 */
class PrebuiltVoiceConfigBuilder {
    /**
     * Holds the voice name.
     */
    var voiceName: String? = null

    /**
     * Handles voice name.
     *
     * @param init The init.
     */
    fun voiceName(init: () -> String) {
        voiceName = init()
    }

    /**
     * Handles build.
     */
    fun build() =
        PrebuiltVoiceConfig(
            voiceName = voiceName,
        ).also {
            require(!voiceName.isNullOrBlank()) {
                "PrebuiltVoiceConfigBuilder requires voiceName."
            }
        }
}

/**
 * Represents the multi speaker voice config builder.
 */
class MultiSpeakerVoiceConfigBuilder {
    /**
     * Holds the speaker voice configs.
     */
    private val speakerVoiceConfigs: MutableList<SpeakerVoiceConfig> = mutableListOf()

    /**
     * Handles speaker voice config.
     *
     * @param init The init.
     */
    fun speakerVoiceConfig(init: SpeakerVoiceConfigBuilder.() -> Unit) {
        speakerVoiceConfigs.add(SpeakerVoiceConfigBuilder().apply(init).build())
    }

    /**
     * Handles build.
     */
    fun build() =
        MultiSpeakerVoiceConfig(
            speakerVoiceConfigs = if (speakerVoiceConfigs.isEmpty()) null else speakerVoiceConfigs.toTypedArray(),
        ).also {
            require(speakerVoiceConfigs.isNotEmpty()) {
                "MultiSpeakerVoiceConfigBuilder requires at least one speakerVoiceConfig."
            }
        }
}

/**
 * Represents the speaker voice config builder.
 */
class SpeakerVoiceConfigBuilder {
    /**
     * Holds the speaker.
     */
    var speaker: String? = null

    /**
     * Holds the voice config.
     */
    var voiceConfig: VoiceConfig? = null

    /**
     * Handles speaker.
     *
     * @param init The init.
     */
    fun speaker(init: () -> String) {
        speaker = init()
    }

    /**
     * Handles voice config.
     *
     * @param init The init.
     */
    fun voiceConfig(init: VoiceConfigBuilder.() -> Unit) {
        voiceConfig = VoiceConfigBuilder().apply(init).build()
    }

    /**
     * Handles build.
     */
    fun build() =
        SpeakerVoiceConfig(
            speaker = speaker,
            voiceConfig = voiceConfig,
        ).also {
            require(!speaker.isNullOrBlank()) {
                "SpeakerVoiceConfigBuilder requires speaker."
            }
            require(voiceConfig != null) {
                "SpeakerVoiceConfigBuilder requires voiceConfig."
            }
        }
}

/**
 * Handles speech config.
 *
 * @param init The init.
 */
fun speechConfig(init: SpeechConfigBuilder.() -> Unit): SpeechConfig = SpeechConfigBuilder().apply(init).build()
