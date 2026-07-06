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
                displayName = "Build status",
                description = "Notify when a build finishes.",
                url = "https://example.com/webhook",
                events = listOf("interaction.completed"),
                secret = "secret",
                enabled = true,
                created = "2026-07-06T00:00:00Z",
                updated = "2026-07-06T01:00:00Z",
            )

        val encoded = json.encodeToString(webhook)
        val expected =
            """
            {
              "id": "wh_123",
              "display_name": "Build status",
              "description": "Notify when a build finishes.",
              "url": "https://example.com/webhook",
              "events": [
                "interaction.completed"
              ],
              "secret": "secret",
              "enabled": true,
              "created": "2026-07-06T00:00:00Z",
              "updated": "2026-07-06T01:00:00Z"
            }
            """.trimIndent()

        assertEquals(json.parseToJsonElement(expected), json.parseToJsonElement(encoded))
        assertEquals(webhook, json.decodeFromString<Webhook>(encoded))
    }

    @Test
    fun createWebhookRequestSerializesCorrectly() {
        val request =
            CreateWebhookRequest(
                id = "wh_123",
                displayName = "Build status",
                url = "https://example.com/webhook",
                events = listOf("interaction.completed"),
                enabled = true,
            )

        val encoded = json.encodeToString(request)
        val expected =
            """
            {
              "id": "wh_123",
              "display_name": "Build status",
              "url": "https://example.com/webhook",
              "events": [
                "interaction.completed"
              ],
              "enabled": true
            }
            """.trimIndent()

        assertEquals(json.parseToJsonElement(expected), json.parseToJsonElement(encoded))
    }

    @Test
    fun listWebhooksResponseSerializesCorrectly() {
        val response =
            ListWebhooksResponse(
                webhooks = listOf(Webhook(id = "wh_123", url = "https://example.com/webhook")),
                nextPageToken = "next-token",
            )

        val encoded = json.encodeToString(response)
        val expected =
            """
            {
              "webhooks": [
                {
                  "id": "wh_123",
                  "url": "https://example.com/webhook"
                }
              ],
              "nextPageToken": "next-token"
            }
            """.trimIndent()

        assertEquals(json.parseToJsonElement(expected), json.parseToJsonElement(encoded))
        assertEquals(response, json.decodeFromString<ListWebhooksResponse>(encoded))
    }
}
