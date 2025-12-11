package io.github.ugaikit.gemini4kt.batch

import io.github.ugaikit.gemini4kt.GenerateContentRequest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement

/**
 * A DSL for building [CreateBatchRequest] objects.
 */
fun createBatchRequest(block: CreateBatchRequestBuilder.() -> Unit): CreateBatchRequest = CreateBatchRequestBuilder().apply(block).build()

class CreateBatchRequestBuilder {
    private var batch: BatchConfig? = null

    fun batch(block: BatchConfigBuilder.() -> Unit) {
        this.batch = BatchConfigBuilder().apply(block).build()
    }

    fun build(): CreateBatchRequest =
        CreateBatchRequest(
            batch = batch ?: throw IllegalStateException("Batch config must be provided."),
        )
}

/**
 * A DSL for building [BatchConfig] objects.
 */
fun batchConfig(block: BatchConfigBuilder.() -> Unit): BatchConfig = BatchConfigBuilder().apply(block).build()

class BatchConfigBuilder {
    var displayName: String? = null
    private var inputConfig: BatchInputConfig? = null

    fun inputConfig(block: BatchInputConfigBuilder.() -> Unit) {
        this.inputConfig = BatchInputConfigBuilder().apply(block).build()
    }

    fun build(): BatchConfig =
        BatchConfig(
            displayName = displayName,
            inputConfig = inputConfig ?: throw IllegalStateException("Input config must be provided."),
        )
}

/**
 * A DSL for building [BatchInputConfig] objects.
 */
fun batchInputConfig(block: BatchInputConfigBuilder.() -> Unit): BatchInputConfig = BatchInputConfigBuilder().apply(block).build()

class BatchInputConfigBuilder {
    private var requests: BatchRequestInput? = null

    fun requests(block: BatchRequestInputBuilder.() -> Unit) {
        this.requests = BatchRequestInputBuilder().apply(block).build()
    }

    fun build(): BatchInputConfig =
        BatchInputConfig(
            requests = requests ?: throw IllegalStateException("Requests must be provided."),
        )
}

/**
 * A DSL for building [BatchRequestInput] objects.
 */
fun batchRequestInput(block: BatchRequestInputBuilder.() -> Unit): BatchRequestInput = BatchRequestInputBuilder().apply(block).build()

class BatchRequestInputBuilder {
    private val requests = mutableListOf<BatchItemRequest>()

    fun request(block: BatchItemRequestBuilder.() -> Unit) {
        requests.add(BatchItemRequestBuilder().apply(block).build())
    }

    fun build(): BatchRequestInput = BatchRequestInput(requests)
}

/**
 * A DSL for building [BatchItemRequest] objects.
 */
fun batchItemRequest(block: BatchItemRequestBuilder.() -> Unit): BatchItemRequest = BatchItemRequestBuilder().apply(block).build()

class BatchItemRequestBuilder {
    private var request: JsonElement? = null
    private var metadata: ResponseMetadata? = null

    fun request(
        request: GenerateContentRequest,
        json: Json = Json,
    ) {
        this.request = json.encodeToJsonElement(request)
    }

    fun metadata(block: ResponseMetadataBuilder.() -> Unit) {
        this.metadata = ResponseMetadataBuilder().apply(block).build()
    }

    fun build(): BatchItemRequest =
        BatchItemRequest(
            request = request ?: throw IllegalStateException("Request must be provided."),
            metadata = metadata,
        )
}

/**
 * A DSL for building [ResponseMetadata] objects.
 */
fun responseMetadata(block: ResponseMetadataBuilder.() -> Unit): ResponseMetadata = ResponseMetadataBuilder().apply(block).build()

class ResponseMetadataBuilder {
    var key: String? = null

    fun build(): ResponseMetadata = ResponseMetadata(key = key)
}
