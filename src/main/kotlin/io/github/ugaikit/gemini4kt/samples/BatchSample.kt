package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.Content
import io.github.ugaikit.gemini4kt.Gemini
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
        val batchJob = batchClient.createBatch("gemini-1.5-flash", createBatchRequest)
        println("Batch Job Created: ${batchJob.name}")
        println("State: ${batchJob.metadata?.state}")

        // Check status
        println("Checking status...")
        val status = batchClient.getBatch(batchJob.name)
        println("Current State: ${status.metadata?.state}")

        // List batches
        println("Listing recent batches...")
        val batchesList = batchClient.listBatches(pageSize = 5)
        batchesList.batches.forEach {
            println("- ${it.name} (${it.metadata?.state})")
        }
    } catch (e: Exception) {
        println("Error: ${e.message}")
        e.printStackTrace()
    }
}
