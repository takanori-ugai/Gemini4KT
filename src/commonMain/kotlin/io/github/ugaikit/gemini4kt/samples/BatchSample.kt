package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.Content
import io.github.ugaikit.gemini4kt.GenerateContentRequest
import io.github.ugaikit.gemini4kt.Part
import io.github.ugaikit.gemini4kt.batch.Batch
import io.github.ugaikit.gemini4kt.batch.createBatchRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Represents the batch sample.
 */
object BatchSample {
    /**
     * Handles run.
     *
     * @param batchClient The batch client.
     */
    suspend fun run(batchClient: Batch? = null) {
        withBatchClient(batchClient) { client ->
            // Prepare standard GenerateContentRequests
            val request1 =
                GenerateContentRequest(
                    contents = arrayOf(Content(parts = arrayOf(Part(text = "Tell me a haiku about coding.")))),
                )

            val request2 =
                GenerateContentRequest(
                    contents = arrayOf(Content(parts = arrayOf(Part(text = "Tell me a haiku about coffee.")))),
                )

            // Create CreateBatchRequest using the DSL
            val createBatchRequest =
                createBatchRequest {
                    batch {
                        displayName = "My First Batch Job"
                        inputConfig {
                            requests {
                                request {
                                    request(request1)
                                    metadata {
                                        key = "haiku-coding"
                                    }
                                }
                                request {
                                    request(request2)
                                    metadata {
                                        key = "haiku-coffee"
                                    }
                                }
                            }
                        }
                    }
                }

            println("Creating batch job...")
            val createdBatchJob = client.createBatch("gemini-2.0-flash", createBatchRequest)
            println("Batch Job Created: ${createdBatchJob.name}")
            println("Initial State: ${createdBatchJob.metadata?.state}")

            var batchJob = createdBatchJob
            var state = batchJob.metadata?.state

            println("Waiting for job completion...")
            val completed =
                withTimeoutOrNull(60_000) {
                    while (
                        state != "BATCH_STATE_SUCCEEDED" &&
                        state != "BATCH_STATE_FAILED" &&
                        state != "BATCH_STATE_CANCELLED"
                    ) {
                        delay(10000)
                        batchJob = client.getBatch(batchJob.name)
                        state = batchJob.metadata?.state
                        println("Current State: $state")
                    }
                    true
                }
            if (completed == null) {
                error("Timed out waiting for batch job completion.")
            }

            if (state == "BATCH_STATE_SUCCEEDED") {
                println("Job succeeded!")
                batchJob.response?.inlinedResponses?.inlinedResponses?.forEachIndexed { index, response ->
                    println("\nResponse $index:")
                    println("Metadata Key: ${response.metadata?.key}")
                    println(response.response)
                }
            } else {
                println("Job failed or cancelled. Error: ${batchJob.error}")
            }

            // List batches
            println("\nListing recent batches...")
            val batchesList = client.listBatches(pageSize = 5)
            batchesList.operations?.forEach {
                println("- ${it.name} (${it.metadata?.state})")
            }
        }
    }
}
