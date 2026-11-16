package io.github.ugaikit.gemini4kt

import io.github.ugaikit.gemini4kt.interaction.InteractionTool
import io.github.ugaikit.gemini4kt.samples.ComputerUseSample
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ComputerUseGaTest {
    @Test
    fun generateContentSupportsGaEnvironmentsAndOptions() {
        val environments =
            listOf(Environment.ENVIRONMENT_BROWSER, Environment.ENVIRONMENT_MOBILE, Environment.ENVIRONMENT_DESKTOP)
        for (environment in environments) {
            for (detection in listOf(false, true)) {
                val config =
                    computerUse {
                        this.environment = environment
                        enablePromptInjectionDetection = detection
                        disabledSafetyPolicies = arrayOf("DATA_MODIFICATION")
                    }
                val encoded = Json.encodeToString(config)
                val fields = Json.parseToJsonElement(encoded).jsonObject
                assertEquals(JsonPrimitive(environment.name), fields["environment"])
                assertEquals(JsonPrimitive(detection), fields["enablePromptInjectionDetection"])
                assertEquals(JsonPrimitive("DATA_MODIFICATION"), fields["disabledSafetyPolicies"]?.jsonArray?.single())
                val decoded = Json.decodeFromString<ComputerUse>(encoded)
                assertEquals(config, decoded)
                assertEquals(config.hashCode(), decoded.hashCode())
            }
        }
    }

    @Test
    fun interactionsUseSnakeCaseGaOptions() {
        for (environment in listOf("browser", "mobile", "desktop")) {
            val tool =
                InteractionTool(
                    type = "computer_use",
                    environment = environment,
                    enablePromptInjectionDetection = false,
                    disabledSafetyPolicies = arrayOf("data_modification"),
                )
            val encoded = Json.encodeToString(tool)
            val fields = Json.parseToJsonElement(encoded).jsonObject
            assertEquals(JsonPrimitive(false), fields["enable_prompt_injection_detection"])
            assertEquals(JsonPrimitive("data_modification"), fields["disabled_safety_policies"]?.jsonArray?.single())
            val decoded = Json.decodeFromString<InteractionTool>(encoded)
            assertEquals(tool, decoded)
            assertEquals(tool.hashCode(), decoded.hashCode())
        }
    }

    @Test
    fun newOptionsParticipateInEquality() {
        val config =
            ComputerUse(enablePromptInjectionDetection = true, disabledSafetyPolicies = arrayOf("DATA_MODIFICATION"))
        assertNotEquals(config, config.copy(enablePromptInjectionDetection = false))
        assertNotEquals(config, config.copy(disabledSafetyPolicies = arrayOf("ACCOUNT_CREATION")))
        val tool = InteractionTool(type = "computer_use", enablePromptInjectionDetection = true)
        assertNotEquals(tool, tool.copy(enablePromptInjectionDetection = false))
        assertNotEquals(tool, tool.copy(disabledSafetyPolicies = arrayOf("account_creation")))
    }

    @Test
    fun gaSamplesUseSupportedModelsAndCorrectToolShapes() =
        runTest {
            val json = Json { ignoreUnknownKeys = true }
            val client =
                HttpClient(MockEngine) {
                    engine {
                        addHandler { request ->
                            val body = Json.parseToJsonElement((request.body as TextContent).text).jsonObject
                            val tool = body["tools"]?.jsonArray?.single()?.jsonObject
                            if (request.url.encodedPath.endsWith("interactions")) {
                                assertEquals(JsonPrimitive("gemini-4-hydrogen"), body["model"])
                                assertEquals(JsonPrimitive("computer_use"), tool?.get("type"))
                                assertEquals(JsonPrimitive(true), tool?.get("enable_prompt_injection_detection"))
                                respond(
                                    """{"id":"cu_1","status":"completed"}""",
                                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                                )
                            } else {
                                assertEquals(
                                    "/v1beta/models/gemini-4-hydrogen:generateContent",
                                    request.url.encodedPath,
                                )
                                val config = tool?.get("computer_use")?.jsonObject
                                assertEquals(JsonPrimitive("ENVIRONMENT_BROWSER"), config?.get("environment"))
                                assertEquals(JsonPrimitive(true), config?.get("enablePromptInjectionDetection"))
                                respond(
                                    """{"candidates":[]}""",
                                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                                )
                            }
                        }
                    }
                    install(ContentNegotiation) { json(json) }
                }
            try {
                ComputerUseSample.run(GeminiAI(client = client, apiKey = "test-key"))
                ComputerUseSample.runGenerateContent(Gemini("test-key", client = client))
            } finally {
                client.close()
            }
        }
}
