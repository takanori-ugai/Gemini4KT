package io.github.ugaikit.gemini4kt

import kotlin.test.Test
import kotlin.test.assertEquals

class PromptFeedbackBuilderTest {
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

    @Test
    fun buildWithNoSafetyRatings() {
        val promptFeedback = promptFeedback {}
        assertEquals(0, promptFeedback.safetyRatings.size)
    }
}
