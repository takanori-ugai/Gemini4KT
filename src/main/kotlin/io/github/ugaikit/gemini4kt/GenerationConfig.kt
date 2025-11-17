package io.github.ugaikit.gemini4kt

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Configures the parameters for content generation, including conditions for
 * stopping generation, creativity controls, output limits, and the format of the
 * generated content.
 *
 * @property stopSequences A list of strings that, when generated, will signal the
 * model to stop generating further content. This can be used to define natural
 * endpoints or boundaries for generated content.
 * @property temperature A double value controlling the randomness of the
 * generation. Higher values increase creativity and diversity, while lower values
 * make the output more deterministic.
 * @property maxOutputTokens An integer specifying the maximum number of tokens
 * that can be generated. This serves as a hard limit on the size of the generated

 * content.
 * @property topP A double value for nucleus sampling, a stochastic decoding method
 * that focuses generation on the most likely next tokens with cumulative
 * probability above this threshold.
 * @property topK An integer that limits the model to consider only the top-k most
 * likely next tokens for each step of generation, enhancing control over the
 * randomness and relevance of the output.
 * @property responseMimeType An optional string specifying the MIME type of the
 * response. This is particularly relevant for specialized applications like
 * Gemini 1.5 pro, where "application/json" might be required. It is nullable to
 * accommodate different or default response formats.
 */
@Serializable
data class GenerationConfig(
    val stopSequences: List<String>? = null,
    val temperature: Double? = null,
    val maxOutputTokens: Int? = null,
    val topP: Double? = null,
    val topK: Int? = null,
    @SerialName("response_mime_type")
    val responseMimeType: String? = null,
    val thinkingConfig: ThinkingConfig? = null,
)

class GenerationConfigBuilder {
    private val stopSequences: MutableList<String> = mutableListOf()
    var temperature: Double? = null
    var maxOutputTokens: Int? = null
    var topP: Double? = null
    var topK: Int? = null
    var responseMimeType: String? = null
    var thinkingConfig: ThinkingConfig? = null

    fun stopSequence(sequence: String) {
        stopSequences.add(sequence)
    }

    fun build() =
        GenerationConfig(
            stopSequences = stopSequences,
            temperature = temperature,
            maxOutputTokens = maxOutputTokens,
            topP = topP,
            topK = topK,
            responseMimeType = responseMimeType,
            thinkingConfig = thinkingConfig,
        )
}

fun generationConfig(
    init: GenerationConfigBuilder.() -> Unit,
): GenerationConfig = GenerationConfigBuilder().apply(init).build()
