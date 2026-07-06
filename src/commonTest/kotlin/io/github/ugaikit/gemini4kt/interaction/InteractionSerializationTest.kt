package io.github.ugaikit.gemini4kt.interaction

import io.github.ugaikit.gemini4kt.MediaResolution
import io.github.ugaikit.gemini4kt.MediaResolutionLevel
import io.github.ugaikit.gemini4kt.PrebuiltVoiceConfig
import io.github.ugaikit.gemini4kt.SpeechConfig
import io.github.ugaikit.gemini4kt.VoiceConfig
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.AbstractDecoder
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.modules.SerializersModule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class InteractionSerializationTest {
    private val json =
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = false
        }

    @Test
    fun interactionContentSerializesCorrectly() {
        val content =
            InteractionContent(
                type = "image",
                data = "base64",
                mimeType = "image/png",
            )
        val encoded = json.encodeToString(content)
        val expected =
            """
            {
              "type": "image",
              "data": "base64",
              "mime_type": "image/png"
            }
            """.trimIndent()
        assertEquals(json.parseToJsonElement(expected), json.parseToJsonElement(encoded))

        val decoded = json.decodeFromString<InteractionContent>(encoded)
        assertEquals(content, decoded)
    }

    @Test
    fun interactionToolSerializesAllowedTools() {
        val tool =
            InteractionTool(
                type = "function",
                name = "get_weather",
                description = "Weather lookup",
                allowedTools =
                    arrayOf(
                        InteractionAllowedTools(
                            mode = "auto",
                            tools = arrayOf("web_search", "code_execution"),
                        ),
                    ),
            )

        val encoded = json.encodeToString(tool)
        val expected =
            """
            {
              "type": "function",
              "name": "get_weather",
              "description": "Weather lookup",
              "allowed_tools": [
                {
                  "mode": "auto",
                  "tools": ["web_search", "code_execution"]
                }
              ]
            }
            """.trimIndent()
        assertEquals(json.parseToJsonElement(expected), json.parseToJsonElement(encoded))

        val decoded = json.decodeFromString<InteractionTool>(encoded)
        assertEquals(tool, decoded)
    }

    @Test
    fun interactionToolSerializesComputerUseConfig() {
        val tool =
            InteractionTool(
                type = "computer_use",
                environment = "browser",
                excludedPredefinedFunctions = arrayOf("CLICK", "TYPE"),
            )

        val encoded = json.encodeToString(tool)

        assertTrue(encoded.contains("\"computer_use\""))
        assertTrue(encoded.contains("\"excluded_predefined_functions\""))

        val decoded = json.decodeFromString<InteractionTool>(encoded)
        assertEquals(tool, decoded)
    }

    @Test
    fun interactionEnumSerializesToSnakeCase() {
        val statusJson = json.encodeToString(InteractionStatus.IN_PROGRESS)
        assertEquals("\"in_progress\"", statusJson)

        val modalityJson = json.encodeToString(InteractionResponseModality.TEXT)
        assertEquals("\"text\"", modalityJson)

        val decodedStatus = json.decodeFromString<InteractionStatus>("\"requires_action\"")
        assertEquals(InteractionStatus.REQUIRES_ACTION, decodedStatus)
    }

    @Test
    fun responseFormatRoundTrips() {
        val responseFormat =
            TextResponseFormat(
                mimeType = "application/json",
                schema =
                    buildJsonObject {
                        put("type", "object")
                        put(
                            "properties",
                            buildJsonObject {
                                put("summary", buildJsonObject { put("type", "string") })
                            },
                        )
                    },
            )

        val encoded = json.encodeToString(ResponseFormatSerializer, responseFormat)
        val decoded = json.decodeFromString(ResponseFormatSerializer, encoded)

        assertEquals(responseFormat, decoded)
    }

    @Test
    fun audioResponseFormatRoundTrips() {
        val responseFormat =
            AudioResponseFormat(
                mimeType = "audio/mp3",
                delivery = "inline",
                sampleRate = 24000,
                bitRate = 128000,
            )

        val encoded = json.encodeToString(ResponseFormatSerializer, responseFormat)
        val decoded = json.decodeFromString(ResponseFormatSerializer, encoded)

        assertEquals(responseFormat, decoded)
    }

    @Test
    fun imageResponseFormatRoundTrips() {
        val responseFormat =
            ImageResponseFormat(
                mimeType = "image/png",
                delivery = "inline",
                aspectRatio = "16:9",
                imageSize = "1024x1024",
            )

        val encoded = json.encodeToString(ResponseFormatSerializer, responseFormat)
        val decoded = json.decodeFromString(ResponseFormatSerializer, encoded)

        assertEquals(responseFormat, decoded)
    }

    @Test
    fun responseFormatRejectsNonObjectJson() {
        assertFailsWith<SerializationException> {
            json.decodeFromString(ResponseFormatSerializer, "\"text\"")
        }
    }

    @Test
    fun responseFormatRejectsMissingType() {
        assertFailsWith<SerializationException> {
            json.decodeFromString(
                ResponseFormatSerializer,
                """{"mime_type":"application/json"}""",
            )
        }
    }

    @Test
    fun responseFormatRejectsUnknownType() {
        assertFailsWith<SerializationException> {
            json.decodeFromString(
                ResponseFormatSerializer,
                """{"type":"markdown"}""",
            )
        }
    }

    @Test
    fun responseFormatRejectsNonJsonDecoder() {
        assertFailsWith<SerializationException> {
            ResponseFormatSerializer.deserialize(NonJsonDecoder)
        }
    }

    @Test
    fun environmentConfigRoundTrips() {
        val environment =
            EnvironmentConfig(
                sources =
                    arrayOf(
                        EnvironmentSource(
                            type = "inline",
                            target = ".agents/AGENTS.md",
                            content = "Write a summary table",
                        ),
                    ),
            )

        val encoded = json.encodeToString(InteractionEnvironmentSerializer, environment)
        val decoded = json.decodeFromString(InteractionEnvironmentSerializer, encoded)

        assertEquals(environment, decoded)
    }

    @Test
    fun environmentReferenceRoundTrips() {
        val environment = EnvironmentReference(id = "remote-env-1")

        val encoded = json.encodeToString(InteractionEnvironmentSerializer, environment)
        val decoded = json.decodeFromString(InteractionEnvironmentSerializer, encoded)

        assertEquals("\"remote-env-1\"", encoded)
        assertEquals(environment, decoded)
    }

    @Test
    fun interactionRoundTripsWithOutputsAndTools() {
        val content = InteractionContent(type = "text", text = "Hello")
        val steps =
            arrayOf(
                InteractionStep(
                    type = "user_input",
                    content = arrayOf(InteractionContent(type = "text", text = "Hello")),
                ),
            )
        val tool =
            InteractionTool(
                type = "function",
                name = "say_hi",
                parameters =
                    buildJsonObject {
                        put("type", "object")
                    },
            )
        val interaction =
            Interaction(
                id = "inter_1",
                status = InteractionStatus.COMPLETED,
                outputs = arrayOf(content),
                tools = arrayOf(tool),
                responseModalities = arrayOf(InteractionResponseModality.TEXT),
                responseFormat =
                    TextResponseFormat(
                        mimeType = "application/json",
                        schema =
                            buildJsonObject {
                                put("type", "object")
                            },
                    ),
                steps = steps,
            )

        val encoded = json.encodeToString(interaction)
        val decoded = json.decodeFromString<Interaction>(encoded)
        assertEquals(interaction, decoded)
    }

    @Test
    fun interactionGenerationConfigCoversThinkingEnumsAndArrayEquality() {
        val left =
            InteractionGenerationConfig(
                stopSequences = arrayOf("END"),
                thinkingLevel = ThinkingLevel.HIGH,
                thinkingSummaries = ThinkingSummaries.AUTO,
                maxOutputTokens = 128,
                speechConfig =
                    SpeechConfig(
                        voiceConfig =
                            VoiceConfig(
                                prebuiltVoiceConfig =
                                    PrebuiltVoiceConfig(voiceName = "Kore"),
                            ),
                    ),
            )
        val right =
            InteractionGenerationConfig(
                stopSequences = arrayOf("END"),
                thinkingLevel = ThinkingLevel.HIGH,
                thinkingSummaries = ThinkingSummaries.AUTO,
                maxOutputTokens = 128,
                speechConfig =
                    SpeechConfig(
                        voiceConfig =
                            VoiceConfig(
                                prebuiltVoiceConfig =
                                    PrebuiltVoiceConfig(voiceName = "Kore"),
                            ),
                    ),
            )

        assertEquals(left, right)
        assertEquals(left.hashCode(), right.hashCode())
        val encoded = json.encodeToString(left)
        assertTrue(encoded.contains("\"thinking_level\":\"high\""))
        assertTrue(encoded.contains("\"thinking_summaries\":\"auto\""))
        assertTrue(encoded.contains("\"speech_config\":{"))
    }

    private object NonJsonDecoder : AbstractDecoder() {
        override val serializersModule: SerializersModule = SerializersModule {}

        override fun decodeElementIndex(descriptor: kotlinx.serialization.descriptors.SerialDescriptor): Int = CompositeDecoder.DECODE_DONE
    }

    @Test
    fun interactionContentRoundTripWithSummaryUrlsAndAnnotations() {
        val content =
            InteractionContent(
                type = "tool_result",
                text = "ok",
                urls = arrayOf("https://example.com"),
                summary =
                    InteractionThoughtSummary(
                        content = InteractionContent(type = "text", text = "reasoning"),
                    ),
                annotations =
                    arrayOf(
                        InteractionAnnotation(startIndex = 0, endIndex = 2, source = "unit-test"),
                    ),
            )

        val encoded = json.encodeToString(content)
        val decoded = json.decodeFromString<InteractionContent>(encoded)
        assertEquals(content, decoded)
        assertEquals(content.hashCode(), decoded.hashCode())
    }

    @Test
    fun interactionRoundTripsWithTypedOutputMedia() {
        val interaction =
            Interaction(
                id = "inter_media",
                status = InteractionStatus.COMPLETED,
                outputImage =
                    InteractionImageContent(
                        type = "image",
                        data = "image-base64",
                        mimeType = "image/png",
                        resolution = MediaResolution(level = MediaResolutionLevel.MEDIA_RESOLUTION_HIGH),
                    ),
                outputAudio =
                    InteractionAudioContent(
                        type = "audio",
                        data = "audio-base64",
                        mimeType = "audio/mp3",
                        channels = 2,
                        sampleRate = 24000,
                    ),
                outputVideo =
                    InteractionVideoContent(
                        type = "video",
                        data = "video-base64",
                        mimeType = "video/mp4",
                        resolution = MediaResolution(level = MediaResolutionLevel.MEDIA_RESOLUTION_MEDIUM),
                    ),
            )

        val encoded = json.encodeToString(interaction)
        val decoded = json.decodeFromString<Interaction>(encoded)

        assertEquals(interaction, decoded)
    }

    @Test
    fun interactionOutputTextFallsBackToLastModelStep() {
        val interaction =
            Interaction(
                id = "inter_text",
                status = InteractionStatus.COMPLETED,
                steps =
                    arrayOf(
                        InteractionStep(
                            type = "user_input",
                            content = arrayOf(InteractionContent(type = "text", text = "Hello")),
                        ),
                        InteractionStep(
                            type = "model_output",
                            content = arrayOf(InteractionContent(type = "text", text = "Hello from the model.")),
                        ),
                    ),
            )

        assertEquals("Hello from the model.", interaction.outputText)
    }

    @Test
    fun interactionAgentConfigSerializesThinkingSummaries() {
        val config =
            InteractionAgentConfig(
                type = "remote",
                thinkingSummaries = ThinkingSummaries.NONE,
            )

        val encoded = json.encodeToString(config)
        assertTrue(encoded.contains("\"thinking_summaries\":\"none\""))
    }

    @Test
    fun interactionModalityTokenCountConstructs() {
        val count =
            InteractionModalityTokenCount(
                modality = InteractionResponseModality.TEXT,
                tokens = 42,
            )
        assertEquals(42, count.tokens)
        assertEquals(InteractionResponseModality.TEXT, count.modality)
    }

    @Test
    fun testInteractionEqualsAndHashCode() {
        val steps1 =
            arrayOf(
                InteractionStep(
                    type = "user_input",
                    content = arrayOf(InteractionContent(type = "text", text = "step1")),
                ),
            )
        val steps2 =
            arrayOf(
                InteractionStep(
                    type = "user_input",
                    content = arrayOf(InteractionContent(type = "text", text = "step1")),
                ),
            )
        val stepsDifferent =
            arrayOf(
                InteractionStep(
                    type = "user_input",
                    content = arrayOf(InteractionContent(type = "text", text = "stepDifferent")),
                ),
            )

        val interaction1 =
            Interaction(
                id = "inter_1",
                status = InteractionStatus.COMPLETED,
                environmentId = "env_1",
                outputTextRaw = "output",
                steps = steps1,
            )
        val interaction2 =
            Interaction(
                id = "inter_1",
                status = InteractionStatus.COMPLETED,
                environmentId = "env_1",
                outputTextRaw = "output",
                steps = steps2,
            )
        val interactionDiffId =
            Interaction(
                id = "inter_2",
                status = InteractionStatus.COMPLETED,
                environmentId = "env_1",
                outputTextRaw = "output",
                steps = steps1,
            )
        val interactionDiffStatus =
            Interaction(
                id = "inter_1",
                status = InteractionStatus.IN_PROGRESS,
                environmentId = "env_1",
                outputTextRaw = "output",
                steps = steps1,
            )
        val interactionDiffEnvId =
            Interaction(
                id = "inter_1",
                status = InteractionStatus.COMPLETED,
                environmentId = "env_2",
                outputTextRaw = "output",
                steps = steps1,
            )
        val interactionDiffOutput =
            Interaction(
                id = "inter_1",
                status = InteractionStatus.COMPLETED,
                environmentId = "env_1",
                outputTextRaw = "output_diff",
                steps = steps1,
            )
        val interactionDiffSteps =
            Interaction(
                id = "inter_1",
                status = InteractionStatus.COMPLETED,
                environmentId = "env_1",
                outputTextRaw = "output",
                steps = stepsDifferent,
            )
        val interactionNullSteps1 =
            Interaction(
                id = "inter_1",
                status = InteractionStatus.COMPLETED,
                environmentId = "env_1",
                outputTextRaw = "output",
                steps = null,
            )
        val interactionNullSteps2 =
            Interaction(
                id = "inter_1",
                status = InteractionStatus.COMPLETED,
                environmentId = "env_1",
                outputTextRaw = "output",
                steps = null,
            )

        // Test Interaction equals and hashCode
        assertEquals(interaction1, interaction1)
        assertEquals(interaction1, interaction2)
        assertEquals(interaction1.hashCode(), interaction2.hashCode())
        assertEquals(interactionNullSteps1, interactionNullSteps2)
        assertEquals(interactionNullSteps1.hashCode(), interactionNullSteps2.hashCode())

        // Check inequality cases
        kotlin.test.assertNotEquals(interaction1, null as Interaction?)
        kotlin.test.assertNotEquals(interaction1, "not an interaction" as Any?)
        kotlin.test.assertNotEquals(interaction1, interactionDiffId)
        kotlin.test.assertNotEquals(interaction1, interactionDiffStatus)
        kotlin.test.assertNotEquals(interaction1, interactionDiffEnvId)
        kotlin.test.assertNotEquals(interaction1, interactionDiffOutput)
        kotlin.test.assertNotEquals(interaction1, interactionDiffSteps)
        kotlin.test.assertNotEquals(interaction1, interactionNullSteps1)
        kotlin.test.assertNotEquals(interactionNullSteps1, interaction1)

        val request1 =
            CreateInteractionRequest(
                model = "gemini-2.5-flash",
                input = "Hello",
                environment = EnvironmentReference(id = "remote"),
            )
        val request2 =
            CreateInteractionRequest(
                model = "gemini-2.5-flash",
                input = "Hello",
                environment = EnvironmentReference(id = "remote"),
            )
        val requestDiffModel =
            CreateInteractionRequest(
                model = "gemini-1.5-flash",
                input = "Hello",
                environment = EnvironmentReference(id = "remote"),
            )
        val requestDiffInput =
            CreateInteractionRequest(
                model = "gemini-2.5-flash",
                input = "World",
                environment = EnvironmentReference(id = "remote"),
            )
        val requestDiffEnv =
            CreateInteractionRequest(
                model = "gemini-2.5-flash",
                input = "Hello",
                environment = EnvironmentReference(id = "local"),
            )

        // Test CreateInteractionRequest equals and hashCode
        assertEquals(request1, request1)
        assertEquals(request1, request2)
        assertEquals(request1.hashCode(), request2.hashCode())

        // Check inequality cases
        kotlin.test.assertNotEquals(request1, null as CreateInteractionRequest?)
        kotlin.test.assertNotEquals(request1, "not a request" as Any?)
        kotlin.test.assertNotEquals(request1, requestDiffModel)
        kotlin.test.assertNotEquals(request1, requestDiffInput)
        kotlin.test.assertNotEquals(request1, requestDiffEnv)
    }
}
