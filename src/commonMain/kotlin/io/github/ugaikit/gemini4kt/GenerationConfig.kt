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
 * model to stop generating further content.
 * @property temperature A double value controlling the randomness of the generation.
 * @property maxOutputTokens An integer specifying the maximum number of tokens that can be generated.
 * @property topP A double value for nucleus sampling.
 * @property topK An integer that limits the model to consider only the top-k most likely next tokens.
 * @property responseMimeType An optional string specifying the MIME type of the response.
 * @property responseModalities Array of modalities allowed in the response.
 * @property thinkingConfig Thinking configuration for budgeting model thought processes.
 * @property imageConfig Image generation configurations.
 * @property speechConfig Speech configuration for audio output.
 * @property responseSchema Structure schema of the output response.
 * @property responseJsonSchema Json element representation of the response schema.
 * @property underscoreResponseJsonSchema Backward compatibility response json schema.
 * @property seed Random seed configuration for deterministic sampling.
 * @property presencePenalty Penalty for repeating topics.
 * @property frequencyPenalty Penalty for repeating tokens.
 * @property responseLogprobs True if logprobs should be returned.
 * @property logprobs Number of top logprob candidates to return.
 * @property enableEnhancedCivicAnswers True to enable enhanced civic answers.
 * @property mediaResolution Media resolution level.
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
    @SerialName("speech_config")
    val speechConfig: SpeechConfig? = null,
    val responseSchema: Schema? = null,
    @SerialName("response_json_schema")
    val responseJsonSchema: kotlinx.serialization.json.JsonElement? = null,
    @SerialName("_responseJsonSchema")
    val underscoreResponseJsonSchema: kotlinx.serialization.json.JsonElement? = null,
    val seed: Int? = null,
    val presencePenalty: Double? = null,
    val frequencyPenalty: Double? = null,
    val responseLogprobs: Boolean? = null,
    val logprobs: Int? = null,
    val enableEnhancedCivicAnswers: Boolean? = null,
    val mediaResolution: MediaResolutionLevel? = null,
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
     * Holds the response schema.
     */
    var responseSchema: Schema? = null

    /**
     * Holds the response json schema.
     */
    var responseJsonSchema: kotlinx.serialization.json.JsonElement? = null

    /**
     * Holds the underscore response json schema.
     */
    var underscoreResponseJsonSchema: kotlinx.serialization.json.JsonElement? = null

    /**
     * Holds the seed.
     */
    var seed: Int? = null

    /**
     * Holds the presence penalty.
     */
    var presencePenalty: Double? = null

    /**
     * Holds the frequency penalty.
     */
    var frequencyPenalty: Double? = null

    /**
     * Holds the response logprobs flag.
     */
    var responseLogprobs: Boolean? = null

    /**
     * Holds the logprobs value.
     */
    var logprobs: Int? = null

    /**
     * Holds the enable enhanced civic answers flag.
     */
    var enableEnhancedCivicAnswers: Boolean? = null

    /**
     * Holds the media resolution.
     */
    var mediaResolution: MediaResolutionLevel? = null

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
     * Handles response schema.
     *
     * @param init The init.
     */
    fun responseSchema(init: SchemaBuilder.() -> Unit) {
        responseSchema = SchemaBuilder().apply(init).build()
    }

    /**
     * Handles build.
     */
    fun build(): GenerationConfig {
        val schemaCount =
            listOfNotNull(
                responseSchema,
                responseJsonSchema,
                underscoreResponseJsonSchema,
            ).size
        require(schemaCount <= 1) {
            "Only one of responseSchema, responseJsonSchema, or underscoreResponseJsonSchema can be set."
        }

        return GenerationConfig(
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
            responseSchema = responseSchema,
            responseJsonSchema = responseJsonSchema,
            underscoreResponseJsonSchema = underscoreResponseJsonSchema,
            seed = seed,
            presencePenalty = presencePenalty,
            frequencyPenalty = frequencyPenalty,
            responseLogprobs = responseLogprobs,
            logprobs = logprobs,
            enableEnhancedCivicAnswers = enableEnhancedCivicAnswers,
            mediaResolution = mediaResolution,
        )
    }
}

/**
 * Handles generation config.
 *
 * @param init The init.
 */
fun generationConfig(init: GenerationConfigBuilder.() -> Unit): GenerationConfig =
    GenerationConfigBuilder()
        .apply(init)
        .build()
