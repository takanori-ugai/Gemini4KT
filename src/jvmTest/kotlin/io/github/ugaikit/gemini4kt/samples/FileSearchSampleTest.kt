package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.Candidate
import io.github.ugaikit.gemini4kt.Content
import io.github.ugaikit.gemini4kt.GenerateContentResponse
import io.github.ugaikit.gemini4kt.Part
import io.github.ugaikit.gemini4kt.TestUtils.captureStdout
import io.github.ugaikit.gemini4kt.filesearch.FileSearch
import io.github.ugaikit.gemini4kt.filesearch.FileSearchStore
import io.github.ugaikit.gemini4kt.filesearch.Operation
import io.github.ugaikit.gemini4kt.getApiKey
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Validates the FileSearchSample by mocking FileSearch and Gemini interactions.
 */
class FileSearchSampleTest {
    /**
     * Confirms upload polling, content generation, and cleanup are invoked and produce expected output.
     */
    @Test
    fun runUploadsPollsAndGeneratesContent() =
        runTest {
            mockkStatic("io.github.ugaikit.gemini4kt.PlatformKt")
            every { getApiKey() } returns "test-api-key"
            try {
                val store = FileSearchStore(name = "stores/1")
                val initialUpload = Operation(name = "operations/1", done = false)
                val finishedUpload = initialUpload.copy(done = true)

                val fileSearch = mockk<FileSearch>(relaxed = true)
                coEvery { fileSearch.createFileSearchStore(any()) } returns store
                coEvery {
                    fileSearch.uploadToFileSearchStore(any(), any(), any(), any())
                } returns initialUpload
                coEvery { fileSearch.getFileSearchStoreOperation(initialUpload.name!!) } returns finishedUpload

                val gemini = mockk<io.github.ugaikit.gemini4kt.Gemini>(relaxed = true)
                coEvery {
                    gemini.generateContent(any(), model = any())
                } returns
                    GenerateContentResponse(
                        candidates =
                            listOf(
                                Candidate(
                                    content =
                                        Content(
                                            parts = arrayOf(Part(text = "The fox says ring-ding-ding.")),
                                        ),
                                ),
                            ),
                    )

                val output =
                    captureStdout {
                        FileSearchSample.run(
                            filePath = "dummy.txt",
                            geminiInstance = gemini,
                            fileSearchInstance = fileSearch,
                        )
                    }

                coVerify { fileSearch.createFileSearchStore(any()) }
                coVerify { fileSearch.uploadToFileSearchStore(store.name!!, any(), any(), any()) }
                coVerify { fileSearch.getFileSearchStoreOperation(initialUpload.name!!) }
                coVerify { fileSearch.deleteFileSearchStore(store.name!!, force = true) }
                coVerify {
                    gemini.generateContent(any(), model = "gemma-4-31b-it")
                }
                assertTrue(output.contains("Upload complete."))
                assertTrue(output.contains("The fox says"))
            } finally {
                unmockkStatic("io.github.ugaikit.gemini4kt.PlatformKt")
            }
        }
}
