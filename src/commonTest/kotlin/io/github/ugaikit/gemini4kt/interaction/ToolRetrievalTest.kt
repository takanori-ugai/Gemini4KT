package io.github.ugaikit.gemini4kt.interaction

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class ToolRetrievalTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun toolRetrievalSerializesServerClientAndRemoteMcpOptions() {
        val server = InteractionTool(type = "tool_search")
        val deferredFunction = InteractionTool(type = "function", name = "lookup", deferLoading = true)
        val deferredMcp =
            InteractionTool(
                type = "mcp_server",
                name = "registry",
                url = "https://example.test/mcp",
                deferLoading = true,
            )
        val client = InteractionTool(type = "tool_search", name = "search_registry", execution = "client")
        for (tool in arrayOf(server, deferredFunction, deferredMcp, client)) {
            val decoded = json.decodeFromString<InteractionTool>(json.encodeToString(tool))
            assertEquals(tool, decoded)
            assertEquals(tool.hashCode(), decoded.hashCode())
        }
        assertNotEquals(deferredFunction, deferredFunction.copy(deferLoading = false))
        assertNotEquals(client, client.copy(execution = null))
        assertEquals("""{"type":"tool_search"}""", json.encodeToString(server))
        val deferredFields = json.parseToJsonElement(json.encodeToString(deferredMcp)).jsonObject
        assertEquals(JsonPrimitive(true), deferredFields["defer_loading"])
        val clientFields = json.parseToJsonElement(json.encodeToString(client)).jsonObject
        assertEquals(JsonPrimitive("client"), clientFields["execution"])
        val matches = arrayOf(deferredFunction.copy(deferLoading = null))
        val result = interactionToolSearchResult("call_1", "search_registry", matches)
        val fields = json.parseToJsonElement(json.encodeToString(result)).jsonObject
        assertEquals(JsonPrimitive("function_result"), fields["type"])
        assertEquals(JsonPrimitive("call_1"), fields["call_id"])
        val schema =
            fields
                .getValue("result")
                .jsonArray
                .single()
                .jsonObject
        assertEquals(JsonPrimitive("lookup"), schema["name"])
        assertEquals(null, schema["defer_loading"])
    }

    @Test
    fun statelessToolRetrievalPreservesSignedStepsAndOrder() {
        val history =
            json.parseToJsonElement(
                """
                [
                  {"type":"tool_search_call","id":"search","arguments":{"function_names":["lookup"]},"signature":"call-sig"},
                  {"type":"tool_search_result","call_id":"search","result":[{"type":"function","name":"lookup"}],
                   "signature":"result-sig"}
                ]
                """.trimIndent(),
            )
        val input = json.decodeFromString<InteractionInput>(history.toString())
        assertTrue(input is InteractionInput.StepList)
        assertEquals("call-sig", input.value[0].signature)
        assertEquals("result-sig", input.value[1].signature)
        val request = CreateInteractionRequest(model = "gemini-4-neon", input = input, store = false)
        val fields = json.parseToJsonElement(json.encodeToString(request)).jsonObject
        assertEquals(history, fields["input"])
    }
}
