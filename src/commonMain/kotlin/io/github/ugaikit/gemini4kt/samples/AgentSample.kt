package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.GeminiAI
import io.github.ugaikit.gemini4kt.agent.CreateAgentRequest
import io.github.ugaikit.gemini4kt.interaction.CreateInteractionRequest
import io.github.ugaikit.gemini4kt.interaction.EnvironmentConfig
import kotlin.random.Random

/**
 * Represents a minimal Agent API sample.
 */
object AgentSample {
    private const val BASE_AGENT = "antigravity-preview-05-2026"

    /**
     * Creates an agent, uses it for one interaction, then deletes it.
     *
     * @param client The Gemini AI client to use. A client is created from `GEMINI_API_KEY` when omitted.
     */
    suspend fun run(client: GeminiAI? = null) {
        withGeminiAIClient(client) { ai ->
            val agentId = "agent-sample-${Random.nextInt(100000, 999999)}"
            val createRequest =
                CreateAgentRequest(
                    id = agentId,
                    baseAgent = BASE_AGENT,
                    systemInstruction = "You are a concise assistant that answers with short, direct output.",
                )

            var created = false
            try {
                println("--- Agent API Sample ---")

                val createdAgent = ai.createAgent(createRequest)
                created = true
                println("Created agent: ${createdAgent.id}")
                println("Base agent: ${createdAgent.baseAgent ?: "N/A"}")

                val retrievedAgent = ai.getAgent(agentId)
                println("Retrieved agent: ${retrievedAgent.id}")

                val listResponse = ai.listAgents(pageSize = 5)
                println("First page agent ids: ${listResponse.agents.joinToString { it.id }}")

                val interaction =
                    ai.createInteraction(
                        CreateInteractionRequest(
                            agent = agentId,
                            input = "Explain what this agent does in one sentence.",
                            environment = EnvironmentConfig(),
                        ),
                    )
                println("Interaction id: ${interaction.id}")
                println("Interaction status: ${interaction.status.name.lowercase()}")
                println("Interaction output: ${interaction.outputText ?: "N/A"}")
            } finally {
                if (created) {
                    try {
                        ai.deleteAgent(agentId)
                        println("Deleted agent: $agentId")
                    } catch (e: Exception) {
                        println("Agent cleanup failed: ${e.message}")
                    }
                }
            }
        }
    }
}
