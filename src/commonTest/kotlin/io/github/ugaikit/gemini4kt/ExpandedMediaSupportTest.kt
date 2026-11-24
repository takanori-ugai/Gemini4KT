package io.github.ugaikit.gemini4kt

import io.github.ugaikit.gemini4kt.interaction.CreateInteractionRequest
import io.github.ugaikit.gemini4kt.interaction.InteractionContent
import io.github.ugaikit.gemini4kt.interaction.InteractionInput
import io.github.ugaikit.gemini4kt.interaction.InteractionPageOptions
import io.github.ugaikit.gemini4kt.interaction.InteractionSpreadsheetOptions
import io.github.ugaikit.gemini4kt.interaction.InteractionStaticProcessing
import io.github.ugaikit.gemini4kt.interaction.InteractionStep
import io.github.ugaikit.gemini4kt.interaction.InteractionVideoProcessing
import io.github.ugaikit.gemini4kt.interaction.interactionFunctionResult
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ExpandedMediaSupportTest {
    private val officeMimeTypes =
        listOf(
            "application/msword",
            "application/vnd.ms-powerpoint",
            "application/vnd.oasis.opendocument.presentation",
            "application/vnd.oasis.opendocument.spreadsheet",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.template",
            "application/vnd.oasis.opendocument.text",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-excel",
        )

    @Test
    fun officeFilesSupportInlineAndUriInputsWithDisplayNames() {
        for (mimeType in officeMimeTypes) {
            val reference =
                fileData {
                    this.mimeType = mimeType
                    fileUri = "https://generativelanguage.googleapis.com/v1beta/files/document"
                    displayName = "report"
                }
            val inline =
                inlineData {
                    mimeType { mimeType }
                    data { "AQID" }
                    displayName { "report-inline" }
                }
            val request =
                GenerateContentRequest(
                    contents = arrayOf(Content(parts = arrayOf(Part(fileData = reference), Part(inlineData = inline)))),
                )
            val decoded = Json.decodeFromString<GenerateContentRequest>(Json.encodeToString(request))
            val parts = checkNotNull(decoded.contents.single().parts)
            assertEquals(reference, parts[0].fileData)
            assertEquals(inline, parts[1].inlineData)
        }
        val oldShape = Json.decodeFromString<FileData>("""{"mimeType":"application/pdf","fileUri":"files/old"}""")
        assertNull(oldShape.displayName)
    }

    @Test
    fun functionResponsesAcceptFilesAudioVideoAndExistingMedia() {
        val mimeTypes = officeMimeTypes + listOf("audio/wav", "video/mp4", "image/png", "application/pdf", "text/plain")
        for (mimeType in mimeTypes) {
            val response =
                FunctionResponse(
                    name = "read_file",
                    response = buildJsonObject { },
                    parts =
                        arrayOf(
                            Part(inlineData = InlineData(mimeType, "AQID", "inline-result")),
                            Part(fileData = FileData(mimeType, "files/result", "uri-result")),
                        ),
                )
            val encoded = Json.encodeToString(response)
            val fields =
                Json
                    .parseToJsonElement(encoded)
                    .jsonObject
                    .getValue("parts")
                    .jsonArray
            assertEquals(
                JsonPrimitive("inline-result"),
                fields[0].jsonObject["inlineData"]?.jsonObject?.get("displayName"),
            )
            assertEquals(JsonPrimitive("uri-result"), fields[1].jsonObject["fileData"]?.jsonObject?.get("displayName"))
            val parts = checkNotNull(Json.decodeFromString<FunctionResponse>(encoded).parts)
            assertEquals(mimeType, parts[0].inlineData?.mimeType)
            assertEquals(mimeType, parts[1].fileData?.mimeType)
            assertEquals("files/result", parts[1].fileData?.fileUri)
        }
    }

    @Test
    fun videoFunctionResponsePartsPreserveProcessingModes() {
        for (mode in listOf(MediaProcessing.STATIC, MediaProcessing.AGENTIC)) {
            val video =
                part {
                    fileData { FileData("video/mp4", "files/video", "clip") }
                    mediaProcessing { mode }
                }
            val response = FunctionResponse("read_video", buildJsonObject { }, arrayOf(video))
            val encoded = Json.encodeToString(response)
            val fields =
                Json
                    .parseToJsonElement(encoded)
                    .jsonObject
                    .getValue("parts")
                    .jsonArray
                    .single()
                    .jsonObject
            assertEquals(JsonPrimitive(mode.name), fields["mediaProcessing"])
            val decoded = Json.decodeFromString<FunctionResponse>(encoded)
            assertEquals(mode, decoded.parts?.single()?.mediaProcessing)
        }
    }

    @Test
    fun interactionFunctionResultsAcceptDocumentAudioAndVideoByDataOrUri() {
        val mediaTypes =
            officeMimeTypes.map { "document" to it } + listOf("audio" to "audio/wav", "video" to "video/mp4")
        for ((type, mimeType) in mediaTypes) {
            for (mode in listOf(InteractionVideoProcessing.STATIC, InteractionVideoProcessing.AGENTIC)) {
                val content =
                    arrayOf(
                        InteractionContent(type = type, mimeType = mimeType, data = "AQID"),
                        InteractionContent(
                            type = type,
                            mimeType = mimeType,
                            uri = "files/result",
                            processing = if (type == "video") mode else null,
                        ),
                    )
                val result = Json.encodeToJsonElement(content)
                val step =
                    InteractionStep(type = "function_result", name = "read_file", callId = "call_1", result = result)
                val request = CreateInteractionRequest(input = InteractionInput.StepList(arrayOf(step)))
                val encoded = Json.encodeToString(request)
                val decoded = Json.decodeFromString<CreateInteractionRequest>(encoded)
                val decodedStep = (decoded.input as InteractionInput.StepList).value.single()
                assertEquals("call_1", decodedStep.callId)
                assertEquals(result, decodedStep.result)
                if (type == "video") {
                    assertEquals(JsonPrimitive(mode.name.lowercase()), result.jsonArray[1].jsonObject["processing"])
                }
            }
        }
    }

    @Test
    fun generateContentSerializesSlicingInInputsAndFunctionResponseParts() {
        val attachments =
            arrayOf(
                part {
                    fileData { FileData("application/msword", "files/report") }
                    documentMetadata { DocumentMetadata(3, 7, listOf("PAGE_TEXT")) }
                },
                part {
                    fileData { FileData("application/vnd.ms-excel", "files/sheet") }
                    spreadsheetMetadata { SpreadsheetMetadata(2, "A1:H50", listOf("FORMULAS")) }
                },
                part {
                    inlineData {
                        mimeType { "audio/wav" }
                        data { "AQID" }
                    }
                    audioMetadata { AudioMetadata("10.5s", "30s") }
                },
                part {
                    fileData { FileData("video/mp4", "files/video") }
                    videoMetadata { VideoMetadata("1s", "5s", 2.0, listOf("TRANSCRIPT")) }
                },
            )
        val request = GenerateContentRequest(contents = arrayOf(Content(parts = attachments)))
        val input =
            Json
                .encodeToJsonElement(request)
                .jsonObject
                .getValue("contents")
                .jsonArray
                .single()
                .jsonObject
        val response = FunctionResponse("inspect", buildJsonObject { }, attachments)
        assertEquals(input["parts"], Json.encodeToJsonElement(response).jsonObject["parts"])

        val parts = input.getValue("parts").jsonArray.map { it.jsonObject }
        assertEquals(
            Json.parseToJsonElement("""{"startPage":3,"endPage":7,"contentLayers":["PAGE_TEXT"]}"""),
            parts[0]["documentMetadata"],
        )
        assertEquals(
            Json.parseToJsonElement("""{"sheetIndex":2,"visibleCells":"A1:H50","contentLayers":["FORMULAS"]}"""),
            parts[1]["spreadsheetMetadata"],
        )
        assertEquals(
            Json.parseToJsonElement("""{"startOffset":"10.5s","endOffset":"30s"}"""),
            parts[2]["audioMetadata"],
        )
        assertEquals(
            Json.parseToJsonElement(
                """{"startOffset":"1s","endOffset":"5s","fps":2.0,"contentLayers":["TRANSCRIPT"]}""",
            ),
            parts[3]["videoMetadata"],
        )
    }

    @Test
    fun interactionsSerializeSlicingInInputsAndFunctionResults() {
        val attachments = slicedInteractionAttachments()
        val prompt =
            CreateInteractionRequest(
                model = "gemini-4-neon",
                input = InteractionInput.InteractionContentList(attachments),
            )
        val encodedPrompt = Json.encodeToJsonElement(prompt).jsonObject
        val input = encodedPrompt.getValue("input").jsonArray
        val result = interactionFunctionResult("call_1", "inspect", attachments)
        val followUp =
            prompt.copy(
                input = InteractionInput.StepList(arrayOf(result)),
                previousInteractionId = "interaction_1",
            )
        val encodedFollowUp = Json.encodeToJsonElement(followUp).jsonObject
        val step =
            encodedFollowUp
                .getValue("input")
                .jsonArray
                .single()
                .jsonObject
        assertEquals(input, step["result"])
        assertEquals(JsonPrimitive("call_1"), step["call_id"])
        val expected =
            Json.parseToJsonElement(
                """
                [
                  {"type":"static","page_options":{"start_page":3,"end_page":7,"content_layers":["page_text"]}},
                  {"type":"static","spreadsheet_options":{
                    "sheet_index":2,"visible_cells":"A1:H50","content_layers":["formulas"]
                  }},
                  {"type":"static","start_offset":"10.5s","end_offset":"30s"},
                  {"type":"static","start_offset":"1s","end_offset":"5s","fps":2.0,"content_layers":["transcript"]}
                ]
                """.trimIndent(),
            )
        assertEquals(expected.jsonArray, input.map { it.jsonObject.getValue("processing") })
        assertEquals(prompt, Json.decodeFromString<CreateInteractionRequest>(Json.encodeToString(prompt)))
    }

    private fun slicedInteractionAttachments(): Array<InteractionContent> =
        arrayOf(
            InteractionContent(
                type = "document",
                uri = "files/report",
                mimeType = "application/vnd.oasis.opendocument.text",
                processing =
                    InteractionStaticProcessing(
                        pageOptions = InteractionPageOptions(3, 7, listOf("page_text")),
                    ),
            ),
            InteractionContent(
                type = "document",
                uri = "files/sheet",
                mimeType = "application/vnd.oasis.opendocument.spreadsheet",
                processing =
                    InteractionStaticProcessing(
                        spreadsheetOptions = InteractionSpreadsheetOptions(2, "A1:H50", listOf("formulas")),
                    ),
            ),
            InteractionContent(
                type = "audio",
                data = "AQID",
                mimeType = "audio/wav",
                processing = InteractionStaticProcessing(startOffset = "10.5s", endOffset = "30s"),
            ),
            InteractionContent(
                type = "video",
                uri = "files/video",
                mimeType = "video/mp4",
                processing =
                    InteractionStaticProcessing(
                        startOffset = "1s",
                        endOffset = "5s",
                        fps = 2.0,
                        contentLayers = listOf("transcript"),
                    ),
            ),
        )
}
