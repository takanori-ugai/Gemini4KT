package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.live.music.AudioChunk
import io.github.ugaikit.gemini4kt.live.music.LiveMusic
import io.github.ugaikit.gemini4kt.live.music.LiveMusicServerContent
import io.github.ugaikit.gemini4kt.live.music.LiveMusicServerMessage
import io.github.ugaikit.gemini4kt.live.music.LiveMusicSession
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class MusicGenerationJvmTest {
    @Test
    fun runStreamsAudioAndCleansUpSession() =
        runTest {
            val session = mockk<LiveMusicSession>(relaxed = true)
            val chunk = AudioChunk(data = "audio-data", mimeType = "audio/pcm")
            val serverMessage =
                LiveMusicServerMessage(
                    serverContent = LiveMusicServerContent(audioChunks = listOf(chunk)),
                )
            coEvery { session.receive() } returns flowOf(serverMessage)

            val client = mockk<LiveMusic>()
            coEvery { client.connect() } returns session

            val received = mutableListOf<String>()

            MusicGeneration.run(
                onAudioData = { received.add(it) },
                liveMusicClient = client,
                apiKey = "dummy-key",
            )

            assertEquals(listOf("audio-data"), received)
            coVerify(exactly = 1) { session.setWeightedPrompts(any()) }
            coVerify(exactly = 1) { session.setMusicGenerationConfig(any()) }
            coVerify(exactly = 1) { session.play() }
            coVerify(exactly = 1) { session.stop() }
            coVerify(exactly = 1) { session.close() }
        }
}
