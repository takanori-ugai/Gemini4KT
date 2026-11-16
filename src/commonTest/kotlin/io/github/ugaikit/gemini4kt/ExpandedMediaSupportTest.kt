package io.github.ugaikit.gemini4kt

import io.github.ugaikit.gemini4kt.interaction.CreateInteractionRequest
import io.github.ugaikit.gemini4kt.interaction.InteractionContent
import io.github.ugaikit.gemini4kt.interaction.InteractionInput
import io.github.ugaikit.gemini4kt.interaction.InteractionStep
import io.github.ugaikit.gemini4kt.interaction.InteractionVideoProcessing
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
}
