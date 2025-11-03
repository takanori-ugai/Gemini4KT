package io.github.ugaikit.gemini4kt

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.UninitializedPropertyAccessException

class GroundingAttributionBuilderTest {

    @Test
    fun `build with all properties`() {
        val groundingAttribution = groundingAttribution {
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
        assertThrows<UninitializedPropertyAccessException> {
            groundingAttribution {
                content {
                    part { text { "Attribution content" } }
                }
            }
        }

        assertThrows<UninitializedPropertyAccessException> {
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
