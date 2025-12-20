package io.github.ugaikit.gemini4kt.batch

import io.github.ugaikit.gemini4kt.GenerateContentRequest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement

/**
 * A DSL for building [CreateBatchRequest] objects.
 */
fun createBatchRequest(block: CreateBatchRequestBuilder.() -> Unit): CreateBatchRequest = CreateBatchRequestBuilder().apply(block).build()

/**
 * Represents the create batch request builder.
 */
class CreateBatchRequestBuilder {
    /**
     * Holds the batch.
     */
    private var batch: BatchConfig? = null

    /**
     * Handles batch.
     *
     * @param block The block.
     */
    fun batch(block: BatchConfigBuilder.() -> Unit) {
        this.batch = BatchConfigBuilder().apply(block).build()
    }

    /**
     * Handles build.
     */
    fun build(): CreateBatchRequest =
        CreateBatchRequest(
            batch = batch ?: error("Batch config must be provided."),
        )
}

/**
 * A DSL for building [BatchConfig] objects.
 */
fun batchConfig(block: BatchConfigBuilder.() -> Unit): BatchConfig = BatchConfigBuilder().apply(block).build()

/**
 * Represents the batch config builder.
 */
class BatchConfigBuilder {
    /**
     * Holds the display name.
     */
    var displayName: String? = null

    /**
     * Holds the input config.
     */
    private var inputConfig: BatchInputConfig? = null

    /**
     * Handles input config.
     *
     * @param block The block.
     */
    fun inputConfig(block: BatchInputConfigBuilder.() -> Unit) {
        this.inputConfig = BatchInputConfigBuilder().apply(block).build()
    }

    /**
     * Handles build.
     */
    fun build(): BatchConfig =
        BatchConfig(
            displayName = displayName,
            inputConfig = inputConfig ?: error("Input config must be provided."),
        )
}

/**
 * A DSL for building [BatchInputConfig] objects.
 */
fun batchInputConfig(block: BatchInputConfigBuilder.() -> Unit): BatchInputConfig = BatchInputConfigBuilder().apply(block).build()

/**
 * Represents the batch input config builder.
 */
class BatchInputConfigBuilder {
    /**
     * Holds the requests.
     */
    private var requests: BatchRequestInput? = null

    /**
     * Handles requests.
     *
     * @param block The block.
     */
    fun requests(block: BatchRequestInputBuilder.() -> Unit) {
        this.requests = BatchRequestInputBuilder().apply(block).build()
    }

    /**
     * Handles build.
     */
    fun build(): BatchInputConfig =
        BatchInputConfig(
            requests = requests ?: error("Requests must be provided."),
        )
}

/**
 * A DSL for building [BatchRequestInput] objects.
 */
fun batchRequestInput(block: BatchRequestInputBuilder.() -> Unit): BatchRequestInput = BatchRequestInputBuilder().apply(block).build()

/**
 * Represents the batch request input builder.
 */
class BatchRequestInputBuilder {
    /**
     * Holds the requests.
     */
    private val requests = mutableListOf<BatchItemRequest>()

    /**
     * Handles request.
     *
     * @param block The block.
     */
    fun request(block: BatchItemRequestBuilder.() -> Unit) {
        requests.add(BatchItemRequestBuilder().apply(block).build())
    }

    /**
     * Handles build.
     */
    fun build(): BatchRequestInput = BatchRequestInput(requests)
}

/**
 * A DSL for building [BatchItemRequest] objects.
 */
fun batchItemRequest(block: BatchItemRequestBuilder.() -> Unit): BatchItemRequest = BatchItemRequestBuilder().apply(block).build()

/**
 * Represents the batch item request builder.
 */
class BatchItemRequestBuilder {
    /**
     * Holds the request.
     */
    private var request: JsonElement? = null

    /**
     * Holds the metadata.
     */
    private var metadata: ResponseMetadata? = null

    /**
     * Handles request.
     *
     * @param request The request.
     * @param json The json.
     */
    fun request(
        request: GenerateContentRequest,
        json: Json = Json,
    ) {
        this.request = json.encodeToJsonElement(request)
    }

    /**
     * Handles metadata.
     *
     * @param block The block.
     */
    fun metadata(block: ResponseMetadataBuilder.() -> Unit) {
        this.metadata = ResponseMetadataBuilder().apply(block).build()
    }

    /**
     * Handles build.
     */
    fun build(): BatchItemRequest =
        BatchItemRequest(
            request = request ?: error("Request must be provided."),
            metadata = metadata,
        )
}

/**
 * A DSL for building [ResponseMetadata] objects.
 */
fun responseMetadata(block: ResponseMetadataBuilder.() -> Unit): ResponseMetadata = ResponseMetadataBuilder().apply(block).build()

/**
 * Represents the response metadata builder.
 */
class ResponseMetadataBuilder {
    /**
     * Holds the key.
     */
    var key: String? = null

    /**
     * Handles build.
     */
    fun build(): ResponseMetadata = ResponseMetadata(key = key)
}
