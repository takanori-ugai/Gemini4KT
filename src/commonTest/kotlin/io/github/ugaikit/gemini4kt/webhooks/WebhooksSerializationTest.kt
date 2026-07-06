package io.github.ugaikit.gemini4kt.webhooks

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class WebhooksSerializationTest {
    private val json =
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = false
        }

    @Test
    fun webhookSerializesCorrectly() {
        val webhook =
            Webhook(
                id = "wh_123",
                name = "Build status",
                uri = "https://example.com/webhook",
                subscribedEvents = listOf("interaction.completed"),
                newSigningSecret = "secret",
                signingSecrets = listOf(SigningSecret(truncatedSecret = "abcd", expireTime = "2026-07-06T02:00:00Z")),
                state = "enabled",
                createTime = "2026-07-06T00:00:00Z",
                updateTime = "2026-07-06T01:00:00Z",
            )

        val encoded = json.encodeToString(webhook)
        val expected =
            """
            {
              "id": "wh_123",
              "name": "Build status",
              "uri": "https://example.com/webhook",
              "subscribed_events": [
                "interaction.completed"
              ],
              "new_signing_secret": "secret",
              "signing_secrets": [
                {
                  "truncated_secret": "abcd",
                  "expire_time": "2026-07-06T02:00:00Z"
                }
              ],
              "state": "enabled",
              "create_time": "2026-07-06T00:00:00Z",
              "update_time": "2026-07-06T01:00:00Z"
            }
            """.trimIndent()

        assertEquals(json.parseToJsonElement(expected), json.parseToJsonElement(encoded))
        assertEquals(webhook, json.decodeFromString<Webhook>(encoded))
    }

    @Test
    fun createWebhookRequestSerializesCorrectly() {
        val request =
            CreateWebhookRequest(
                name = "Build status",
                uri = "https://example.com/webhook",
                subscribedEvents = listOf("interaction.completed"),
            )

        val encoded = json.encodeToString(request)
        val expected =
            """
            {
              "name": "Build status",
              "uri": "https://example.com/webhook",
              "subscribed_events": [
                "interaction.completed"
              ]
            }
            """.trimIndent()

        assertEquals(json.parseToJsonElement(expected), json.parseToJsonElement(encoded))
    }

    @Test
    fun listWebhooksResponseSerializesCorrectly() {
        val response =
            ListWebhooksResponse(
                webhooks = listOf(Webhook(id = "wh_123", uri = "https://example.com/webhook")),
                nextPageToken = "next-token",
            )

        val encoded = json.encodeToString(response)
        val expected =
            """
            {
              "webhooks": [
                {
                  "id": "wh_123",
                  "uri": "https://example.com/webhook"
                }
              ],
              "next_page_token": "next-token"
            }
            """.trimIndent()

        assertEquals(json.parseToJsonElement(expected), json.parseToJsonElement(encoded))
        assertEquals(response, json.decodeFromString<ListWebhooksResponse>(encoded))
    }
}
