package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.batch.Batch
import io.github.ugaikit.gemini4kt.batch.BatchInlineResponse
import io.github.ugaikit.gemini4kt.batch.BatchJob
import io.github.ugaikit.gemini4kt.batch.BatchJobMetadata
import io.github.ugaikit.gemini4kt.batch.BatchJobResponse
import io.github.ugaikit.gemini4kt.batch.InlinedResponsesWrapper
import io.github.ugaikit.gemini4kt.batch.ListBatchesResponse
import io.github.ugaikit.gemini4kt.batch.ResponseMetadata
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Covers the BatchSample happy path using a mocked Batch client.
 */
class BatchSampleTest {
    /**
     * Verifies polling to success, listing batches, and printing metadata from inline responses.
     */
    @Test
    fun runPollsUntilSuccessAndListsBatches() =
        runTest {
            val pending =
                BatchJob(
                    name = "batches/1",
                    metadata = BatchJobMetadata(state = "BATCH_STATE_RUNNING"),
                    done = false,
                )
            val succeeded =
                BatchJob(
                    name = "batches/1",
                    metadata = BatchJobMetadata(state = "BATCH_STATE_SUCCEEDED"),
                    done = true,
                    response =
                        BatchJobResponse(
                            inlinedResponses =
                                InlinedResponsesWrapper(
                                    listOf(
                                        BatchInlineResponse(
                                            metadata = ResponseMetadata(key = "haiku-coding"),
                                        ),
                                    ),
                                ),
                        ),
                )
            val listResponse = ListBatchesResponse(operations = listOf(succeeded))

            val batch = mockk<Batch>(relaxed = true)
            coEvery { batch.createBatch(any(), any()) } returns pending
            coEvery { batch.getBatch(pending.name) } returns succeeded
            coEvery { batch.listBatches(pageSize = 5) } returns listResponse

            val output =
                captureStdout {
                    BatchSample.run(batch)
                }

            coVerify(exactly = 1) { batch.createBatch("gemini-2.0-flash", any()) }
            coVerify(exactly = 1) { batch.getBatch(pending.name) }
            coVerify(exactly = 1) { batch.listBatches(pageSize = 5) }
            assertTrue(output.contains("Job succeeded"))
            assertTrue(output.contains("Metadata Key: haiku-coding"))
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
