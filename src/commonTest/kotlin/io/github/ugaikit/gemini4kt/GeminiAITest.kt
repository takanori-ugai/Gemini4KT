package io.github.ugaikit.gemini4kt

import io.github.ugaikit.gemini4kt.agent.CreateAgentRequest
import io.github.ugaikit.gemini4kt.interaction.CreateInteractionRequest
import io.github.ugaikit.gemini4kt.interaction.InteractionStatus
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GeminiAITest {
    companion object {
        private const val EXPECTED_API_REVISION = "2026-05-20"
    }

    private fun createGeminiAI(handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData): GeminiAI {
        val client =
            HttpClient(MockEngine) {
                engine {
                    addHandler(handler)
                }
                install(ContentNegotiation) {
                    json(
                        Json {
                            ignoreUnknownKeys = true
                            encodeDefaults = false
                            explicitNulls = false
                        },
                    )
                }
            }
        return GeminiAI(client = client, apiKey = "test-key")
    }

    @Test
    fun testCreateInteraction() =
        runTest {
            val responseJson =
                """
                {
                  "id": "v1_123",
                  "status": "completed",
                  "outputs": [
                    {
                      "type": "text",
                      "text": "Hello world"
                    }
                  ]
                }
                """.trimIndent()

            val geminiAI =
                createGeminiAI { request ->
                    assertEquals(HttpMethod.Post, request.method)
                    assertTrue(request.url.toString().contains("v1beta/interactions"))
                    respond(
                        content = responseJson,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )
                }

            val request =
                CreateInteractionRequest(
                    model = "gemini-2.5-flash",
                    input = JsonPrimitive("Hello"),
                )
            val interaction = geminiAI.createInteraction(request)

            assertEquals("v1_123", interaction.id)
            assertEquals(InteractionStatus.COMPLETED, interaction.status)
            assertEquals(1, interaction.outputs?.size)
            assertEquals("text", interaction.outputs?.get(0)?.type)
            assertEquals("Hello world", interaction.outputs?.get(0)?.text)
        }

    @Test
    fun testGetInteraction() =
        runTest {
            val responseJson =
                """
                {
                  "id": "v1_123",
                  "status": "completed"
                }
                """.trimIndent()

            val geminiAI =
                createGeminiAI { request ->
                    assertEquals(HttpMethod.Get, request.method)
                    assertTrue(request.url.toString().contains("v1beta/interactions/v1_123"))
                    respond(
                        content = responseJson,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )
                }

            val interaction = geminiAI.getInteraction("v1_123")
            assertEquals("v1_123", interaction.id)
            assertEquals(InteractionStatus.COMPLETED, interaction.status)
        }

    @Test
    fun testCancelInteraction() =
        runTest {
            val responseJson =
                """
                {
                  "id": "v1_123",
                  "status": "cancelled"
                }
                """.trimIndent()

            val geminiAI =
                createGeminiAI { request ->
                    assertEquals(HttpMethod.Post, request.method)
                    assertTrue(request.url.toString().contains("v1beta/interactions/v1_123/cancel"))
                    respond(
                        content = responseJson,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )
                }

            val interaction = geminiAI.cancelInteraction("v1_123")
            assertEquals("v1_123", interaction.id)
            assertEquals(InteractionStatus.CANCELLED, interaction.status)
        }

    @Test
    fun testDeleteInteraction() =
        runTest {
            val geminiAI =
                createGeminiAI { request ->
                    assertEquals(HttpMethod.Delete, request.method)
                    assertTrue(request.url.toString().contains("v1beta/interactions/v1_123"))
                    respond(
                        content = "",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )
                }

            geminiAI.deleteInteraction("v1_123")
        }

    @Test
    fun testStreamInteraction() =
        runTest {
            val responseSSE =
                """
                data: {"id": "v1_123", "status": "in_progress"}
                data: {"id": "v1_123", "status": "completed"}
                """.trimIndent() + "\n"

            val geminiAI =
                createGeminiAI { request ->
                    assertEquals(HttpMethod.Post, request.method)
                    assertTrue(request.url.toString().contains("v1beta/interactions"))
                    assertTrue(request.url.toString().contains("alt=sse"))
                    assertEquals(EXPECTED_API_REVISION, request.headers["Api-Revision"])
                    respond(
                        content = responseSSE,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Text.EventStream.toString()),
                    )
                }

            val request =
                CreateInteractionRequest(
                    model = "gemini-2.5-flash",
                    input = JsonPrimitive("Hello"),
                )
            val events = geminiAI.streamInteraction(request).toList()

            assertEquals(2, events.size)
            assertEquals("v1_123", events[0].jsonObject["id"]?.jsonPrimitive?.content)
            assertEquals("in_progress", events[0].jsonObject["status"]?.jsonPrimitive?.content)
            assertEquals("completed", events[1].jsonObject["status"]?.jsonPrimitive?.content)
        }

    @Test
    fun testDownloadEnvironmentFiles() =
        runTest {
            val responseBytes = byteArrayOf(1, 2, 3, 4)
            val geminiAI =
                createGeminiAI { request ->
                    assertEquals(HttpMethod.Get, request.method)
                    assertTrue(request.url.toString().contains("v1beta/files/env_123"))
                    assertEquals(EXPECTED_API_REVISION, request.headers["Api-Revision"])
                    respond(
                        content = responseBytes,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.OctetStream.toString()),
                    )
                }

            val result = geminiAI.downloadEnvironmentFiles("env_123")
            assertContentEquals(responseBytes, result)
        }

    @Test
    fun testCreateAgent() =
        runTest {
            val responseJson =
                """
                {
                  "id": "fibonacci-analyst",
                  "base_agent": "antigravity-preview-05-2026"
                }
                """.trimIndent()

            val geminiAI =
                createGeminiAI { request ->
                    assertEquals(HttpMethod.Post, request.method)
                    assertTrue(request.url.toString().contains("v1beta/agents"))
                    assertEquals(EXPECTED_API_REVISION, request.headers["Api-Revision"])
                    respond(
                        content = responseJson,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )
                }

            val request = CreateAgentRequest(id = "fibonacci-analyst", baseAgent = "antigravity-preview-05-2026")
            val agent = geminiAI.createAgent(request)
            assertEquals("fibonacci-analyst", agent.id)
            assertEquals("antigravity-preview-05-2026", agent.baseAgent)
        }

    @Test
    fun testGetAgent() =
        runTest {
            val responseJson =
                """
                {
                  "id": "fibonacci-analyst",
                  "base_agent": "antigravity-preview-05-2026"
                }
                """.trimIndent()

            val geminiAI =
                createGeminiAI { request ->
                    assertEquals(HttpMethod.Get, request.method)
                    assertTrue(request.url.toString().contains("v1beta/agents/fibonacci-analyst"))
                    assertEquals(EXPECTED_API_REVISION, request.headers["Api-Revision"])
                    respond(
                        content = responseJson,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )
                }

            val agent = geminiAI.getAgent("fibonacci-analyst")
            assertEquals("fibonacci-analyst", agent.id)
        }

    @Test
    fun testDeleteAgent() =
        runTest {
            val geminiAI =
                createGeminiAI { request ->
                    assertEquals(HttpMethod.Delete, request.method)
                    assertTrue(request.url.toString().contains("v1beta/agents/fibonacci-analyst"))
                    assertEquals(EXPECTED_API_REVISION, request.headers["Api-Revision"])
                    respond(
                        content = "",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )
                }

            geminiAI.deleteAgent("fibonacci-analyst")
        }

    @Test
    fun testListAgents() =
        runTest {
            val responseJson =
                """
                {
                  "agents": [
                    {
                      "id": "fibonacci-analyst"
                    }
                  ],
                  "nextPageToken": "token123"
                }
                """.trimIndent()

            val geminiAI =
                createGeminiAI { request ->
                    assertEquals(HttpMethod.Get, request.method)
                    assertTrue(request.url.toString().contains("v1beta/agents"))
                    assertTrue(request.url.toString().contains("pageSize=5"))
                    assertTrue(request.url.toString().contains("pageToken=start"))
                    assertEquals(EXPECTED_API_REVISION, request.headers["Api-Revision"])
                    respond(
                        content = responseJson,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )
                }

            val result = geminiAI.listAgents(pageSize = 5, pageToken = "start")
            assertEquals(1, result.agents.size)
            assertEquals("fibonacci-analyst", result.agents[0].id)
            assertEquals("token123", result.nextPageToken)
        }

    @Test
    fun testStreamInteractionError() =
        runTest {
            val responseJson =
                """
                {
                  "error": {
                    "code": 400,
                    "message": "Invalid request",
                    "status": "INVALID_ARGUMENT"
                  }
                }
                """.trimIndent()

            val geminiAI =
                createGeminiAI { request ->
                    respond(
                        content = responseJson,
                        status = HttpStatusCode.BadRequest,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )
                }

            val request =
                CreateInteractionRequest(
                    model = "gemini-2.5-flash",
                    input = JsonPrimitive("Hello"),
                )

            try {
                geminiAI.streamInteraction(request).toList()
                assertTrue(false, "Should have thrown GeminiException")
            } catch (e: GeminiException) {
                assertEquals(400, e.error.code)
                assertEquals("Invalid request", e.error.message)
            }
        }

    @Test
    fun testStreamInteractionNonDataLine() =
        runTest {
            val responseSSE =
                """
                
                : this is a comment/ping line
                data: {"id": "v1_123", "status": "completed"}
                
                """.trimIndent() + "\n"

            val geminiAI =
                createGeminiAI { request ->
                    respond(
                        content = responseSSE,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Text.EventStream.toString()),
                    )
                }

            val request =
                CreateInteractionRequest(
                    model = "gemini-2.5-flash",
                    input = JsonPrimitive("Hello"),
                )
            val events = geminiAI.streamInteraction(request).toList()

            assertEquals(1, events.size)
            assertEquals("v1_123", events[0].jsonObject["id"]?.jsonPrimitive?.content)
        }

    @Test
    fun testDownloadEnvironmentFilesError() =
        runTest {
            val responseJson =
                """
                {
                  "error": {
                    "code": 404,
                    "message": "Files not found",
                    "status": "NOT_FOUND"
                  }
                }
                """.trimIndent()

            val geminiAI =
                createGeminiAI { request ->
                    respond(
                        content = responseJson,
                        status = HttpStatusCode.NotFound,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )
                }

            try {
                geminiAI.downloadEnvironmentFiles("env_notFound")
                assertTrue(false, "Should have thrown GeminiException")
            } catch (e: GeminiException) {
                assertEquals(404, e.error.code)
                assertEquals("Files not found", e.error.message)
            }
        }

    @Test
    fun testCreateAgentError() =
        runTest {
            val responseJson =
                """
                {
                  "error": {
                    "code": 400,
                    "message": "Missing environment",
                    "status": "INVALID_ARGUMENT"
                  }
                }
                """.trimIndent()

            val geminiAI =
                createGeminiAI { request ->
                    respond(
                        content = responseJson,
                        status = HttpStatusCode.BadRequest,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )
                }

            val request = CreateAgentRequest(id = "fibonacci-analyst", baseAgent = "antigravity-preview-05-2026")
            try {
                geminiAI.createAgent(request)
                assertTrue(false, "Should have thrown GeminiException")
            } catch (e: GeminiException) {
                assertEquals(400, e.error.code)
                assertEquals("Missing environment", e.error.message)
            }
        }

    @Test
    fun testGetAgentError() =
        runTest {
            val responseJson =
                """
                {
                  "error": {
                    "code": 404,
                    "message": "Agent not found",
                    "status": "NOT_FOUND"
                  }
                }
                """.trimIndent()

            val geminiAI =
                createGeminiAI { request ->
                    respond(
                        content = responseJson,
                        status = HttpStatusCode.NotFound,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )
                }

            try {
                geminiAI.getAgent("non-existent")
                assertTrue(false, "Should have thrown GeminiException")
            } catch (e: GeminiException) {
                assertEquals(404, e.error.code)
                assertEquals("Agent not found", e.error.message)
            }
        }

    @Test
    fun testDeleteAgentError() =
        runTest {
            val responseJson =
                """
                {
                  "error": {
                    "code": 403,
                    "message": "Permission denied",
                    "status": "PERMISSION_DENIED"
                  }
                }
                """.trimIndent()

            val geminiAI =
                createGeminiAI { request ->
                    respond(
                        content = responseJson,
                        status = HttpStatusCode.Forbidden,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )
                }

            try {
                geminiAI.deleteAgent("unauthorized")
                assertTrue(false, "Should have thrown GeminiException")
            } catch (e: GeminiException) {
                assertEquals(403, e.error.code)
                assertEquals("Permission denied", e.error.message)
            }
        }

    @Test
    fun testListAgentsError() =
        runTest {
            val responseJson =
                """
                {
                  "error": {
                    "code": 500,
                    "message": "Internal error",
                    "status": "INTERNAL"
                  }
                }
                """.trimIndent()

            val geminiAI =
                createGeminiAI { request ->
                    respond(
                        content = responseJson,
                        status = HttpStatusCode.InternalServerError,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )
                }

            try {
                geminiAI.listAgents()
                assertTrue(false, "Should have thrown GeminiException")
            } catch (e: GeminiException) {
                assertEquals(500, e.error.code)
                assertEquals("Internal error", e.error.message)
            }
        }

    @Test
    fun testListAgentsDefaultPageToken() =
        runTest {
            val responseJson =
                """
                {
                  "agents": [],
                  "nextPageToken": ""
                }
                """.trimIndent()

            val geminiAI =
                createGeminiAI { request ->
                    assertEquals(HttpMethod.Get, request.method)
                    assertTrue(request.url.toString().contains("v1beta/agents"))
                    assertTrue(request.url.toString().contains("pageSize=10"))
                    val queryParams = request.url.parameters
                    assertTrue(!queryParams.contains("pageToken"))
                    respond(
                        content = responseJson,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )
                }

            val result = geminiAI.listAgents()
            assertEquals(0, result.agents.size)
        }

    @Test
    fun testCreateInteractionError() =
        runTest {
            val responseJson =
                """
                {
                  "error": {
                    "code": 400,
                    "message": "Invalid model",
                    "status": "INVALID_ARGUMENT"
                  }
                }
                """.trimIndent()

            val geminiAI =
                createGeminiAI { request ->
                    respond(
                        content = responseJson,
                        status = HttpStatusCode.BadRequest,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )
                }

            val request = CreateInteractionRequest(model = "invalid-model", input = JsonPrimitive("Hello"))
            try {
                geminiAI.createInteraction(request)
                assertTrue(false, "Should have thrown GeminiException")
            } catch (e: GeminiException) {
                assertEquals(400, e.error.code)
                assertEquals("Invalid model", e.error.message)
            }
        }

    @Test
    fun testGetInteractionError() =
        runTest {
            val responseJson =
                """
                {
                  "error": {
                    "code": 404,
                    "message": "Interaction not found",
                    "status": "NOT_FOUND"
                  }
                }
                """.trimIndent()

            val geminiAI =
                createGeminiAI { request ->
                    respond(
                        content = responseJson,
                        status = HttpStatusCode.NotFound,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )
                }

            try {
                geminiAI.getInteraction("nonexistent-interaction")
                assertTrue(false, "Should have thrown GeminiException")
            } catch (e: GeminiException) {
                assertEquals(404, e.error.code)
                assertEquals("Interaction not found", e.error.message)
            }
        }

    @Test
    fun testCancelInteractionError() =
        runTest {
            val responseJson =
                """
                {
                  "error": {
                    "code": 409,
                    "message": "Interaction already completed",
                    "status": "FAILED_PRECONDITION"
                  }
                }
                """.trimIndent()

            val geminiAI =
                createGeminiAI { request ->
                    respond(
                        content = responseJson,
                        status = HttpStatusCode.Conflict,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )
                }

            try {
                geminiAI.cancelInteraction("completed-interaction")
                assertTrue(false, "Should have thrown GeminiException")
            } catch (e: GeminiException) {
                assertEquals(409, e.error.code)
                assertEquals("Interaction already completed", e.error.message)
            }
        }

    @Test
    fun testDeleteInteractionError() =
        runTest {
            val responseJson =
                """
                {
                  "error": {
                    "code": 403,
                    "message": "Permission denied to delete interaction",
                    "status": "PERMISSION_DENIED"
                  }
                }
                """.trimIndent()

            val geminiAI =
                createGeminiAI { request ->
                    respond(
                        content = responseJson,
                        status = HttpStatusCode.Forbidden,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )
                }

            try {
                geminiAI.deleteInteraction("some-interaction")
                assertTrue(false, "Should have thrown GeminiException")
            } catch (e: GeminiException) {
                assertEquals(403, e.error.code)
                assertEquals("Permission denied to delete interaction", e.error.message)
            }
        }
}
