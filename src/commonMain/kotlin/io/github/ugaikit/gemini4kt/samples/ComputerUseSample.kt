package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.Environment
import io.github.ugaikit.gemini4kt.Gemini
import io.github.ugaikit.gemini4kt.GeminiAI
import io.github.ugaikit.gemini4kt.generateContentRequest
import io.github.ugaikit.gemini4kt.getApiKey
import io.github.ugaikit.gemini4kt.interaction.CreateInteractionRequest
import io.github.ugaikit.gemini4kt.interaction.InteractionTool

/**
 * Demonstrates GA Computer Use requests through both supported APIs.
 */
object ComputerUseSample {
    /**
     * Requests a suggested Computer Use action through the Interactions API.
     *
     * If `gemini` is null, a new GeminiAI client is created using `getApiKey()`.
     *
     * @param gemini Optional GeminiAI client to use for the request; when null a client
     *   is constructed from `getApiKey()`.
     */
    suspend fun run(
        gemini: GeminiAI? = null,
        model: String = "gemini-4-hydrogen",
    ) {
        val client = gemini ?: GeminiAI(apiKey = getApiKey())
        try {
            val request =
                CreateInteractionRequest(
                    model = model,
                    input = "Search for 'Gemini API' on Google.",
                    tools =
                        arrayOf(
                            InteractionTool(
                                type = "computer_use",
                                environment = "browser",
                                enablePromptInjectionDetection = true,
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

    /**
     * Requests a suggested action through Generate Content. The caller implements UI execution.
     */
    suspend fun runGenerateContent(
        gemini: Gemini? = null,
        model: String = "gemini-4-hydrogen",
    ) {
        val client = gemini ?: Gemini(getApiKey())
        try {
            val request =
                generateContentRequest {
                    content { part { text { "Search for 'Gemini API' on Google." } } }
                    tool {
                        computerUse {
                            environment = Environment.ENVIRONMENT_BROWSER
                            enablePromptInjectionDetection = true
                        }
                    }
                    store = false
                }
            println(client.generateContent(request, model))
        } finally {
            if (gemini == null) {
                client.close()
            }
        }
    }
}
