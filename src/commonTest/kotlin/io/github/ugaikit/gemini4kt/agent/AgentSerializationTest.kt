package io.github.ugaikit.gemini4kt.agent

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class AgentSerializationTest {
    private val json =
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = false
        }

    @Test
    fun agentSerializesCorrectly() {
        val agent =
            Agent(
                id = "math-agent",
                baseAgent = "antigravity-preview-05-2026",
                systemInstruction = "Help with math",
                baseEnvironment =
                    AgentEnvironment(
                        type = "remote",
                        sources =
                            arrayOf(
                                AgentSource(
                                    type = "inline",
                                    target = ".agents/AGENTS.md",
                                    content = "Write a summary table",
                                ),
                                AgentSource(
                                    type = "repository",
                                    source = "https://github.com/test",
                                    target = ".agents/skills",
                                ),
                            ),
                    ),
                created = "2026-05-22T00:00:00Z",
                updated = "2026-05-22T01:00:00Z",
            )

        val encoded = json.encodeToString(agent)
        val expected =
            """
            {
              "id": "math-agent",
              "base_agent": "antigravity-preview-05-2026",
              "system_instruction": "Help with math",
              "base_environment": {
                "type": "remote",
                "sources": [
                  {
                    "type": "inline",
                    "target": ".agents/AGENTS.md",
                    "content": "Write a summary table"
                  },
                  {
                    "type": "repository",
                    "source": "https://github.com/test",
                    "target": ".agents/skills"
                  }
                ]
              },
              "created": "2026-05-22T00:00:00Z",
              "updated": "2026-05-22T01:00:00Z"
            }
            """.trimIndent()

        assertEquals(json.parseToJsonElement(expected), json.parseToJsonElement(encoded))

        val decoded = json.decodeFromString<Agent>(encoded)
        assertEquals(agent, decoded)
    }

    @Test
    fun createAgentRequestSerializesCorrectly() {
        val request =
            CreateAgentRequest(
                id = "coder",
                baseAgent = "antigravity-preview-05-2026",
                systemInstruction = "Help with code",
            )
        val encoded = json.encodeToString(request)
        val expected =
            """
            {
              "id": "coder",
              "base_agent": "antigravity-preview-05-2026",
              "system_instruction": "Help with code"
            }
            """.trimIndent()

        assertEquals(json.parseToJsonElement(expected), json.parseToJsonElement(encoded))
    }

    @Test
    fun listAgentsResponseSerializesCorrectly() {
        val agent = Agent(id = "agent-1")
        val response = ListAgentsResponse(agents = arrayOf(agent), nextPageToken = "next-token")
        val encoded = json.encodeToString(response)
        val expected =
            """
            {
              "agents": [
                {
                  "id": "agent-1"
                }
              ],
              "nextPageToken": "next-token"
            }
            """.trimIndent()

        assertEquals(json.parseToJsonElement(expected), json.parseToJsonElement(encoded))

        val decoded = json.decodeFromString<ListAgentsResponse>(encoded)
        assertEquals(response, decoded)
    }
}
