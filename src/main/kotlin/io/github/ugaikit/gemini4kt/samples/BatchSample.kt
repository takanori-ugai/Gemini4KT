package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.Content
import io.github.ugaikit.gemini4kt.Gemini
import io.github.ugaikit.gemini4kt.GeminiException
import io.github.ugaikit.gemini4kt.GenerateContentRequest
import io.github.ugaikit.gemini4kt.Part
import io.github.ugaikit.gemini4kt.batch.Batch
import io.github.ugaikit.gemini4kt.batch.BatchConfig
import io.github.ugaikit.gemini4kt.batch.BatchInputConfig
import io.github.ugaikit.gemini4kt.batch.BatchItemRequest
import io.github.ugaikit.gemini4kt.batch.BatchRequestInput
import io.github.ugaikit.gemini4kt.batch.CreateBatchRequest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import java.io.IOException
import java.util.Properties

fun main() {
    val path = Gemini::class.java.getResourceAsStream("/prop.properties")
    val prop =
        Properties().also {
            it.load(path)
        }
    val apiKey = prop.getProperty("apiKey")

    // Initialize Batch client
    val batchClient = Batch(apiKey)
    val json = Json { ignoreUnknownKeys = true }

    // Prepare a standard GenerateContentRequest
    val request1 =
        GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = "Tell me a haiku about coding.")))),
        )

    val request2 =
        GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = "Tell me a haiku about coffee.")))),
        )

    // Wrap in BatchItemRequest
    val batchItems =
        listOf(
            BatchItemRequest(
                request = json.encodeToJsonElement(request1),
                metadata = mapOf("key" to "haiku-coding"),
            ),
            BatchItemRequest(
                request = json.encodeToJsonElement(request2),
                metadata = mapOf("key" to "haiku-coffee"),
            ),
        )

    // Create CreateBatchRequest
    val createBatchRequest =
        CreateBatchRequest(
            batch =
                BatchConfig(
                    displayName = "My First Batch Job",
                    inputConfig =
                        BatchInputConfig(
                            requests = BatchRequestInput(requests = batchItems),
                        ),
                ),
        )

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
                println(response.response)
            }
        } else {
            println("Job failed or cancelled. Error: ${batchJob.error}")
        }

        // List batches
        println("\nListing recent batches...")
        val batchesList = batchClient.listBatches(pageSize = 5)
        batchesList.operations!!.forEach {
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
