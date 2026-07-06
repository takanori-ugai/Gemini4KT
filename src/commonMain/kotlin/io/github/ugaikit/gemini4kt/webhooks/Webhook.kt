package io.github.ugaikit.gemini4kt.webhooks

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Represents a webhook resource.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class Webhook(
    val id: String,
    @SerialName("display_name") val displayName: String? = null,
    val description: String? = null,
    val url: String? = null,
    val events: List<String>? = null,
    val secret: String? = null,
    val enabled: Boolean? = null,
    val created: String? = null,
    val updated: String? = null,
)

/**
 * Request payload for creating a webhook.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class CreateWebhookRequest(
    val id: String,
    @SerialName("display_name") val displayName: String? = null,
    val description: String? = null,
    val url: String? = null,
    val events: List<String>? = null,
    val secret: String? = null,
    val enabled: Boolean? = null,
)

/**
 * Response payload for listing webhooks.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class ListWebhooksResponse(
    val webhooks: List<Webhook>,
    @SerialName("nextPageToken") val nextPageToken: String? = null,
)
