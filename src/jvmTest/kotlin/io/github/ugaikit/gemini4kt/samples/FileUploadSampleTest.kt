package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.Candidate
import io.github.ugaikit.gemini4kt.Content
import io.github.ugaikit.gemini4kt.FileData
import io.github.ugaikit.gemini4kt.Gemini
import io.github.ugaikit.gemini4kt.GeminiFile
import io.github.ugaikit.gemini4kt.GenerateContentResponse
import io.github.ugaikit.gemini4kt.Part
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Verifies the FileUploadSample flow using mocked Gemini client.
 */
class FileUploadSampleTest {
    /**
     * Ensures the sample uploads a file, calls generateContent with the returned file URI, and prints output.
     */
    @Test
    fun runUploadsFileAndPrintsResponseText() =
        runTest {
            val gemini = mockk<Gemini>(relaxed = true)
            val uploaded =
                GeminiFile(
                    name = "files/123",
                    displayName = "Scones",
                    uri = "files/123",
                    mimeType = "image/jpeg",
                    createTime = "",
                    updateTime = "",
                    expirationTime = "",
                    sha256Hash = "",
                    sizeBytes = 1,
                )
            coEvery { gemini.uploadFile(any(), any(), any()) } returns uploaded

            coEvery {
                gemini.generateContent(any(), model = any())
            } returns
                GenerateContentResponse(
                    candidates =
                        listOf(
                            Candidate(
                                content =
                                    Content(
                                        parts = arrayOf(Part(text = "Scones and cream.")),
                                    ),
                            ),
                        ),
                )

            val output =
                captureStdout {
                    FileUploadSample.run(
                        imagePath = "scones.jpg",
                        gemini = gemini,
                    )
                }

            coVerify { gemini.uploadFile(any(), "image/jpeg", "Scones") }
            coVerify {
                gemini.generateContent(
                    match { request ->
                        request.contents.firstOrNull()?.parts?.any { part ->
                            part.fileData == FileData(mimeType = "image/jpeg", fileUri = "files/123")
                        } == true
                    },
                    model = "gemini-2.5-flash-lite",
                )
            }
            assertTrue(output.contains("Uploading file"))
            assertTrue(output.contains("File uploaded successfully"))
            assertTrue(output.contains("Scones and cream"))
        }

    private inline fun captureStdout(block: () -> Unit): String {
        val original = System.out
        val buffer = java.io.ByteArrayOutputStream()
        System.setOut(java.io.PrintStream(buffer))
        return try {
            block()
            buffer.toString()
        } finally {
            System.setOut(original)
        }
    }
}
