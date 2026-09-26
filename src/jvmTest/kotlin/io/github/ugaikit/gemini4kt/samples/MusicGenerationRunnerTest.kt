package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.interaction.Interaction
import io.github.ugaikit.gemini4kt.interaction.InteractionStatus
import kotlinx.coroutines.test.runTest
import java.io.IOException
import java.nio.file.Files
import java.util.Base64
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MusicGenerationRunnerTest {
    @Test
    fun runWritesGeneratedMp3() =
        runTest {
            val outputDir = Files.createTempDirectory("music-runner").resolve("nested").toFile()
            val expectedAudio = byteArrayOf(0, 1, 2, 3)
            val interaction =
                Interaction(
                    id = "lyria_123",
                    status = InteractionStatus.COMPLETED,
                    outputTextRaw = "[Verse] Homeward",
                )

            val outputFile =
                MusicGenerationRunner.run(outputDir) { onAudioData ->
                    onAudioData(Base64.getEncoder().encodeToString(expectedAudio))
                    interaction
                }

            assertEquals(outputDir.resolve("generated_music.mp3"), outputFile)
            assertContentEquals(expectedAudio, outputFile!!.readBytes())
        }

    @Test
    fun runDoesNotWriteFileWhenAudioIsMissing() =
        runTest {
            val outputDir = Files.createTempDirectory("music-runner-empty").resolve("nested").toFile()
            val interaction = Interaction(id = "lyria_123", status = InteractionStatus.COMPLETED)

            val outputFile =
                MusicGenerationRunner.run(outputDir) { onAudioData ->
                    onAudioData("")
                    interaction
                }

            assertNull(outputFile)
            assertFalse(outputDir.resolve("generated_music.mp3").exists())
        }

    @Test
    fun runRejectsOutputFileBeforeCallingGenerator() =
        runTest {
            val outputPath = Files.createTempFile("music-runner-output", ".tmp").toFile()
            var generatorCalled = false

            assertFailsWith<IOException> {
                MusicGenerationRunner.run(outputPath) {
                    generatorCalled = true
                    Interaction(id = "lyria_123", status = InteractionStatus.COMPLETED)
                }
            }

            assertFalse(generatorCalled)
        }

    @Test
    fun runRemovesTemporaryFileWhenGenerationFails() =
        runTest {
            val outputDir = Files.createTempDirectory("music-runner-failure").toFile()

            assertFailsWith<IllegalStateException> {
                MusicGenerationRunner.run(outputDir) { onAudioData ->
                    onAudioData(Base64.getEncoder().encodeToString(byteArrayOf(1, 2, 3)))
                    error("generation failed")
                }
            }

            assertTrue(outputDir.listFiles().orEmpty().isEmpty())
        }
}
