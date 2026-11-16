package io.github.ugaikit.gemini4kt

import io.github.ugaikit.gemini4kt.interaction.CreateInteractionRequest
import io.github.ugaikit.gemini4kt.interaction.Interaction
import io.github.ugaikit.gemini4kt.interaction.InteractionContent
import io.github.ugaikit.gemini4kt.interaction.InteractionGenerationConfig
import io.github.ugaikit.gemini4kt.interaction.InteractionInput
import io.github.ugaikit.gemini4kt.interaction.InteractionVideoConfig
import io.github.ugaikit.gemini4kt.interaction.InteractionVideoTask
import io.github.ugaikit.gemini4kt.samples.OmniVideoSample
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class OmniVideoSupportTest {
    @Test
    fun defaultVideoRequestUsesOmni12WithoutForcingATask() {
        val request = OmniVideoSample.request()
        assertEquals("gemini-omni-1.2-flash", request.model)
        assertNull(request.generationConfig)
        val decoded = Json.decodeFromString<CreateInteractionRequest>(Json.encodeToString(request))
        assertEquals(request, decoded)
    }

    @Test
    fun videoTaskOverridesUseApiValuesAndParticipateInEquality() {
        val names = listOf("text_to_video", "image_to_video", "reference_to_video", "edit", "extend")
        for ((task, name) in InteractionVideoTask.entries.zip(names)) {
            val config = InteractionGenerationConfig(videoConfig = InteractionVideoConfig(task))
            val encoded = Json.encodeToString(config)
            val fields =
                Json
                    .parseToJsonElement(encoded)
                    .jsonObject
                    .getValue("video_config")
                    .jsonObject
            assertEquals(JsonPrimitive(name), fields["task"])
            val decoded = Json.decodeFromString<InteractionGenerationConfig>(encoded)
            assertEquals(config, decoded)
            assertEquals(config.hashCode(), decoded.hashCode())
            assertNotEquals(config, config.copy(videoConfig = null))
        }
    }

    @Test
    fun referenceAndInterpolationRequestsPreserveAllImages() {
        val cases = listOf(6 to InteractionVideoTask.REFERENCE_TO_VIDEO, 2 to InteractionVideoTask.IMAGE_TO_VIDEO)
        for ((count, task) in cases) {
            val images =
                Array(count) { InteractionContent(type = "image", uri = "files/image-$it", mimeType = "image/png") }
            val request = OmniVideoSample.request("Animate these images for 8 seconds.", images, task)
            val decoded = Json.decodeFromString<CreateInteractionRequest>(Json.encodeToString(request))
            val input = (decoded.input as InteractionInput.InteractionContentList).value
            assertEquals(count + 1, input.size)
            assertEquals(images.toList(), input.take(count))
            assertEquals(task, decoded.generationConfig?.videoConfig?.task)
        }
    }

    @Test
    fun videoExtensionRetainsConversationIdAndTask() {
        val request =
            OmniVideoSample.request(
                prompt = "Extend the video by 5 seconds with the marble reaching the finish line.",
                task = InteractionVideoTask.EXTEND,
                previousInteractionId = "video_1",
            )
        val decoded = Json.decodeFromString<CreateInteractionRequest>(Json.encodeToString(request))
        assertEquals("video_1", decoded.previousInteractionId)
        assertEquals(InteractionVideoTask.EXTEND, decoded.generationConfig?.videoConfig?.task)
    }

    @Test
    fun generatedVideosDecodeFromOutputVideo() {
        val response =
            Json.decodeFromString<Interaction>(
                """{"id":"video_1","status":"completed","output_video":{"data":"AQID","mime_type":"video/mp4"}}""",
            )
        assertEquals("AQID", response.outputVideo?.data)
        assertEquals("video/mp4", response.outputVideo?.mimeType)
    }
}
