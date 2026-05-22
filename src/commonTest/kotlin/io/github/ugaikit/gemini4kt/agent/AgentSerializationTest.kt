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

    @Test
    fun testEqualsAndHashCode() {
        val source1 = AgentSource("inline", "target1", "content1", "source1")
        val source2 = AgentSource("inline", "target1", "content1", "source1")
        val sourceDifferent = AgentSource("inline", "target2", "content1", "source1")

        val env1 = AgentEnvironment("remote", arrayOf(source1))
        val env2 = AgentEnvironment("remote", arrayOf(source2))
        val envDifferentType = AgentEnvironment("local", arrayOf(source1))
        val envDifferentSources = AgentEnvironment("remote", arrayOf(sourceDifferent))
        val envNullSources1 = AgentEnvironment("remote", null)
        val envNullSources2 = AgentEnvironment("remote", null)

        // Test AgentEnvironment equals and hashCode
        assertEquals(env1, env1)
        assertEquals(env1, env2)
        assertEquals(env1.hashCode(), env2.hashCode())
        assertEquals(envNullSources1, envNullSources2)
        assertEquals(envNullSources1.hashCode(), envNullSources2.hashCode())

        // Check inequality cases for AgentEnvironment
        kotlin.test.assertNotEquals(env1, null as AgentEnvironment?)
        kotlin.test.assertNotEquals(env1, "not an environment" as Any?)
        kotlin.test.assertNotEquals(env1, envDifferentType)
        kotlin.test.assertNotEquals(env1, envDifferentSources)
        kotlin.test.assertNotEquals(env1, envNullSources1)
        kotlin.test.assertNotEquals(envNullSources1, env1)

        val agent1 = Agent("1", "base", "sys", env1, "created", "updated")
        val agent2 = Agent("1", "base", "sys", env2, "created", "updated")
        val agentDiffId = Agent("2", "base", "sys", env1, "created", "updated")
        val agentDiffBase = Agent("1", "base2", "sys", env1, "created", "updated")
        val agentDiffSys = Agent("1", "base", "sys2", env1, "created", "updated")
        val agentDiffEnv = Agent("1", "base", "sys", envDifferentType, "created", "updated")
        val agentDiffCreated = Agent("1", "base", "sys", env1, "created2", "updated")
        val agentDiffUpdated = Agent("1", "base", "sys", env1, "created", "updated2")

        // Test Agent equals and hashCode
        assertEquals(agent1, agent1)
        assertEquals(agent1, agent2)
        assertEquals(agent1.hashCode(), agent2.hashCode())

        // Check inequality cases for Agent
        kotlin.test.assertNotEquals(agent1, null as Agent?)
        kotlin.test.assertNotEquals(agent1, "not an agent" as Any?)
        kotlin.test.assertNotEquals(agent1, agentDiffId)
        kotlin.test.assertNotEquals(agent1, agentDiffBase)
        kotlin.test.assertNotEquals(agent1, agentDiffSys)
        kotlin.test.assertNotEquals(agent1, agentDiffEnv)
        kotlin.test.assertNotEquals(agent1, agentDiffCreated)
        kotlin.test.assertNotEquals(agent1, agentDiffUpdated)

        // Test ListAgentsResponse equals and hashCode
        val response1 = ListAgentsResponse(arrayOf(agent1), "token")
        val response2 = ListAgentsResponse(arrayOf(agent2), "token")
        val responseDiffAgents = ListAgentsResponse(arrayOf(agentDiffId), "token")
        val responseDiffToken = ListAgentsResponse(arrayOf(agent1), "token2")

        assertEquals(response1, response1)
        assertEquals(response1, response2)
        assertEquals(response1.hashCode(), response2.hashCode())

        kotlin.test.assertNotEquals(response1, null as ListAgentsResponse?)
        kotlin.test.assertNotEquals(response1, "not a response" as Any?)
        kotlin.test.assertNotEquals(response1, responseDiffAgents)
        kotlin.test.assertNotEquals(response1, responseDiffToken)
    }
}
