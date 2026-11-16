package io.github.ugaikit.gemini4kt.decisions

import io.github.ugaikit.gemini4kt.GeminiAI
import io.github.ugaikit.gemini4kt.GeminiException
import io.github.ugaikit.gemini4kt.TestHttpClientTracker
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class DecisionsApiTest {
    private val clients = TestHttpClientTracker()
    private val headers = headersOf(HttpHeaders.ContentType, "application/json")

    @AfterTest
    fun closeClients() {
        clients.closeAll()
    }

    private fun client(handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData): GeminiAI {
        val http = HttpClient(MockEngine) { engine { addHandler(handler) } }
        return GeminiAI(client = clients.track(http), apiKey = "test-key")
    }

    @Test
    fun mixedQuestionsUseTheDecisionsEndpointAndDecodeTypedAnswers() =
        runTest {
            val ai =
                client { request ->
                    assertEquals(HttpMethod.Post, request.method)
                    assertEquals("https://generativelanguage.googleapis.com/v1beta/decisions", request.url.toString())
                    assertEquals("test-key", request.headers["x-goog-api-key"])
                    val body = Json.parseToJsonElement((request.body as TextContent).text).jsonObject
                    assertEquals(JsonPrimitive("gemini-decision-1"), body["model"])
                    assertEquals(JsonPrimitive("The item arrived broken."), body["input"])
                    val questions = body.getValue("questions").jsonObject
                    assertEquals(JsonPrimitive("noul"), questions.getValue("damaged").jsonObject["type"])
                    val choice = questions.getValue("route").jsonObject
                    assertEquals(JsonPrimitive("choice"), choice["type"])
                    assertEquals(JsonPrimitive("Payments"), choice.getValue("criteria").jsonObject["billing"])
                    val score = questions.getValue("severity").jsonObject
                    assertEquals(JsonPrimitive("score"), score["type"])
                    assertEquals(2, score.getValue("criteria").jsonArray.size)
                    respond(
                        """
                        {"answers":{
                          "damaged":{"type":"noul","noul":0.99,"confidence":0.99},
                          "route":{"type":"choice","choice":"billing",
                            "probabilities":{"billing":0.75,"other":0.25},"confidence":0.75},
                          "severity":{"type":"score","score":0.8,
                            "probabilities":{"0":0.2,"1":0.8},"legend":{"0":"Minor","1":"Major"},"confidence":0.8}
                        },"usage":{"total_input_tokens":100,"total_output_tokens":0,"total_tokens":100,
                          "input_tokens_by_modality":[{"modality":"text","tokens":100}]},"future_field":true}
                        """.trimIndent(),
                        headers = headers,
                    )
                }
            val request =
                CreateDecisionRequest(
                    input = "The item arrived broken.",
                    questions =
                        mapOf(
                            "damaged" to DecisionQuestion.Noul("Is the item damaged?"),
                            "route" to
                                DecisionQuestion.Choice(
                                    "Where should it go?",
                                    mapOf("billing" to "Payments", "other" to ""),
                                ),
                            "severity" to DecisionQuestion.Score("How severe is it?", listOf("Minor", "Major")),
                        ),
                )
            val response = ai.createDecision(request)
            assertEquals(0.99, assertIs<DecisionAnswer.Noul>(response.answers["damaged"]).noul)
            val choice = assertIs<DecisionAnswer.Choice>(response.answers["route"])
            assertEquals("billing", choice.choice)
            assertEquals(0.25, choice.probabilities["other"])
            val score = assertIs<DecisionAnswer.Score>(response.answers["severity"])
            assertEquals(0.8, score.score)
            assertEquals("Major", score.legend["1"])
            assertEquals(0, response.usage?.totalOutputTokens)
            assertEquals(100, response.usage?.totalInputTokens)
        }

    @Test
    fun httpErrorsPreserveNumericAndStringApiCodes() =
        runTest {
            val cases =
                listOf(
                    HttpStatusCode.BadRequest to """{"error":{"code":"safety","message":"Blocked"}}""",
                    HttpStatusCode.TooManyRequests to
                        """{"error":{"code":429,"message":"Quota","status":"RESOURCE_EXHAUSTED"}}""",
                    HttpStatusCode.ServiceUnavailable to "Service unavailable",
                )
            for ((status, body) in cases) {
                val ai = client { respond(body, status, headers) }
                val error =
                    assertFailsWith<GeminiException> {
                        ai.createDecision(
                            CreateDecisionRequest("Hello", mapOf("ok" to DecisionQuestion.Noul("Is it a greeting?"))),
                        )
                    }
                assertEquals(status.value, error.error.code)
                if (status == HttpStatusCode.BadRequest) assertEquals("safety", error.error.apiCode)
                assertTrue(error.error.message.isNotBlank())
            }
        }

    @Test
    fun malformedSuccessfulResponsesFailRatherThanReturningEmptyAnswers() =
        runTest {
            val ai = client { respond("""{"answers":{"q":{"type":"unknown","confidence":0.5}}}""", headers = headers) }
            assertFailsWith<SerializationException> {
                ai.createDecision(
                    CreateDecisionRequest("Hello", mapOf("q" to DecisionQuestion.Noul("Is it a greeting?"))),
                )
            }
        }
}
