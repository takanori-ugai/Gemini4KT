package io.github.ugaikit.gemini4kt.decisions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

private const val MIN_CRITERIA_COUNT = 2
private const val MAX_CRITERIA_COUNT = 255

/** A typed yes/no, categorical, or ordered-rubric question. Instructions are always text only. */
@Serializable
sealed interface DecisionQuestion {
    val instructions: DecisionInput

    /** Binary judgment; the answer's `noul` field is the probability of yes. */
    @Serializable
    @SerialName("noul")
    data class Noul(
        override val instructions: DecisionInput,
        val criteria: NoulCriteria? = null,
    ) : DecisionQuestion {
        constructor(instructions: String, criteria: NoulCriteria? = null) : this(DecisionInput(instructions), criteria)

        init {
            instructions.requireTextOnly()
        }
    }

    /** Categorical judgment. Criteria map option identifiers to their descriptions. */
    @Serializable
    @SerialName("choice")
    data class Choice(
        override val instructions: DecisionInput,
        val criteria: Map<String, String>,
    ) : DecisionQuestion {
        constructor(instructions: String, criteria: Map<String, String>) : this(DecisionInput(instructions), criteria)

        init {
            instructions.requireTextOnly()
            require(criteria.size in MIN_CRITERIA_COUNT..MAX_CRITERIA_COUNT) {
                "Choice questions require 2 to 255 options."
            }
        }
    }

    /** Ordered rubric, from the lowest level at index zero to the highest level. */
    @Serializable
    @SerialName("score")
    data class Score(
        override val instructions: DecisionInput,
        val criteria: List<String>,
    ) : DecisionQuestion {
        constructor(instructions: String, criteria: List<String>) : this(DecisionInput(instructions), criteria)

        init {
            instructions.requireTextOnly()
            require(criteria.size in MIN_CRITERIA_COUNT..MAX_CRITERIA_COUNT && criteria.all { it.isNotBlank() }) {
                "Score questions require 2 to 255 non-empty rubric levels."
            }
        }
    }
}

/** Optional definitions of yes and no for a binary question. */
@Serializable
data class NoulCriteria(
    @SerialName("true_description") val trueDescription: String? = null,
    @SerialName("false_description") val falseDescription: String? = null,
)
