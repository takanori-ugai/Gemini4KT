package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.GeminiAI
import io.github.ugaikit.gemini4kt.getApiKey
import io.github.ugaikit.gemini4kt.interaction.CreateInteractionRequest
import io.github.ugaikit.gemini4kt.interaction.EnvironmentConfig
import io.github.ugaikit.gemini4kt.interaction.InteractionTool
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private const val ANTIGRAVITY_AGENT = "antigravity-preview-09-2026"

/**
 * Demonstrates Google Search through the Antigravity managed agent.
 *
 * The agent decides which search queries to issue. The generated
 * `google_search_call` steps expose those queries in their `arguments`.
 */
object AntigravityGoogleSearchSample {
    /**
     * Runs the Antigravity Google Search sample.
     *
     * @param client Optional Gemini AI client.
     */
    suspend fun run(client: GeminiAI? = null) {
        val ai = client ?: GeminiAI(apiKey = getApiKey())
        try {
            val interaction =
                ai.createInteraction(
                    CreateInteractionRequest(
                        agent = ANTIGRAVITY_AGENT,
                        input =
                            "Use Google Search to find the latest official Gemini API Interactions API documentation " +
                                "and summarize the key capabilities in three bullet points.",
                        tools = arrayOf(InteractionTool(type = "google_search")),
                        environment = EnvironmentConfig(),
                        stream = false,
                    ),
                )

            println("--- Antigravity Google Search ---")
            println("Interaction id: ${interaction.id}")
            println("Status: ${interaction.status}")

            val searchCalls = interaction.steps.orEmpty().filter { it.type == "google_search_call" }
            if (searchCalls.isEmpty()) {
                println("No google_search_call steps returned.")
            } else {
                searchCalls.forEachIndexed { index, step ->
                    val queries =
                        step.arguments
                            ?.jsonObject
                            ?.get("queries")
                            ?.jsonArray
                            ?.map { it.jsonPrimitive.content }
                            .orEmpty()
                    println("google_search[$index](queries=${queries.joinToString()})")
                }
            }

            println("Output: ${interaction.outputText ?: "N/A"}")
        } finally {
            if (client == null) {
                ai.close()
            }
        }
    }
}
