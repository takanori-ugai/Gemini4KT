package io.github.ugaikit.gemini4kt

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Represents the grounding passage id builder test.
 */
class GroundingPassageIdBuilderTest {
    /**
     * Tests test grounding passage id builder.
     */
    @Test
    fun testGroundingPassageIdBuilder() {
        val groundingPassageId =
            groundingPassageId {
                passageId = "passage-123"
                partIndex = 1
            }

        assertEquals("passage-123", groundingPassageId.passageId)
        assertEquals(1, groundingPassageId.partIndex)
    }
}
