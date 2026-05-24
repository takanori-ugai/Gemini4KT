package io.github.ugaikit.gemini4kt

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlin.reflect.full.memberFunctions
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@GeminiFunction(description = "Echoes text")
private fun echoText(
    @GeminiParameter(description = "text") text: String,
): String = text

@GeminiFunction(description = "Summarizes payload")
private fun summarizePayload(
    @GeminiParameter(description = "payload") payload: Map<String, *>,
): String {
    val nested = payload["nested"] as Map<*, *>
    val list = payload["list"] as List<*>
    return "${payload["name"]}|${payload["count"]}|${payload["enabled"]}|${nested["k"]}|${list.size}|${payload["nothing"] == null}"
}

@GeminiFunction(description = "Returns an int array")
private fun arrayResult(
    @GeminiParameter(description = "base") base: Int,
): Array<Int> = arrayOf(base, base + 1)

private class MemberFunctionFixture {
    @GeminiFunction(description = "Member function")
    fun memberEcho(
        @GeminiParameter(description = "value") value: String,
    ): String = value
}

class AutomaticFunctionCallingJvmTest {
    @Test
    fun buildFunctionToolsRejectsEmptyInput() {
        val exception = assertFailsWith<IllegalArgumentException> { buildFunctionTools() }
        assertEquals("At least one function is required.", exception.message)
    }

    @Test
    fun handlerRejectsFunctionNameMismatch() =
        runTest {
            val binding = buildAutomaticFunctionBinding(arrayOf(::echoText))
            val handler = binding.handlers.getValue("echoText")
            val exception =
                assertFailsWith<IllegalArgumentException> {
                    handler(
                        FunctionCall(
                            name = "wrongName",
                            args = mapOf("text" to JsonPrimitive("hi")),
                        ),
                    )
                }
            assertTrue(exception.message?.contains("Function name mismatch") == true)
        }

    @Test
    fun handlerRejectsUnboundMemberFunctions() =
        runTest {
            val function =
                MemberFunctionFixture::class
                    .memberFunctions
                    .first { it.name == "memberEcho" }
            val binding = buildAutomaticFunctionBinding(arrayOf(function))
            val handler = binding.handlers.getValue("memberEcho")
            val exception =
                assertFailsWith<IllegalArgumentException> {
                    handler(
                        FunctionCall(
                            name = "memberEcho",
                            args = mapOf("value" to JsonPrimitive("x")),
                        ),
                    )
                }
            assertTrue(exception.message?.contains("Only top-level or bound functions are supported") == true)
        }

    @Test
    fun handlerRejectsNullForNonNullableParameters() =
        runTest {
            val binding = buildAutomaticFunctionBinding(arrayOf(::echoText))
            val handler = binding.handlers.getValue("echoText")
            val exception =
                assertFailsWith<IllegalArgumentException> {
                    handler(
                        FunctionCall(
                            name = "echoText",
                            args = mapOf("text" to JsonNull),
                        ),
                    )
                }
            assertTrue(exception.message?.contains("Non-null parameter received null") == true)
        }

    @Test
    fun handlerConvertsMapStarArgumentsAndReturnsArrayResult() =
        runTest {
            val binding = buildAutomaticFunctionBinding(arrayOf(::summarizePayload, ::arrayResult))

            val summarizeHandler = binding.handlers.getValue("summarizePayload")
            val summarizeResponse =
                summarizeHandler(
                    FunctionCall(
                        name = "summarizePayload",
                        args =
                            mapOf(
                                "payload" to
                                    buildJsonObject {
                                        put("name", "party")
                                        put("count", 3)
                                        put("enabled", true)
                                        put("nested", buildJsonObject { put("k", "v") })
                                        put(
                                            "list",
                                            buildJsonArray {
                                                add(JsonPrimitive(1))
                                                add(JsonPrimitive("two"))
                                            },
                                        )
                                        put("nothing", JsonNull)
                                    },
                            ),
                    ),
                )
            assertEquals("party|3|true|v|2|true", summarizeResponse.response["result"]?.jsonPrimitive?.content)

            val arrayHandler = binding.handlers.getValue("arrayResult")
            val arrayResponse =
                arrayHandler(
                    FunctionCall(
                        name = "arrayResult",
                        args = mapOf("base" to JsonPrimitive(5)),
                    ),
                )
            val resultArray = arrayResponse.response["result"]?.jsonArray
            assertEquals(2, resultArray?.size)
            assertEquals(5, resultArray?.get(0)?.jsonPrimitive?.int)
            assertEquals(6, resultArray?.get(1)?.jsonPrimitive?.int)
        }
}
