package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.Content
import io.github.ugaikit.gemini4kt.EmbedContentRequest
import io.github.ugaikit.gemini4kt.Gemini
import io.github.ugaikit.gemini4kt.GeminiException
import io.github.ugaikit.gemini4kt.GenerateContentRequest
import io.github.ugaikit.gemini4kt.Part
import io.github.ugaikit.gemini4kt.batch.Batch
import io.github.ugaikit.gemini4kt.batch.createBatchRequest
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

        // Test createBatchEmbeddings
        println("\n--- Testing createBatchEmbeddings ---")
        val embeddingRequest1 =
            EmbedContentRequest(
                model = "models/text-embedding-004",
                content = Content(parts = listOf(Part(text = "Hello world"))),
            )
        val embeddingRequest2 =
            EmbedContentRequest(
                model = "models/text-embedding-004",
                content = Content(parts = listOf(Part(text = "Batch embeddings are cool"))),
            )

        val createBatchEmbeddingsRequest =
            createBatchRequest {
                batch {
                    displayName = "Batch Embeddings Job"
                    inputConfig {
                        requests {
                            request {
                                request(embeddingRequest1)
                                metadata { key = "embedding-1" }
                            }
                            request {
                                request(embeddingRequest2)
                                metadata { key = "embedding-2" }
                            }
                        }
                    }
                }
            }

        println("Creating batch embeddings job...")
        val createdEmbeddingsJob =
            batchClient.createBatchEmbeddings(
                "text-embedding-004",
                createBatchEmbeddingsRequest,
            )
        println("Batch Embeddings Job Created: ${createdEmbeddingsJob.name}")
        println("Initial State: ${createdEmbeddingsJob.metadata?.state}")

        var embeddingsJob = createdEmbeddingsJob
        var embeddingsState = embeddingsJob.metadata?.state

        println("Waiting for embeddings job completion...")
        while (embeddingsState != "BATCH_STATE_SUCCEEDED" && embeddingsState != "BATCH_STATE_FAILED" && embeddingsState != "BATCH_STATE_CANCELLED") {
            Thread.sleep(10000) // Wait for 10 seconds
            embeddingsJob = batchClient.getBatch(embeddingsJob.name)
            embeddingsState = embeddingsJob.metadata?.state
            println("Current State: $embeddingsState")
        }

        if (embeddingsState == "BATCH_STATE_SUCCEEDED") {
            println("Embeddings Job succeeded!")
            val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
            embeddingsJob.response?.inlinedResponses?.inlinedResponses?.forEachIndexed { index, response ->
                println("\nEmbedding Response $index:")
                println("Metadata Key: ${response.metadata?.key}")
                response.response?.let {
                    try {
                        val embedResponse =
                            json.decodeFromJsonElement(
                                io.github.ugaikit.gemini4kt.EmbedResponse
                                    .serializer(),
                                it,
                            )
                        println("Embedding Length: ${embedResponse.embedding.values.size}")
                        println("First 5 values: ${embedResponse.embedding.values.take(5)}")
                    } catch (e: Exception) {
                        println("Failed to parse embedding response: ${e.message}")
                        println("Raw response: $it")
                    }
                }
            }
        } else {
            println("Embeddings Job failed or cancelled. Error: ${embeddingsJob.error}")
        }
    } catch (e: GeminiException) {
        println("Gemini API Error: ${e.message}")
    } catch (e: IOException) {
        println("IO Error: ${e.message}")
    } catch (e: InterruptedException) {
        println("Interrupted: ${e.message}")
    }
}
