package io.github.ugaikit.gemini4kt

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.UninitializedPropertyAccessException

class CandidateBuilderTest {
    @Test
    fun `build with all properties`() {
        val candidate =
            candidate {
                content {
                    part { text { "This is a test." } }
                }
                finishReason = "STOP"
                index = 1
                safetyRating {
                    category = HarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT
                    probability = HarmProbability.NEGLIGIBLE
                }
                citationMetadata =
                    CitationMetadata(
                        citationSources =
                            listOf(
                                CitationSource(
                                    startIndex = 0,
                                    endIndex = 10,
                                    uri = "http://example.com",
                                    license = "Creative Commons",
                                ),
                            ),
                    )
                tokenCount = 5
                avgLogprobs = 0.9
                logprobsResult =
                    LogprobsResult(
                        topCandidates = emptyList(),
                        chosenCandidates = emptyList(),
                    )
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
            }

        assertNotNull(candidate.content)
        assertEquals("STOP", candidate.finishReason)
        assertEquals(1, candidate.index)
        assertEquals(1, candidate.safetyRatings?.size)
        assertNotNull(candidate.citationMetadata)
        assertEquals(5, candidate.tokenCount)
        assertEquals(0.9, candidate.avgLogprobs)
        assertNotNull(candidate.logprobsResult)
        assertEquals(1, candidate.groundingAttributions.size)
    }

    @Test
    fun `build with required properties only`() {
        val candidate =
            candidate {
                content {
                    part { text { "This is a test." } }
                }
                finishReason = "STOP"
            }

        assertNotNull(candidate.content)
        assertEquals("STOP", candidate.finishReason)
        assertNull(candidate.safetyRatings)
        assertNull(candidate.citationMetadata)
        assertNull(candidate.tokenCount)
        assertNull(candidate.avgLogprobs)
        assertNull(candidate.logprobsResult)
        assertEquals(0, candidate.groundingAttributions.size)
        assertNull(candidate.urlContextMetadata)
    }

    @Test
    fun `build with urlContextMetadata`() {
        val candidate =
            candidate {
                content {
                    part { text { "This is a test." } }
                }
                finishReason = "STOP"
                urlContextMetadata =
                    UrlContextMetadata(
                        urlMetadata =
                            listOf(
                                UrlMetadata(
                                    retrievedUrl = "http://example.com",
                                    urlRetrievalStatus = "SUCCESS",
                                ),
                            ),
                    )
            }

        assertNotNull(candidate.urlContextMetadata)
        assertEquals(1, candidate.urlContextMetadata!!.urlMetadata.size)
        assertEquals("http://example.com", candidate.urlContextMetadata!!.urlMetadata[0].retrievedUrl)
    }

    @Test
    fun `build with multiple safety ratings`() {
        val candidate =
            candidate {
                content {
                    part { text { "This is a test." } }
                }
                finishReason = "STOP"
                safetyRating {
                    category = HarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT
                    probability = HarmProbability.NEGLIGIBLE
                }
                safetyRating {
                    category = HarmCategory.HARM_CATEGORY_HARASSMENT
                    probability = HarmProbability.LOW
                }
            }

        assertEquals(2, candidate.safetyRatings?.size)
    }

    @Test
    fun `build without required properties throws exception`() {
        assertThrows<UninitializedPropertyAccessException> {
            candidate {
                finishReason = "STOP"
            }
        }

        assertThrows<UninitializedPropertyAccessException> {
            candidate {
                index = 1
            }
        }
    }
}
