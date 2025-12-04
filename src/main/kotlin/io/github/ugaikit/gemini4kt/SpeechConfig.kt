package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

/**
 * Configures the speech generation parameters.
 *
 * @property voiceConfig The configuration for a single voice.
 * @property multiSpeakerVoiceConfig The configuration for multiple speakers.
 */
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
@Serializable
data class VoiceConfig(
    val prebuiltVoiceConfig: PrebuiltVoiceConfig? = null,
)

/**
 * Configures the prebuilt voice parameters.
 *
 * @property voiceName The name of the prebuilt voice (e.g., "Kore", "Puck").
 */
@Serializable
data class PrebuiltVoiceConfig(
    val voiceName: String? = null,
)

/**
 * Configures the multi-speaker voice parameters.
 *
 * @property speakerVoiceConfigs A list of speaker voice configurations.
 */
@Serializable
data class MultiSpeakerVoiceConfig(
    val speakerVoiceConfigs: List<SpeakerVoiceConfig>? = null,
)

/**
 * Configures the voice parameters for a specific speaker.
 *
 * @property speaker The name of the speaker.
 * @property voiceConfig The voice configuration for the speaker.
 */
@Serializable
data class SpeakerVoiceConfig(
    val speaker: String? = null,
    val voiceConfig: VoiceConfig? = null,
)

class SpeechConfigBuilder {
    var voiceConfig: VoiceConfig? = null
    var multiSpeakerVoiceConfig: MultiSpeakerVoiceConfig? = null

    fun voiceConfig(init: VoiceConfigBuilder.() -> Unit) {
        voiceConfig = VoiceConfigBuilder().apply(init).build()
    }

    fun multiSpeakerVoiceConfig(init: MultiSpeakerVoiceConfigBuilder.() -> Unit) {
        multiSpeakerVoiceConfig = MultiSpeakerVoiceConfigBuilder().apply(init).build()
    }

    fun build() =
        SpeechConfig(
            voiceConfig = voiceConfig,
            multiSpeakerVoiceConfig = multiSpeakerVoiceConfig,
        )
}

class VoiceConfigBuilder {
    var prebuiltVoiceConfig: PrebuiltVoiceConfig? = null

    fun prebuiltVoiceConfig(init: PrebuiltVoiceConfigBuilder.() -> Unit) {
        prebuiltVoiceConfig = PrebuiltVoiceConfigBuilder().apply(init).build()
    }

    fun build() =
        VoiceConfig(
            prebuiltVoiceConfig = prebuiltVoiceConfig,
        )
}

class PrebuiltVoiceConfigBuilder {
    var voiceName: String? = null

    fun voiceName(init: () -> String) {
        voiceName = init()
    }

    fun build() =
        PrebuiltVoiceConfig(
            voiceName = voiceName,
        )
}

class MultiSpeakerVoiceConfigBuilder {
    private val speakerVoiceConfigs: MutableList<SpeakerVoiceConfig> = mutableListOf()

    fun speakerVoiceConfig(init: SpeakerVoiceConfigBuilder.() -> Unit) {
        speakerVoiceConfigs.add(SpeakerVoiceConfigBuilder().apply(init).build())
    }

    fun build() =
        MultiSpeakerVoiceConfig(
            speakerVoiceConfigs = if (speakerVoiceConfigs.isEmpty()) null else speakerVoiceConfigs,
        )
}

class SpeakerVoiceConfigBuilder {
    var speaker: String? = null
    var voiceConfig: VoiceConfig? = null

    fun speaker(init: () -> String) {
        speaker = init()
    }

    fun voiceConfig(init: VoiceConfigBuilder.() -> Unit) {
        voiceConfig = VoiceConfigBuilder().apply(init).build()
    }

    fun build() =
        SpeakerVoiceConfig(
            speaker = speaker,
            voiceConfig = voiceConfig,
        )
}

fun speechConfig(init: SpeechConfigBuilder.() -> Unit): SpeechConfig = SpeechConfigBuilder().apply(init).build()
