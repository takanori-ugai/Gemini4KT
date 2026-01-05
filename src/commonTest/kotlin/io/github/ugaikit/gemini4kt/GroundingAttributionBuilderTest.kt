package io.github.ugaikit.gemini4kt

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

/**
 * Represents the grounding attribution builder test.
 */
class GroundingAttributionBuilderTest {
    /**
     * Handles build with all properties.
     */
    @Test
    fun buildWithAllProperties() {
        val groundingAttribution =
            groundingAttribution {
                sourceId {
                    groundingPassage {
                        GroundingPassageId(
                            passageId = "passage123",
                            partIndex = 1,
                        )
                    }
                }
                content {
                    part { text { "Attribution content" } }
                }
            }

        assertNotNull(groundingAttribution.sourceId)
        assertNotNull(groundingAttribution.content)
    }

    /**
     * Handles build without required properties throws exception.
     */
    @Test
    fun buildWithoutRequiredPropertiesThrowsException() {
        assertFailsWith<RuntimeException> {
            groundingAttribution {
                content {
                    part { text { "Attribution content" } }
                }
            }
        }

        assertFailsWith<RuntimeException> {
            groundingAttribution {
                sourceId {
                    groundingPassage {
                        GroundingPassageId(
                            passageId = "passage123",
                            partIndex = 1,
                        )
                    }
                }
            }
        }
    }
}
