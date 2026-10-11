package io.github.ugaikit.gemini4kt

import io.github.ugaikit.gemini4kt.interaction.CreateInteractionRequest
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@GeminiFunction(description = "Add two integers for an interaction function-binding test")
private fun addInteractionNumbers(
    @GeminiParameter(description = "First integer") a: Int,
    @GeminiParameter(description = "Second integer") b: Int,
): Int = a + b

class GeminiAIInteractionFunctionBindingTest {
    @Test
    fun executesFunctionAndSubmitsResultBeforeReturningFinalInteraction() =
        runTest {
            val requestBodies = mutableListOf<String>()
            val (ai, client) = createGeminiAI(
                responses = listOf(
                    """{"id":"initial","status":"completed","steps":[{"type":"function_call","id":"call-1","name":"addInteractionNumbers","arguments":{"a":2,"b":3}}]}""",
                    """{"id":"final","status":"completed","steps":[{"type":"model_output","content":[{"type":"text","text":"5"}]}]}""",
                ),
                onRequest = { body -> requestBodies.add(body) },
            )
            try {
                val interaction = ai.createInteraction(
                    CreateInteractionRequest(model = "test-model", input = "Add 2 and 3"),
                    ::addInteractionNumbers,
                    maxIterations = 1,
                )

                assertEquals("final", interaction.id)
                assertEquals(2, requestBodies.size)
                assertTrue(requestBodies.first().contains("\"name\":\"addInteractionNumbers\""))
                assertTrue(requestBodies[1].contains("\"previous_interaction_id\":\"initial\""))
                assertTrue(requestBodies[1].contains("\"type\":\"function_result\""))
                assertTrue(requestBodies[1].contains("\"call_id\":\"call-1\""))
                assertTrue(requestBodies[1].contains("\"result\":{\"result\":5}"))
            } finally {
                client.close()
            }
        }

    @Test
    fun usesCallIdWhenFunctionCallIdIsMissing() =
        runTest {
            val requestBodies = mutableListOf<String>()
            val (ai, client) = createGeminiAI(
                responses = listOf(
                    """{"id":"initial","status":"completed","steps":[{"type":"function_call","call_id":"call-1","name":"addInteractionNumbers","arguments":{"a":2,"b":3}}]}""",
                    """{"id":"final","status":"completed","steps":[{"type":"model_output","content":[{"type":"text","text":"5"}]}]}""",
                ),
                onRequest = { body -> requestBodies.add(body) },
            )
            try {
                val interaction = ai.createInteraction(
                    CreateInteractionRequest(model = "test-model", input = "Add 2 and 3"),
                    ::addInteractionNumbers,
                    maxIterations = 1,
                )

                assertEquals("final", interaction.id)
                assertTrue(requestBodies[1].contains("\"call_id\":\"call-1\""))
                assertTrue(requestBodies[1].contains("\"result\":{\"result\":5}"))
            } finally {
                client.close()
            }
        }

    @Test
    fun returnsWhenInitialInteractionHasNoFunctionCalls() =
        runTest {
            val (ai, client) = createGeminiAI(
                responses = listOf(
                    """{"id":"final","status":"completed","output_text":"No tool needed.","steps":[{"type":"model_output","content":[{"type":"text","text":"No tool needed."}]}]}""",
                ),
            )
            try {
                val interaction = ai.createInteraction(
                    CreateInteractionRequest(input = "Say hello"),
                    ::addInteractionNumbers,
                )
                assertEquals("final", interaction.id)
                assertEquals("No tool needed.", interaction.outputText)
            } finally {
                client.close()
            }
        }

    @Test
    fun reportsWhenModelKeepsRequestingFunctionsAfterIterationLimit() =
        runTest {
            val repeatingCall =
                """{"id":"continued","status":"completed","steps":[{"type":"function_call","id":"call-2","name":"addInteractionNumbers","arguments":{"a":1,"b":1}}]}"""
            val (ai, client) = createGeminiAI(
                responses = listOf(repeatingCall, repeatingCall),
            )
            try {
                val error = try {
                    ai.createInteraction(
                        CreateInteractionRequest(input = "Keep adding"),
                        ::addInteractionNumbers,
                        maxIterations = 1,
                    )
                    error("Expected the function-call limit to be exceeded.")
                } catch (exception: IllegalStateException) {
                    exception
                }
                assertTrue(error.message.orEmpty().contains("maxIterations=1"))
            } finally {
                client.close()
            }
        }

    @Test
    fun rejectsFunctionCallsWithMissingOrUnregisteredData() =
        runTest {
            val malformedCalls = listOf(
                """{"type":"function_call","id":"call-1","arguments":{"a":1,"b":2}}""" to "did not include a name",
                """{"type":"function_call","name":"addInteractionNumbers","arguments":{"a":1,"b":2}}""" to "did not include an id",
                """{"type":"function_call","id":"call-1","name":"addInteractionNumbers"}""" to "did not include object arguments",
                """{"type":"function_call","id":"call-1","name":"unknownFunction","arguments":{}}""" to "No function handler",
            )
            for ((call, expectedError) in malformedCalls) {
                val (ai, client) = createGeminiAI(
                    responses = listOf("""{"id":"initial","status":"completed","steps":[$call]}"""),
                )
                try {
                    val exception = try {
                        ai.createInteraction(
                            CreateInteractionRequest(input = "Call a function"),
                            ::addInteractionNumbers,
                        )
                        error("Expected malformed function call to fail.")
                    } catch (failure: IllegalStateException) {
                        failure
                    }
                    assertTrue(exception.message.orEmpty().contains(expectedError))
                } finally {
                    client.close()
                }
            }
        }

    @Test
    fun validatesFunctionReferencesAndIterationLimit() =
        runTest {
            val (ai, client) = createGeminiAI(responses = emptyList())
            try {
                assertFailsWith<IllegalArgumentException> {
                    ai.createInteraction(CreateInteractionRequest(input = "Hello"), maxIterations = 2)
                }
                assertFailsWith<IllegalArgumentException> {
                    ai.createInteraction(
                        CreateInteractionRequest(input = "Hello"),
                        ::addInteractionNumbers,
                        maxIterations = 0,
                    )
                }
            } finally {
                client.close()
            }
        }

    private fun createGeminiAI(
        responses: List<String>,
        onRequest: (String) -> Unit = {},
    ): Pair<GeminiAI, HttpClient> {
        var responseIndex = 0
        val client = HttpClient(MockEngine) {
            engine {
                addHandler { request ->
                    onRequest((request.body as TextContent).text)
                    respond(
                        content = responses[responseIndex++],
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )
                }
            }
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        return GeminiAI(client = client, apiKey = "test-key") to client
    }
}
