package io.github.ugaikit.gemini4kt.interaction

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InteractionModelsTest {
    private val json = Json { encodeDefaults = false }

    @Test
    fun interactionTurnRoundTripsThroughJson() {
        val turn = InteractionTurn(role = "user", content = JsonPrimitive("hello"))

        val encoded = json.encodeToString(turn)
        val decoded = json.decodeFromString<InteractionTurn>(encoded)

        assertEquals(turn, decoded)
        assertTrue(encoded.contains("\"role\":\"user\""))
    }

    @Test
    fun interactionUsageRoundTripsWithModalityArrays() {
        val usage =
            InteractionUsage(
                totalInputTokens = 10,
                inputTokensByModality =
                    arrayOf(
                        InteractionModalityTokenCount(InteractionResponseModality.TEXT, 8),
                        InteractionModalityTokenCount(InteractionResponseModality.AUDIO, 2),
                    ),
                totalTokens = 12,
            )

        val encoded = json.encodeToString(usage)
        val decoded = json.decodeFromString<InteractionUsage>(encoded)

        assertEquals(usage, decoded)
        assertEquals(usage.hashCode(), decoded.hashCode())
    }

    @Test
    fun interactionToolEqualsUsesArrayContent() {
        val left =
            InteractionTool(
                type = "file_search",
                fileSearchStoreNames = arrayOf("store-1", "store-2"),
                allowedTools = arrayOf(InteractionAllowedTools(mode = "auto", tools = arrayOf("search"))),
            )
        val right =
            InteractionTool(
                type = "file_search",
                fileSearchStoreNames = arrayOf("store-1", "store-2"),
                allowedTools = arrayOf(InteractionAllowedTools(mode = "auto", tools = arrayOf("search"))),
            )

        assertEquals(left, right)
        assertEquals(left.hashCode(), right.hashCode())
    }

    @Test
    fun createInteractionRequestSerializesSnakeCaseKeys() {
        val request =
            CreateInteractionRequest(
                systemInstruction = "stay concise",
                responseMimeType = "application/json",
                previousInteractionId = "int_prev",
            )

        val encoded = json.encodeToString(request)

        assertTrue(encoded.contains("\"system_instruction\":\"stay concise\""))
        assertTrue(encoded.contains("\"response_mime_type\":\"application/json\""))
        assertTrue(encoded.contains("\"previous_interaction_id\":\"int_prev\""))
    }
}
