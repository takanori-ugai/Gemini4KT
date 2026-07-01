package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.GeminiAI
import io.github.ugaikit.gemini4kt.getApiKey
import io.github.ugaikit.gemini4kt.interaction.CreateInteractionRequest
import io.github.ugaikit.gemini4kt.interaction.InteractionTool
import kotlinx.serialization.json.JsonPrimitive

/**
 * Represents a Computer Use sample using gemini-3.5-flash and the Interactions API.
 */
object ComputerUseGemini35Sample {
    /**
     * Sends a small Computer Use request using the Interactions API.
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
                    model = "gemini-3.5-flash",
                    input = JsonPrimitive("Search for 'Gemini API' on Google."),
                    tools =
                        arrayOf(
                            InteractionTool(
                                type = "computer_use",
                                environment = "browser",
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
