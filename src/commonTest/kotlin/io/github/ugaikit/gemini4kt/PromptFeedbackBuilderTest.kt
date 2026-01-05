package io.github.ugaikit.gemini4kt

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Represents the prompt feedback builder test.
 */
class PromptFeedbackBuilderTest {
    /**
     * Handles build with single safety rating.
     */
    @Test
    fun buildWithSingleSafetyRating() {
        val promptFeedback =
            promptFeedback {
                safetyRating {
                    category = HarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT
                    probability = HarmProbability.NEGLIGIBLE
                }
            }

        assertEquals(1, promptFeedback.safetyRatings.size)
        assertEquals(HarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT, promptFeedback.safetyRatings[0].category)
    }

    /**
     * Handles build with multiple safety ratings.
     */
    @Test
    fun buildWithMultipleSafetyRatings() {
        val promptFeedback =
            promptFeedback {
                safetyRating {
                    category = HarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT
                    probability = HarmProbability.NEGLIGIBLE
                }
                safetyRating {
                    category = HarmCategory.HARM_CATEGORY_HARASSMENT
                    probability = HarmProbability.LOW
                }
            }

        assertEquals(2, promptFeedback.safetyRatings.size)
    }

    /**
     * Handles build with no safety ratings.
     */
    @Test
    fun buildWithNoSafetyRatings() {
        val promptFeedback = promptFeedback {}
        assertEquals(0, promptFeedback.safetyRatings.size)
    }
}
