package io.github.ugaikit.gemini4kt

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
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
    return "${payload["name"]}|${payload["count"]}|${payload["enabled"]}|${nested["k"]}|" +
        "${list.size}|${payload["nothing"] == null}"
}

@GeminiFunction(description = "Summarizes labels")
private fun summarizeLabels(
    @GeminiParameter(description = "labels") labels: Map<String, String>,
): String = "${labels["name"]}|${labels["status"]}"

@GeminiFunction(description = "Returns an int array")
private fun arrayResult(
    @GeminiParameter(description = "base") base: Int,
): Array<Int> = arrayOf(base, base + 1)

@GeminiFunction(description = "Returns Unit")
private fun unitResult(
    @GeminiParameter(description = "message") message: String,
) {
    message.length
}

@Serializable
private enum class Priority {
    LOW,
    HIGH,
}

@Serializable
private data class Address(
    val city: String,
    val zipCode: Int,
)

@Serializable
private data class Profile(
    val name: String,
    val priority: Priority,
    val address: Address,
)

@GeminiFunction(description = "Summarizes profile")
private fun summarizeProfile(
    @GeminiParameter(description = "profile") profile: Profile,
): String = "${profile.name}|${profile.priority}|${profile.address.city}|${profile.address.zipCode}"

@GeminiFunction(description = "Extension function")
private fun String.extensionEcho(
    @GeminiParameter(description = "suffix") suffix: String,
): String = this + suffix

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
    fun handlerRejectsExtensionFunctionInvocation() =
        runTest {
            val exception =
                assertFailsWith<IllegalArgumentException> {
                    buildAutomaticFunctionBinding(arrayOf(String::extensionEcho))
                }
            assertTrue(
                exception.message?.contains("Extension functions are not supported for automatic binding") == true,
            )
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
    fun handlerRejectsStarProjectedMapParameters() =
        runTest {
            val exception =
                assertFailsWith<IllegalArgumentException> {
                    buildAutomaticFunctionBinding(arrayOf(::summarizePayload, ::arrayResult))
                }
            assertTrue(exception.message?.contains("Map parameter type must declare a value type") == true)
        }

    @Test
    fun handlerConvertsMapArgumentsAndReturnsArrayResult() =
        runTest {
            val binding = buildAutomaticFunctionBinding(arrayOf(::summarizeLabels, ::arrayResult))

            val summarizeHandler = binding.handlers.getValue("summarizeLabels")
            val summarizeResponse =
                summarizeHandler(
                    FunctionCall(
                        name = "summarizeLabels",
                        args =
                            mapOf(
                                "labels" to
                                    buildJsonObject {
                                        put("name", "party")
                                        put("status", "ready")
                                    },
                            ),
                    ),
                )
            assertEquals("party|ready", summarizeResponse.response["result"]?.jsonPrimitive?.content)

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

    @Test
    fun handlerSerializesUnitReturnAsEmptyObject() =
        runTest {
            val binding = buildAutomaticFunctionBinding(arrayOf(::unitResult))
            val handler = binding.handlers.getValue("unitResult")
            val response =
                handler(
                    FunctionCall(
                        name = "unitResult",
                        args = mapOf("message" to JsonPrimitive("hello")),
                    ),
                )

            assertTrue(response.response["result"]?.toString() == "{}")
        }

    @Test
    fun declarationSupportsNestedSerializableTypesAndEnums() {
        val declaration = buildFunctionDeclaration(::summarizeProfile)
        val profileSchema = requireNotNull(declaration.parameters.properties["profile"])
        val prioritySchema = requireNotNull(profileSchema.properties["priority"])
        val addressSchema = requireNotNull(profileSchema.properties["address"])

        assertEquals("object", profileSchema.type)
        assertTrue(profileSchema.required.containsAll(listOf("name", "priority", "address")))
        assertEquals(listOf("LOW", "HIGH"), prioritySchema.enum)
        assertEquals("object", addressSchema.type)
        assertEquals("string", addressSchema.properties["city"]?.type)
        assertEquals("integer", addressSchema.properties["zipCode"]?.type)
    }

    @Test
    fun handlerConvertsNestedSerializableTypesAndEnums() =
        runTest {
            val binding = buildAutomaticFunctionBinding(arrayOf(::summarizeProfile))
            val handler = binding.handlers.getValue("summarizeProfile")

            val response =
                handler(
                    FunctionCall(
                        name = "summarizeProfile",
                        args =
                            mapOf(
                                "profile" to
                                    buildJsonObject {
                                        put("name", "Ada")
                                        put("priority", "HIGH")
                                        put(
                                            "address",
                                            buildJsonObject {
                                                put("city", "Tokyo")
                                                put("zipCode", 1000)
                                            },
                                        )
                                    },
                            ),
                    ),
                )

            assertEquals("Ada|HIGH|Tokyo|1000", response.response["result"]?.jsonPrimitive?.content)
        }
}
