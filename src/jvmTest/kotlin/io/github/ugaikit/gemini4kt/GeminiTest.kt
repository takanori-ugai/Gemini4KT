package io.github.ugaikit.gemini4kt

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.io.files.Path
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertFailsWith

@GeminiFunction(description = "Adds two integers")
private fun addDirect(
    @GeminiParameter(description = "first number") a: Int,
    @GeminiParameter(description = "second number") b: Int,
): Int = a + b

@GeminiFunction(description = "Uses multiple scalar types")
private fun typed_scalars(
    @GeminiParameter(description = "long value") l: Long,
    @GeminiParameter(description = "double value") d: Double,
    @GeminiParameter(description = "float value") f: Float,
    @GeminiParameter(description = "boolean value") b: Boolean,
): String = "$l|$d|$f|$b"

@GeminiFunction(description = "Supports nullable input")
private fun nullable_text(
    @GeminiParameter(description = "nullable text") text: String?,
): String = text ?: "null"

@GeminiFunction(description = "Supports optional argument")
private fun optional_text(
    @GeminiParameter(description = "required text") text: String,
    @GeminiParameter(description = "suffix text") suffix: String = "!",
): String = "$text$suffix"

@GeminiFunction(description = "Returns FunctionResponse directly")
private fun direct_response(
    @GeminiParameter(description = "value") value: Int,
): FunctionResponse =
    FunctionResponse(
        name = "direct_response",
        response = buildJsonObject { put("value", value) },
    )

@GeminiFunction(description = "Returns nested map result")
private fun map_result(
    @GeminiParameter(description = "value") value: Int,
): Map<String, Any> =
    mapOf(
        "value" to value,
        "meta" to mapOf("ok" to true),
    )

@GeminiFunction(description = "Consumes list and map parameters")
private fun list_and_map(
    @GeminiParameter(description = "numbers") numbers: List<Int>,
    @GeminiParameter(description = "labels") labels: Map<String, String>,
): String = "${numbers.sum()}-${labels["mode"] ?: "unknown"}"

@GeminiFunction(description = "Unsupported parameter type for declaration")
private fun unsupported_key_map(
    @GeminiParameter(description = "map") value: Map<Int, String>,
): String = value.size.toString()

private data class UnsupportedReturnType(
    val value: Int,
)

@GeminiFunction(description = "Unsupported return type for automatic binding")
private fun unsupported_return(
    @GeminiParameter(description = "value") value: Int,
): UnsupportedReturnType = UnsupportedReturnType(value)

/**
 * Represents the gemini test.
 */
class GeminiTest {
    /**
     * Holds the gemini.
     */
    private lateinit var gemini: Gemini

    /**
     * Holds the file upload provider.
     */
    private lateinit var fileUploadProvider: FileUploadProvider

    /**
     * Holds the base url.
     */
    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta"

    /**
     * Holds the api key.
     */
    private val apiKey = "test-api-key"

    /**
     * Handles setup.
     */
    @BeforeEach
    fun setup() {
        fileUploadProvider = mockk()
    }

    /**
     * Handles create gemini.
     *
     * @param handler The handler.
     */
    private fun createGemini(handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData): Gemini {
        val client =
            HttpClient(MockEngine) {
                engine {
                    addHandler(handler)
                }
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
            }
        return Gemini(
            apiKey = apiKey,
            client = client,
            fileUploadProvider = fileUploadProvider,
        )
    }

    /**
     * Handles stream generate content.
     */
    @Test
    fun `streamGenerateContent yields responses on success`() =
        runTest {
            val responseJson1 = """{"candidates": [{"content": {"parts": [{"text": "Hello"}]}}]}"""
            val responseJson2 = """{"candidates": [{"content": {"parts": [{"text": " World"}]}}]}"""
            val sseStream =
                """
                data: $responseJson1

                data: $responseJson2
                """.trimIndent()

            gemini =
                createGemini { request ->
                    assertEquals(HttpMethod.Post, request.method)
                    assertEquals(
                        "$baseUrl/models/gemini-flash-lite-latest:streamGenerateContent?alt=sse",
                        request.url.toString(),
                    )
                    respond(
                        content = sseStream,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "text/event-stream"),
                    )
                }

