package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.GeminiAI
import io.github.ugaikit.gemini4kt.getApiKey
import io.github.ugaikit.gemini4kt.interaction.CreateInteractionRequest
import io.github.ugaikit.gemini4kt.interaction.InteractionTool
import kotlinx.serialization.json.JsonPrimitive

/**
 * Represents the legacy computer use sample for Gemini 2.5.
 */
object ComputerUseSample {
    /**
     * Sends a small Computer Use request using the legacy Interactions API.
     *
     * If `gemini` is null, a new GeminiAI client is created using `getApiKey()`.
     *
     * @param gemini Optional GeminiAI client to use for the request; when null a client
     *   is constructed from `getApiKey()`.
     */
    suspend fun run(gemini: GeminiAI? = null) {
        val client = gemini ?: GeminiAI(apiKey = getApiKey())
        try {
            val request =
                CreateInteractionRequest(
                    model = "gemini-2.5-computer-use-preview-10-2025",
                    input = JsonPrimitive("Search for 'Gemini API' on Google."),
                    tools =
                        arrayOf(
                            InteractionTool(
                                type = "computer_use",
                                environment = "browser",
                                excludedPredefinedFunctions = arrayOf("CLICK"),
                            ),
                        ),
                )

            val response =
                client.createInteraction(request)

            println(response)
        } finally {
            if (gemini == null) {
                client.close()
            }
        }
    }
}
