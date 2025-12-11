package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.Content
import io.github.ugaikit.gemini4kt.Gemini
import io.github.ugaikit.gemini4kt.GeminiException
import io.github.ugaikit.gemini4kt.GenerateContentRequest
import io.github.ugaikit.gemini4kt.Part
import io.github.ugaikit.gemini4kt.batch.Batch
import io.github.ugaikit.gemini4kt.batch.createBatchRequest
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.util.Properties

object BatchSample {
    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        val path = Gemini::class.java.getResourceAsStream("/prop.properties")
        val prop =
        Properties().also {
            it.load(path)
        }
    val apiKey = prop.getProperty("apiKey")

    // Initialize Batch client
    val batchClient = Batch(apiKey)

    // Prepare standard GenerateContentRequests
    val request1 =
        GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = "Tell me a haiku about coding.")))),
        )

    val request2 =
        GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = "Tell me a haiku about coffee.")))),
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

    try {
        println("Creating batch job...")
        val createdBatchJob = batchClient.createBatch("gemini-2.0-flash", createBatchRequest)
        println("Batch Job Created: ${createdBatchJob.name}")
        println("Initial State: ${createdBatchJob.metadata?.state}")

        var batchJob = createdBatchJob
        var state = batchJob.metadata?.state

        println("Waiting for job completion...")
        while (state != "BATCH_STATE_SUCCEEDED" && state != "BATCH_STATE_FAILED" && state != "BATCH_STATE_CANCELLED") {
            Thread.sleep(10000) // Wait for 10 seconds
            batchJob = batchClient.getBatch(batchJob.name)
            state = batchJob.metadata?.state
            println("Current State: $state")
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
        val batchesList = batchClient.listBatches(pageSize = 5)
        batchesList.operations?.forEach {
            println("- ${it.name} (${it.metadata?.state})")
        }
    } catch (e: GeminiException) {
        println("Gemini API Error: ${e.message}")
    } catch (e: IOException) {
        println("IO Error: ${e.message}")
    } catch (e: InterruptedException) {
        println("Interrupted: ${e.message}")
    }
    }
}
