package io.github.ugaikit.gemini4kt

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TopCandidatesTest {
    private val json =
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = false
        }

    @Test
    fun topCandidatesRoundTripSerialization() {
        val topCandidates =
            TopCandidates(
                candidates =
                    listOf(
                        LogprobCandidate(
                            token = "hello",
                            tokenId = 123,
                            logProbability = -0.5,
                        ),
                    ),
            )

        val encoded = json.encodeToString(topCandidates)
        assertTrue(encoded.contains("candidates"))
        val decoded = json.decodeFromString<TopCandidates>(encoded)
        assertEquals(
            "hello",
            decoded.candidates
                .first()
                .token,
        )
    }
}
