package io.github.ugaikit.gemini4kt

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

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
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class GenerationConfig(
    val stopSequences: Array<String>? = null,
    val temperature: Double? = null,
    val maxOutputTokens: Int? = null,
    val topP: Double? = null,
    val topK: Int? = null,
    @SerialName("response_mime_type")
    val responseMimeType: String? = null,
    @SerialName("response_modalities")
    val responseModalities: Array<Modality>? = null,
    val thinkingConfig: ThinkingConfig? = null,
    val imageConfig: ImageConfig? = null,
    val speechConfig: SpeechConfig? = null,
)

/**
 * Represents the generation config builder.
 */
class GenerationConfigBuilder {
    /**
     * Holds the stop sequences.
     */
    private val stopSequences: MutableList<String> = mutableListOf()

    /**
     * Holds the temperature.
     */
    var temperature: Double? = null

    /**
     * Holds the max output tokens.
     */
    var maxOutputTokens: Int? = null

    /**
     * Holds the top p.
     */
    var topP: Double? = null

    /**
     * Holds the top k.
     */
    var topK: Int? = null

    /**
     * Holds the response mime type.
     */
    var responseMimeType: String? = null

    /**
     * Holds the response modalities.
     */
    private val responseModalities: MutableList<Modality> = mutableListOf()

    /**
     * Holds the thinking config.
     */
    var thinkingConfig: ThinkingConfig? = null

    /**
     * Holds the image config.
     */
    var imageConfig: ImageConfig? = null

    /**
     * Holds the speech config.
     */
    var speechConfig: SpeechConfig? = null

    /**
     * Handles stop sequence.
     *
     * @param sequence The sequence.
     */
    fun stopSequence(sequence: String) {
        stopSequences.add(sequence)
    }

    /**
     * Handles response modality.
     *
     * @param modality The modality.
     */
    fun responseModality(modality: Modality) {
        responseModalities.add(modality)
    }

    /**
     * Handles image config.
     *
     * @param init The init.
     */
    fun imageConfig(init: ImageConfigBuilder.() -> Unit) {
        imageConfig = ImageConfigBuilder().apply(init).build()
    }

    /**
     * Handles speech config.
     *
     * @param init The init.
     */
    fun speechConfig(init: SpeechConfigBuilder.() -> Unit) {
        speechConfig = SpeechConfigBuilder().apply(init).build()
    }

    /**
     * Handles build.
     */
    fun build() =
        GenerationConfig(
            stopSequences = if (stopSequences.isEmpty()) null else stopSequences.toTypedArray(),
            temperature = temperature,
            maxOutputTokens = maxOutputTokens,
            topP = topP,
            topK = topK,
            responseMimeType = responseMimeType,
            responseModalities = if (responseModalities.isEmpty()) null else responseModalities.toTypedArray(),
            thinkingConfig = thinkingConfig,
            imageConfig = imageConfig,
            speechConfig = speechConfig,
        )
}

/**
 * Handles generation config.
 *
 * @param init The init.
 */
fun generationConfig(init: GenerationConfigBuilder.() -> Unit): GenerationConfig = GenerationConfigBuilder().apply(init).build()
