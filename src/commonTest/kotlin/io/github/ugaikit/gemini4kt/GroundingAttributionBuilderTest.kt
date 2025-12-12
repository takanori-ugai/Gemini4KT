package io.github.ugaikit.gemini4kt

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class GroundingAttributionBuilderTest {
    @Test
    fun `build with all properties`() {
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

    @Test
    fun `build without required properties throws exception`() {
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
