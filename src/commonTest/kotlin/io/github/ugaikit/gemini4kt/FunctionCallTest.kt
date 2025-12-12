package io.github.ugaikit.gemini4kt

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals

class FunctionCallTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `serialization and deserialization of FunctionCall with string argument`() {
        val original =
            FunctionCall(
                name = "find_weather",
                args = mapOf("location" to JsonPrimitive("Boston, MA")),
            )
        val encoded = json.encodeToString(original)
        val decoded = json.decodeFromString<FunctionCall>(encoded)
        assertEquals(original, decoded)
    }

    @Test
    fun `serialization and deserialization of FunctionCall with number argument`() {
        val original =
            FunctionCall(
                name = "get_population",
                args = mapOf("zip_code" to JsonPrimitive(90210)),
            )
        val encoded = json.encodeToString(original)
        val decoded = json.decodeFromString<FunctionCall>(encoded)
        assertEquals(original, decoded)
    }

    @Test
    fun `serialization and deserialization of FunctionCall with boolean argument`() {
        val original =
            FunctionCall(
                name = "is_daylight",
                args = mapOf("check_time" to JsonPrimitive(true)),
            )
        val encoded = json.encodeToString(original)
        val decoded = json.decodeFromString<FunctionCall>(encoded)
        assertEquals(original, decoded)
    }

    @Test
    fun `serialization and deserialization of FunctionCall with mixed arguments`() {
        val original =
            FunctionCall(
                name = "get_user_profile",
                args =
                    mapOf(
                        "user_id" to JsonPrimitive(12345),
                        "include_history" to JsonPrimitive(true),
                        "user_name" to JsonPrimitive("John Doe"),
                    ),
            )
        val encoded = json.encodeToString(original)
        val decoded = json.decodeFromString<FunctionCall>(encoded)
        assertEquals(original, decoded)
    }
}
