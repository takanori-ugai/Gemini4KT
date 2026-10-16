package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.GeminiAI
import io.github.ugaikit.gemini4kt.TestUtils.captureStdout
import io.github.ugaikit.gemini4kt.interaction.Interaction
import io.github.ugaikit.gemini4kt.interaction.InteractionContent
import io.github.ugaikit.gemini4kt.interaction.InteractionInput
import io.github.ugaikit.gemini4kt.interaction.InteractionStatus
import io.github.ugaikit.gemini4kt.interaction.InteractionVideoProcessing
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class InteractionVideoSampleTest {
    @Test
    fun runVideoInputSubmitsAgenticVideoAndPollsUntilComplete() =
        runTest {
            val client = mockk<GeminiAI>(relaxed = true)
            val processing = Interaction(id = "video-1", status = InteractionStatus.IN_PROGRESS)
            val completed =
                Interaction(
                    id = "video-1",
                    status = InteractionStatus.COMPLETED,
                    outputTextRaw = "A short video summary.",
                )
            coEvery { client.createInteraction(any()) } returns processing
            coEvery { client.getInteraction("video-1") } returns completed

            val output = captureStdout { InteractionSamples.runVideoInput(client = client) }

            coVerify(exactly = 1) {
                client.createInteraction(
                    match { request ->
                        request.model == "gemini-3.8-flash" &&
                            request.background == true &&
                            request.input ==
                            InteractionInput.InteractionContentList(
                                arrayOf(
                                    InteractionContent(
                                        type = "video",
                                        uri = "https://www.youtube.com/watch?v=9hE5-98ZeCg",
                                        processing = InteractionVideoProcessing.AGENTIC,
                                    ),
                                    InteractionContent(
                                        type = "text",
                                        text = "Summarize this video and mention its key moments with timestamps.",
                                    ),
                                ),
                            )
                    },
                )
            }
            coVerify(exactly = 1) { client.getInteraction("video-1") }
            coVerify(exactly = 0) { client.close() }
            assertTrue(output.contains("A short video summary."))
        }

    @Test
    fun runVideoInputAcceptsCustomUriAndCompletesImmediately() =
        runTest {
            val client = mockk<GeminiAI>(relaxed = true)
            coEvery { client.createInteraction(any()) } returns
                Interaction(id = "video-2", status = InteractionStatus.COMPLETED)

            InteractionSamples.runVideoInput(videoUri = "files/custom-video", client = client)

            coVerify {
                client.createInteraction(
                    match { request ->
                        (request.input as InteractionInput.InteractionContentList)
                            .value[0].uri == "files/custom-video"
                    },
                )
            }
            coVerify(exactly = 0) { client.getInteraction(any()) }
        }
}
