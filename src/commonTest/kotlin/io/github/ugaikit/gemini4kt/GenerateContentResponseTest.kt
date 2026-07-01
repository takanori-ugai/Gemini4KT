package io.github.ugaikit.gemini4kt

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GenerateContentResponseTest {
    @Test
    fun getTextReturnsFirstNonThoughtTextFromFirstCandidate() {
        val response =
            GenerateContentResponse(
                candidates =
                    listOf(
                        Candidate(
                            content =
                                Content(
                                    parts =
                                        arrayOf(
                                            Part(text = "reasoning", thought = true),
                                            Part(text = "final answer"),
                                        ),
                                ),
                        ),
                    ),
            )

        assertEquals("final answer", response.getText())
    }

    @Test
    fun getTextSearchesAcrossCandidates() {
        val response =
            GenerateContentResponse(
                candidates =
                    listOf(
                        Candidate(
                            content = Content(parts = arrayOf(Part(text = "thought", thought = true))),
                        ),
                        Candidate(
                            content = Content(parts = arrayOf(Part(text = "fallback answer"))),
                        ),
                    ),
            )

        assertEquals("fallback answer", response.getText())
    }

    @Test
    fun getTextReturnsNullWhenNoNonThoughtTextExists() {
        val response =
            GenerateContentResponse(
                candidates =
                    listOf(
                        Candidate(
                            content =
                                Content(
                                    parts =
                                        arrayOf(
                                            Part(text = "only thought", thought = true),
                                            Part(functionCall = FunctionCall(name = "tool", args = emptyMap())),
                                        ),
                                ),
                        ),
                    ),
            )

        assertNull(response.getText())
    }

    @Test
    fun getThoughtReturnsFirstThoughtTextFromFirstCandidate() {
        val response =
            GenerateContentResponse(
                candidates =
                    listOf(
                        Candidate(
                            content =
                                Content(
                                    parts =
                                        arrayOf(
                                            Part(text = "chain of thought", thought = true),
                                            Part(text = "final answer"),
                                        ),
                                ),
                        ),
                    ),
            )

        assertEquals("chain of thought", response.getThought())
    }

    @Test
    fun getThoughtReturnsNullWhenNoThoughtTextExists() {
        val response =
            GenerateContentResponse(
                candidates =
                    listOf(
                        Candidate(
                            content =
                                Content(
                                    parts =
                                        arrayOf(
                                            Part(text = "final answer"),
                                            Part(functionCall = FunctionCall(name = "tool", args = emptyMap())),
                                        ),
                                ),
                        ),
                    ),
            )

        assertNull(response.getThought())
    }

    @Test
    fun deserializesWithoutCandidates() {
        val response =
            kotlinx.serialization.json.Json
                .decodeFromString<GenerateContentResponse>("{}")
        assertTrue(response.candidates.isEmpty())
    }
}
