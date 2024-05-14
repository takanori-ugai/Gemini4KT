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
    val stopSequences: List<String>,
    val temperature: Double,
    val maxOutputTokens: Int,
    val topP: Double,
    val topK: Int,
    @SerialName("response_mime_type")
    val responseMimeType: String? = null,
    // "application/json" only for Gemini 1.5 pro
)

class GenerationConfigBuilder {
    private val stopSequences: MutableList<String> = mutableListOf()
    var temperature: Double = 0.0
    var maxOutputTokens: Int = 0
    var topP: Double = 0.0
    var topK: Int = 0
    var responseMimeType: String? = null

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
        )
}

fun generationConfig(init: GenerationConfigBuilder.() -> Unit): GenerationConfig = GenerationConfigBuilder().apply(init).build()
