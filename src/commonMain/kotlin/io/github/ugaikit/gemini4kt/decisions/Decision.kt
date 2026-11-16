package io.github.ugaikit.gemini4kt.decisions

import io.github.ugaikit.gemini4kt.interaction.InteractionUsage
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

private const val MAX_QUESTIONS = 1000
private val QUESTION_KEY = Regex("[A-Za-z0-9_-]{1,64}")

/** Request for `POST /v1beta/decisions`. Each question is evaluated against the same shared input. */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class CreateDecisionRequest(
    val input: DecisionInput,
    val questions: Map<String, DecisionQuestion>,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val model: String = "gemini-decision-1",
) {
    constructor(
        input: String,
        questions: Map<String, DecisionQuestion>,
        model: String = "gemini-decision-1",
    ) : this(DecisionInput(input), questions, model)

    init {
        require(model.isNotBlank()) { "model must not be blank." }
        require(questions.size in 1..MAX_QUESTIONS) { "A decision request requires 1 to 1000 questions." }
        require(questions.keys.all(QUESTION_KEY::matches)) {
            "Question keys must contain 1 to 64 ASCII letters, digits, underscores, or hyphens."
        }
    }
}

/** Typed answers keyed by the request's question identifiers, with input token usage. */
@Serializable
data class DecisionResponse(
    val answers: Map<String, DecisionAnswer>,
    val usage: InteractionUsage? = null,
)

/** Calibrated decision value and confidence. The API generates no text output tokens. */
@Serializable
sealed interface DecisionAnswer {
    val confidence: Double

    /** Probability of yes, between 0.01 and 0.99. */
    @Serializable
    @SerialName("noul")
    data class Noul(
        val noul: Double,
        override val confidence: Double,
    ) : DecisionAnswer

    /** Most likely categorical option and the full probability distribution. */
    @Serializable
    @SerialName("choice")
    data class Choice(
        val choice: String,
        val probabilities: Map<String, Double>,
        override val confidence: Double,
    ) : DecisionAnswer

    /** Probability-weighted rubric index, per-level probabilities, and index descriptions. */
    @Serializable
    @SerialName("score")
    data class Score(
        val score: Double,
        val probabilities: Map<String, Double>,
        val legend: Map<String, String>,
        override val confidence: Double,
    ) : DecisionAnswer
}
