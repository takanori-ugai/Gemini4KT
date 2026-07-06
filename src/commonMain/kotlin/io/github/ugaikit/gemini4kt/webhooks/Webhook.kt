package io.github.ugaikit.gemini4kt.webhooks

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Represents a webhook resource.
 *
 * @property id The unique identifier of the webhook.
 * @property name The name of the webhook resource.
 * @property uri The destination URI where webhook payloads will be sent.
 * @property subscribedEvents The list of event types this webhook is subscribed to.
 * @property newSigningSecret The new signing secret. NOTE: This contains sensitive credential
 * material and must not be logged to prevent accidental exposure.
 * @property signingSecrets The list of active signing secrets for the webhook.
 * @property state The state of the webhook.
 * @property createTime The creation time of the webhook.
 * @property updateTime The update time of the webhook.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class Webhook(
    val id: String,
    val name: String? = null,
    val uri: String? = null,
    @SerialName("subscribed_events") val subscribedEvents: List<String>? = null,
    @SerialName("new_signing_secret") val newSigningSecret: String? = null,
    @SerialName("signing_secrets") val signingSecrets: List<SigningSecret>? = null,
    val state: String? = null,
    @SerialName("create_time") val createTime: String? = null,
    @SerialName("update_time") val updateTime: String? = null,
)

/**
 * Represents a signing secret attached to a webhook.
 *
 * @property truncatedSecret The truncated signing secret. NOTE: While truncated, this relates to
 * sensitive credential material and must not be logged to prevent security leaks.
 * @property expireTime The expiration time of the signing secret.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class SigningSecret(
    @SerialName("truncated_secret") val truncatedSecret: String? = null,
    @SerialName("expire_time") val expireTime: String? = null,
)

/**
 * Request payload for creating a webhook.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class CreateWebhookRequest(
    val name: String? = null,
    val uri: String? = null,
    @SerialName("subscribed_events") val subscribedEvents: List<String>? = null,
)

/**
 * Response payload for listing webhooks.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class ListWebhooksResponse(
    val webhooks: List<Webhook>,
    @SerialName("next_page_token") val nextPageToken: String? = null,
)
