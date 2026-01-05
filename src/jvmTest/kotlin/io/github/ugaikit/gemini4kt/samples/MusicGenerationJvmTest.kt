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
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class MusicGenerationJvmTest {
    @BeforeTest
    fun setApiKey() {
        setEnv("GEMINI_API_KEY", "dummy-key")
    }

    @Test
    fun runStreamsAudioAndCleansUpSession() =
        runTest {
            val session = mockk<LiveMusicSession>(relaxed = true)
            val chunk = AudioChunk(data = "audio-data", mimeType = "audio/pcm")
            val serverMessage = LiveMusicServerMessage(serverContent = LiveMusicServerContent(audioChunks = listOf(chunk)))
            coEvery { session.receive() } returns flowOf(serverMessage)

            val client = mockk<LiveMusic>()
            coEvery { client.connect() } returns session

            val received = mutableListOf<String>()

            MusicGeneration.run(onAudioData = { received.add(it) }, liveMusicClient = client)

            assertEquals(listOf("audio-data"), received)
            coVerify(exactly = 1) { session.setWeightedPrompts(any()) }
            coVerify(exactly = 1) { session.setMusicGenerationConfig(any()) }
            coVerify(exactly = 1) { session.play() }
            coVerify(exactly = 1) { session.stop() }
            coVerify(exactly = 1) { session.close() }
        }

    private fun setEnv(
        key: String,
        value: String,
    ) {
        try {
            val processEnvironment = Class.forName("java.lang.ProcessEnvironment")
            val ciEnv = processEnvironment.getDeclaredField("theCaseInsensitiveEnvironment")
            ciEnv.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            (ciEnv.get(null) as MutableMap<String, String>)[key] = value
            val env = processEnvironment.getDeclaredField("theEnvironment")
            env.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            (env.get(null) as MutableMap<String, String>)[key] = value
        } catch (_: Exception) {
            val env = System.getenv()
            val cl = env.javaClass
            try {
                val m = cl.getDeclaredField("m")
                m.isAccessible = true
                @Suppress("UNCHECKED_CAST")
                (m.get(env) as MutableMap<String, String>)[key] = value
            } catch (_: Exception) {
                // If we cannot mutate the environment, let the test fall back to existing values.
            }
        }
    }
}
