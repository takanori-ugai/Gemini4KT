package io.github.ugaikit.gemini4kt

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PromptFeedbackBuilderTest {

    @Test
    fun `build with single safety rating`() {
        val promptFeedback = promptFeedback {
            safetyRating {
                category = HarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT
                probability = HarmProbability.NEGLIGIBLE
            }
        }

        assertEquals(1, promptFeedback.safetyRatings.size)
        assertEquals(HarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT, promptFeedback.safetyRatings[0].category)
    }

    @Test
    fun `build with multiple safety ratings`() {
        val promptFeedback = promptFeedback {
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
    fun `build with no safety ratings`() {
        val promptFeedback = promptFeedback {}
        assertEquals(0, promptFeedback.safetyRatings.size)
    }
}
