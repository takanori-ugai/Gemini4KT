package io.github.ugaikit.gemini4kt

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.UninitializedPropertyAccessException

class SafetyRatingBuilderTest {

    @Test
    fun `build with all properties`() {
        val safetyRating = safetyRating {
            category = HarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT
            probability = HarmProbability.NEGLIGIBLE
            blocked = true
        }

        assertEquals(HarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT, safetyRating.category)
        assertEquals(HarmProbability.NEGLIGIBLE, safetyRating.probability)
        assertEquals(true, safetyRating.blocked)
    }

    @Test
    fun `build with required properties only`() {
        val safetyRating = safetyRating {
            category = HarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT
            probability = HarmProbability.NEGLIGIBLE
        }

        assertEquals(HarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT, safetyRating.category)
        assertEquals(HarmProbability.NEGLIGIBLE, safetyRating.probability)
        assertNull(safetyRating.blocked)
    }

    @Test
    fun `build without required properties throws exception`() {
        assertThrows<UninitializedPropertyAccessException> {
            safetyRating {
                probability = HarmProbability.NEGLIGIBLE
            }
        }

        assertThrows<UninitializedPropertyAccessException> {
            safetyRating {
                category = HarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT
            }
        }
    }
}
