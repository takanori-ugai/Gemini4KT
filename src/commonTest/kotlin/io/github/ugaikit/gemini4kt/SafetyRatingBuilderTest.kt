package io.github.ugaikit.gemini4kt

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class SafetyRatingBuilderTest {
    @Test
    fun `build with all properties`() {
        val safetyRating =
            safetyRating {
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
        val safetyRating =
            safetyRating {
                category = HarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT
                probability = HarmProbability.NEGLIGIBLE
            }

        assertEquals(HarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT, safetyRating.category)
        assertEquals(HarmProbability.NEGLIGIBLE, safetyRating.probability)
        assertNull(safetyRating.blocked)
    }

    @Test
    fun `build without required properties throws exception`() {
        assertFailsWith<RuntimeException> {
            safetyRating {
                probability = HarmProbability.NEGLIGIBLE
            }
        }

        assertFailsWith<RuntimeException> {
            safetyRating {
                category = HarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT
            }
        }
    }
}