            val request = GenerateContentRequest(contents = emptyArray())
            val flow = gemini.streamGenerateContent(request)
            val results = flow.toList()

            assertEquals(2, results.size)
            assertNotNull(results[0].candidates)
            assertNotNull(results[1].candidates)
        }

    /**
     * Handles get content.
     */
    @Test
    fun `getContent with inputJson returns content on success`() =
        runTest {
            val response = """{"key":"value"}"""
            gemini =
                createGemini { request ->
                    assertEquals(HttpMethod.Post, request.method)
                    respond(
                        content = response,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }

            val result = gemini.getContent("http://localhost", "{}")

            assertEquals(response, result)
        }

    /**
     * Handles get content.
     */
    @Test
    fun `getContent without inputJson returns content on success`() =
        runTest {
            val response = """{"key":"value"}"""
            gemini =
                createGemini { request ->
                    assertEquals(HttpMethod.Get, request.method)
                    respond(
                        content = response,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }

            val result = gemini.getContent("http://localhost")

            assertEquals(response, result)
        }

    /**
     * Handles get content.
     */
    @Test
    fun `getContent returns empty json on error`() =
        runTest {
            gemini =
                createGemini {
                    respond(
                        content = """{"error": {"code": 400, "message": "Error", "status": "INVALID_ARGUMENT"}}""",
                        status = HttpStatusCode.BadRequest,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }

            org.junit.jupiter.api.assertThrows<GeminiException> {
                gemini.getContent("http://localhost")
            }
        }

    /**
     * Handles delete content.
     */
    @Test
    fun `deleteContent succeeds with 200 response`() =
        runTest {
            gemini =
                createGemini { request ->
                    assertEquals(HttpMethod.Delete, request.method)
                    respond(content = "", status = HttpStatusCode.OK)
                }

            gemini.deleteContent("http://localhost")
        }

    /**
     * Handles delete content.
     */
    @Test
    fun `deleteContent handles error response`() =
        runTest {
            gemini =
                createGemini {
                    respond(
                        content = "Error",
                        status = HttpStatusCode.BadRequest,
                    )
                }

            org.junit.jupiter.api.assertThrows<GeminiException> {
                gemini.deleteContent("http://localhost")
            }
        }

    /**
     * Handles generate content.
     */
    @Test
    fun `generateContent calls getContent with correct parameters`() =
        runTest {
            val responseJson = """{"candidates": []}"""
            gemini =
                createGemini { request ->
                    assertEquals("$baseUrl/models/gemini-flash-lite-latest:generateContent", request.url.toString())
                    respond(responseJson, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
                }
            val request = GenerateContentRequest(contents = emptyArray())

            val response = gemini.generateContent(request)

            assertNotNull(response)
        }

    /**
     * Handles create cached content.
     */
    @Test
    fun `createCachedContent calls getContent with correct parameters`() =
        runTest {
            val responseJson = """{"name": "cachedContent-123"}"""
            gemini =
                createGemini { request ->
                    assertEquals("$baseUrl/cachedContents", request.url.toString())
                    respond(responseJson, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
                }
            val request = CachedContent(contents = emptyList())

            val response = gemini.createCachedContent(request)

            assertEquals("cachedContent-123", response.name)
        }

    /**
     * Handles list cached content.
     */
    @Test
    fun `listCachedContent calls getContent with correct parameters`() =
        runTest {
            val responseJson = """{"cachedContents": []}"""
            gemini =
                createGemini { request ->
                    assertEquals("$baseUrl/cachedContents?pageSize=1000", request.url.toString())
                    respond(responseJson, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
                }

            val response = gemini.listCachedContent()

            assertNotNull(response)
        }

    /**
     * Handles get cached content.
     */
    @Test
    fun `getCachedContent calls getContent with correct parameters`() =
        runTest {
            val name = "cachedContent-123"
            val responseJson = """{"name": "cachedContent-123"}"""
            gemini =
                createGemini { request ->
                    assertEquals("$baseUrl/cachedContents/$name", request.url.toString())
                    respond(responseJson, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
                }

            val response = gemini.getCachedContent(name)

            assertEquals(name, response.name)
        }

    /**
     * Handles delete cached content.
     */
    @Test
    fun `deleteCachedContent calls deleteContent with correct parameters`() =
        runTest {
            val name = "cachedContent-123"
            gemini =
                createGemini { request ->
                    assertEquals("$baseUrl/cachedContents/$name", request.url.toString())
                    assertEquals(HttpMethod.Delete, request.method)
                    respond(content = "", status = HttpStatusCode.OK)
                }

            gemini.deleteCachedContent(name)
        }

    /**
     * Handles count tokens.
     */
    @Test
    fun `countTokens calls getContent with correct parameters`() =
        runTest {
            val responseJson = """{"totalTokens": 10}"""
            gemini =
                createGemini { request ->
                    assertEquals("$baseUrl/models/gemini-2.0-flash-lite:countTokens", request.url.toString())
                    respond(responseJson, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
                }
            val request = CountTokensRequest(contents = emptyList())

            val response = gemini.countTokens(request)

            assertEquals(10, response.totalTokens)
        }

    /**
     * Handles batch embed contents.
     */
    @Test
    fun `batchEmbedContents calls getContent with correct parameters`() =
        runTest {
            val responseJson = """{"embeddings": []}"""
            gemini =
                createGemini { request ->
                    assertEquals("$baseUrl/models/embedding-001:batchEmbedContents", request.url.toString())
                    respond(responseJson, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
                }
            val request = BatchEmbedRequest(requests = emptyList())

            val response = gemini.batchEmbedContents(request)

            assertNotNull(response)
        }

    /**
     * Handles embed content.
     */
    @Test
    fun `embedContent calls getContent with correct parameters`() =
        runTest {
            val responseJson = """{"embedding": {"values": [1.0, 2.0, 3.0]}}"""
            gemini =
                createGemini { request ->
                    assertEquals("$baseUrl/models/embedding-001:embedContent", request.url.toString())
                    respond(responseJson, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
                }
            val request = EmbedContentRequest(content = Content(parts = emptyArray()), model = "models/embedding-001")

            val response = gemini.embedContent(request)

            assertNotNull(response)
        }

    /**
     * Handles get models.
     */
    @Test
    fun `getModels calls getContent with correct parameters`() =
        runTest {
            val responseJson = """{"models": []}"""
            gemini =
                createGemini { request ->
                    assertEquals("$baseUrl/models", request.url.toString())
                    respond(responseJson, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
                }

            val response = gemini.getModels()

            assertNotNull(response)
        }

    /**
     * Handles upload file.
     */
    @Test
    fun `uploadFile calls fileUploadProvider with correct parameters`() =
        runTest {
            gemini = createGemini { respond(content = "", status = HttpStatusCode.OK) }

            val file = File("test.txt")
            val path = Path(file.path)
            val mimeType = "text/plain"
            val displayName = "Test File"
            val expectedFile = mockk<GeminiFile>()

            coEvery { fileUploadProvider.upload(path, mimeType, displayName) } returns expectedFile

            // Call with Path because uploadFile extension taking File calls this one,
            // but here we are testing Gemini class directly which uses Path.
            // If we want to test extension, we need to import it.
            // But let's test the member function.
            val result = gemini.uploadFile(path, mimeType, displayName)

            assertEquals(expectedFile, result)
            coVerify { fileUploadProvider.upload(path, mimeType, displayName) }
        }

    @Test
    fun `generateContentWithAutomaticFunctionCalls executes function and returns final response`() =
        runTest {
            var callCount = 0
            gemini =
                createGemini { request ->
                    callCount += 1

                    if (callCount == 2) {
                        val body = (request.body as TextContent).text
                        assertTrue(body.contains("\"functionResponse\""))
                        assertTrue(body.contains("\"result\":579"))
                    }

                    val responseJson =
                        if (callCount == 1) {
                            """
                            {
                              "candidates": [
                                {
                                  "content": {
                                    "parts": [
                                      {
                                        "functionCall": {
                                          "name": "add",
                                          "args": { "a": 123, "b": 456 }
                                        }
                                      }
                                    ]
                                  }
                                }
                              ]
                            }
                            """.trimIndent()
                        } else {
                            """
                            {
                              "candidates": [
                                {
                                  "content": {
                                    "parts": [
                                      {
                                        "text": "The sum is 579."
                                      }
                                    ]
                                  }
                                }
                              ]
                            }
                            """.trimIndent()
                        }

                    respond(
                        content = responseJson,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }

            val request =
                GenerateContentRequest(
                    contents = arrayOf(Content(role = "user", parts = arrayOf(Part(text = "What is 123 plus 456?")))),
                    tools = emptyArray(),
                )

            val handlers =
                mapOf<String, suspend (FunctionCall) -> FunctionResponse>(
                    "add" to { functionCall ->
                        val a = functionCall.args["a"]?.jsonPrimitive?.int ?: 0
                        val b = functionCall.args["b"]?.jsonPrimitive?.int ?: 0
                        FunctionResponse(
                            name = "add",
                            response = buildJsonObject { put("result", a + b) },
                        )
                    },
                )

            val response = gemini.generateContent(request, handlers)

            assertEquals(2, callCount)
            assertEquals(
                "The sum is 579.",
                response.candidates
                    .first()
                    .content.parts
                    ?.firstOrNull()
                    ?.text,
            )
        }

    @Test
    fun `generateContentWithAutomaticFunctionCalls throws when function handler is missing`() =
        runTest {
            gemini =
                createGemini {
                    respond(
                        content =
                            """
                            {
                              "candidates": [
                                {
                                  "content": {
                                    "parts": [
                                      {
                                        "functionCall": {
                                          "name": "unknown_function",
                                          "args": {}
                                        }
                                      }
                                    ]
                                  }
                                }
                              ]
                            }
                            """.trimIndent(),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }

            val request =
                GenerateContentRequest(
                    contents = arrayOf(Content(role = "user", parts = arrayOf(Part(text = "Do something")))),
                )

            val exception =
                try {
                    gemini.generateContent(request, emptyMap())
                    null
                } catch (e: IllegalArgumentException) {
                    e
                }

            assertNotNull(exception)
            assertEquals(
                "No function handler registered for 'unknown_function'.",
                exception?.message,
            )
        }

    @Test
    fun `generateContentWithAutomaticFunctionCalls supports direct kotlin function binding`() =
        runTest {
            var callCount = 0
            gemini =
                createGemini { request ->
                    callCount += 1
                    val body = (request.body as TextContent).text

                    if (callCount == 1) {
                        assertTrue(body.contains("\"functionDeclarations\""))
                        assertTrue(body.contains("\"name\":\"addDirect\""))
                    }
                    if (callCount == 2) {
                        assertTrue(body.contains("\"functionResponse\""))
                        assertTrue(body.contains("\"result\":579"))
                    }

                    val responseJson =
                        if (callCount == 1) {
                            """
                            {
                              "candidates": [
                                {
                                  "content": {
                                    "parts": [
                                      {
                                        "functionCall": {
                                          "name": "addDirect",
                                          "args": { "a": 123, "b": 456 }
                                        }
                                      }
                                    ]
                                  }
                                }
                              ]
                            }
                            """.trimIndent()
                        } else {
                            """
                            {
                              "candidates": [
                                {
                                  "content": {
                                    "parts": [
                                      {
                                        "text": "The sum is 579."
                                      }
                                    ]
                                  }
                                }
                              ]
                            }
                            """.trimIndent()
                        }

                    respond(
                        content = responseJson,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }

            val request =
                GenerateContentRequest(
                    contents = arrayOf(Content(role = "user", parts = arrayOf(Part(text = "What is 123 plus 456?")))),
                )

            val response =
                gemini.generateContent(
                    request,
                    ::addDirect,
                )

            assertEquals(2, callCount)
            assertEquals(
                "The sum is 579.",
                response.candidates
                    .first()
                    .content.parts
                    ?.firstOrNull()
                    ?.text,
            )
        }

    @Test
    fun `generateContent with handlers returns immediately when candidates are empty`() =
        runTest {
            gemini =
                createGemini {
                    respond(
                        content = """{"candidates": []}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }

            val response =
                gemini.generateContent(
                    request = GenerateContentRequest(contents = arrayOf(Content(parts = arrayOf(Part(text = "hi"))))),
                    functionHandlers = emptyMap(),
                )

            assertTrue(response.candidates.isEmpty())
        }

    @Test
    fun `generateContent with handlers throws when maxIterations exceeded`() =
        runTest {
            gemini =
                createGemini {
                    respond(
                        content =
                            """
                            {
                              "candidates": [
                                {
                                  "content": {
                                    "parts": [
                                      {
                                        "functionCall": {
                                          "name": "loop",
                                          "args": {}
                                        }
                                      }
                                    ]
                                  }
                                }
                              ]
                            }
                            """.trimIndent(),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }

            val exception =
                try {
                    gemini.generateContent(
                        request =
                            GenerateContentRequest(
                                contents = arrayOf(Content(parts = arrayOf(Part(text = "loop")))),
                            ),
                        functionHandlers =
                            mapOf(
                                "loop" to {
                                    FunctionResponse(name = "loop", response = buildJsonObject { put("ok", true) })
                                },
                            ),
                        maxIterations = 1,
                    )
                    null
                } catch (e: IllegalStateException) {
                    e
                }

            assertNotNull(exception)
            assertEquals("Automatic function calling exceeded maxIterations=1.", exception!!.message)
        }

    @Test
    fun `generateContent with handlers validates positive maxIterations`() =
        runTest {
            gemini = createGemini { respond("""{"candidates": []}""", HttpStatusCode.OK) }
            val exception =
                try {
                    gemini.generateContent(
                        request =
                            GenerateContentRequest(
                                contents = arrayOf(Content(parts = arrayOf(Part(text = "x")))),
                            ),
                        functionHandlers = emptyMap(),
                        maxIterations = 0,
                    )
                    null
                } catch (e: IllegalArgumentException) {
                    e
                }
            assertNotNull(exception)
            assertEquals("maxIterations must be greater than 0.", exception!!.message)
        }

    @Test
    fun `generateContent with direct binding handles typed scalar args`() =
        runTest {
            var callCount = 0
            gemini =
                createGemini {
                    callCount++
                    val responseJson =
                        if (callCount == 1) {
                            """
                            {
                              "candidates": [
                                {
                                  "content": {
                                    "parts": [
                                      {
                                        "functionCall": {
                                          "name": "typed_scalars",
                                          "args": {
                                            "l": 1234567890123,
                                            "d": 1.25,
                                            "f": 2.5,
                                            "b": true
                                          }
                                        }
                                      }
                                    ]
                                  }
                                }
                              ]
                            }
                            """.trimIndent()
                        } else {
                            """{"candidates":[{"content":{"parts":[{"text":"done"}]}}]}"""
                        }
                    respond(responseJson, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
                }

            val response =
                gemini.generateContent(
                    GenerateContentRequest(contents = arrayOf(Content(parts = arrayOf(Part(text = "go"))))),
                    ::typed_scalars,
                )

            assertEquals(
                "done",
                response.candidates
                    .first()
                    .content.parts
                    ?.firstOrNull()
                    ?.text,
            )
        }

    @Test
    fun `generateContent with direct binding supports nullable and optional args`() =
        runTest {
            var callCount = 0
            gemini =
                createGemini {
                    callCount++
                    val responseJson =
                        when (callCount) {
                            1 ->
                                """
                                {
                                  "candidates": [
                                    {
                                      "content": {
                                        "parts": [
                                          {
                                            "functionCall": {
                                              "name": "nullable_text",
                                              "args": {
                                                "text": null
                                              }
                                            }
                                          }
                                        ]
                                      }
                                    }
                                  ]
                                }
                                """.trimIndent()
                            2 ->
                                """
                                {
                                  "candidates": [
                                    {
                                      "content": {
                                        "parts": [
                                          {
                                            "functionCall": {
                                              "name": "optional_text",
                                              "args": {
                                                "text": "Hi"
                                              }
                                            }
                                          }
                                        ]
                                      }
                                    }
                                  ]
                                }
                                """.trimIndent()
                            else -> """{"candidates":[{"content":{"parts":[{"text":"ok"}]}}]}"""
                        }
                    respond(responseJson, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
                }

            val response =
                gemini.generateContent(
                    GenerateContentRequest(contents = arrayOf(Content(parts = arrayOf(Part(text = "go"))))),
                    ::nullable_text,
                    ::optional_text,
                    maxIterations = 3,
                )

            assertEquals(
                "ok",
                response.candidates
                    .first()
                    .content.parts
                    ?.firstOrNull()
                    ?.text,
            )
        }

    @Test
    fun `generateContent with direct binding supports list and map args`() =
        runTest {
            var callCount = 0
            var secondRequestBody = ""
            gemini =
                createGemini { request ->
                    callCount++
                    if (callCount == 2) {
                        secondRequestBody = (request.body as TextContent).text
                    }
                    val responseJson =
                        if (callCount == 1) {
                            """
                            {
                              "candidates": [
                                {
                                  "content": {
                                    "parts": [
                                      {
                                        "functionCall": {
                                          "name": "list_and_map",
                                          "args": {
                                            "numbers": [1, 2, 3],
                                            "labels": {
                                              "mode": "party"
                                            }
                                          }
                                        }
                                      }
                                    ]
                                  }
                                }
                              ]
                            }
                            """.trimIndent()
                        } else {
                            """{"candidates":[{"content":{"parts":[{"text":"ok"}]}}]}"""
                        }
                    respond(responseJson, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
                }

            val response =
                gemini.generateContent(
                    GenerateContentRequest(contents = arrayOf(Content(parts = arrayOf(Part(text = "go"))))),
                    ::list_and_map,
                )

            assertEquals(
                "ok",
                response.candidates
                    .first()
                    .content.parts
                    ?.firstOrNull()
                    ?.text,
            )
            assertTrue(secondRequestBody.contains("\"result\":\"6-party\""))
        }

    @Test
    fun `generateContent with direct binding supports FunctionResponse and map return types`() =
        runTest {
            var callCount = 0
            var secondRequestBody = ""
            gemini =
                createGemini { request ->
                    callCount++
                    if (callCount == 2) {
                        secondRequestBody = (request.body as TextContent).text
                    }
                    val responseJson =
                        if (callCount == 1) {
                            """
                            {
                              "candidates":[{"content":{"parts":[
                                {"functionCall":{"name":"map_result","args":{"value":7}}},
                                {"functionCall":{"name":"direct_response","args":{"value":7}}}
                              ]}}]
                            }
                            """.trimIndent()
                        } else {
                            """{"candidates":[{"content":{"parts":[{"text":"ok"}]}}]}"""
                        }
                    respond(responseJson, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
                }

            val response =
                gemini.generateContent(
                    GenerateContentRequest(contents = arrayOf(Content(parts = arrayOf(Part(text = "go"))))),
                    ::map_result,
                    ::direct_response,
                )

            assertEquals(
                "ok",
                response.candidates
                    .first()
                    .content.parts
                    ?.firstOrNull()
                    ?.text,
            )
            assertTrue(secondRequestBody.contains("\"name\":\"map_result\""))
            assertTrue(secondRequestBody.contains("\"name\":\"direct_response\""))
            assertTrue(secondRequestBody.contains("\"meta\":{\"ok\":true}"))
            assertTrue(secondRequestBody.contains("\"value\":7"))
        }

    @Test
    fun `generateContent with direct binding rejects unsupported parameter types`() =
        runTest {
            gemini =
                createGemini {
                    respond("""{"candidates": []}""", HttpStatusCode.OK)
                }

            val exception =
                assertFailsWith<IllegalArgumentException> {
                    gemini.generateContent(
                        GenerateContentRequest(contents = arrayOf(Content(parts = arrayOf(Part(text = "x"))))),
                        ::unsupported_key_map,
                    )
                }

            assertTrue(exception.message?.contains("Map parameter keys must be String for automatic binding") == true)
        }

    @Test
    fun `generateContent with direct binding rejects unsupported return types`() =
        runTest {
            gemini =
                createGemini {
                    respond(
                        content =
                            """
                            {
                              "candidates": [
                                {
                                  "content": {
                                    "parts": [
                                      {
                                        "functionCall": {
                                          "name": "unsupported_return",
                                          "args": {
                                            "value": 7
                                          }
                                        }
                                      }
                                    ]
                                  }
                                }
                              ]
                            }
                            """.trimIndent(),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }

            val exception =
                try {
                    gemini.generateContent(
                        GenerateContentRequest(contents = arrayOf(Content(parts = arrayOf(Part(text = "x"))))),
                        ::unsupported_return,
                    )
                    null
                } catch (e: IllegalArgumentException) {
                    e
                }

            assertNotNull(exception)
            assertTrue(exception!!.message?.contains("Unsupported return type for automatic binding") == true)
        }
}
