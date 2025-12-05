package io.github.ugaikit.gemini4kt

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class GeminiExceptionTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `test GeminiException initialization`() {
        val error =
            GeminiError(
                code = 400,
                message = "Bad Request",
                status = "INVALID_ARGUMENT",
            )
        val exception = GeminiException(error)

        assertEquals("Bad Request", exception.message)
        assertEquals(error, exception.error)
    }

    @Test
    fun `test GeminiErrorResponse deserialization`() {
        val jsonString =
            """
            {
              "error": {
                "code": 400,
                "message": "API key not valid. Please pass a valid API key.",
                "status": "INVALID_ARGUMENT",
                "details": [
                  {
                    "@type": "type.googleapis.com/google.rpc.ErrorInfo",
                    "reason": "API_KEY_INVALID",
                    "domain": "googleapis.com",
                    "metadata": {
                      "service": "generativelanguage.googleapis.com"
                    }
                  }
                ]
              }
            }
            """.trimIndent()

        val response = json.decodeFromString<GeminiErrorResponse>(jsonString)

        assertEquals(400, response.error.code)
        assertEquals("API key not valid. Please pass a valid API key.", response.error.message)
        assertEquals("INVALID_ARGUMENT", response.error.status)
        assertNotNull(response.error.details)
        assertEquals(1, response.error.details?.size)

        val detail = response.error.details!![0]
        assertEquals("type.googleapis.com/google.rpc.ErrorInfo", detail.type)
        assertEquals("API_KEY_INVALID", detail.reason)
        assertEquals("googleapis.com", detail.domain)
        assertEquals("generativelanguage.googleapis.com", detail.metadata?.get("service"))
    }

    @Test
    fun `test GeminiError serialization`() {
        val error =
            GeminiError(
                code = 404,
                message = "Not Found",
                status = "NOT_FOUND",
                details =
                    listOf(
                        GeminiErrorDetail(
                            type = "type.googleapis.com/google.rpc.ResourceInfo",
                            reason = "RESOURCE_NOT_FOUND",
                        ),
                    ),
            )

        val jsonString = json.encodeToString(error)
        val decodedError = json.decodeFromString<GeminiError>(jsonString)

        assertEquals(error, decodedError)
    }

    @Test
    fun `test GeminiErrorDetail with multiple fields`() {
        val detail =
            GeminiErrorDetail(
                type = "type.googleapis.com/google.rpc.QuotaFailure",
                violations =
                    listOf(
                        GeminiErrorViolation(
                            description = "Quota exceeded",
                            quotaMetric = "requests",
                            quotaDimensions = mapOf("region" to "us-central1"),
                        ),
                    ),
                retryDelay = "10s",
                links =
                    listOf(
                        GeminiErrorLink(
                            description = "Google Cloud Console",
                            url = "https://console.cloud.google.com",
                        ),
                    ),
            )

        val jsonString = json.encodeToString(detail)
        val decodedDetail = json.decodeFromString<GeminiErrorDetail>(jsonString)

        assertEquals(detail, decodedDetail)
        assertEquals("requests", decodedDetail.violations?.get(0)?.quotaMetric)
        assertEquals(
            "us-central1",
            decodedDetail.violations
                ?.get(0)
                ?.quotaDimensions
                ?.get("region"),
        )
        assertEquals("https://console.cloud.google.com", decodedDetail.links?.get(0)?.url)
    }

    @Test
    fun `test GeminiErrorDetail empty`() {
        val detail = GeminiErrorDetail()
        assertNull(detail.type)
        assertNull(detail.reason)
        assertNull(detail.domain)
        assertNull(detail.metadata)
        assertNull(detail.retryDelay)
        assertNull(detail.links)
        assertNull(detail.violations)

        val jsonString = json.encodeToString(detail)
        // Should be valid JSON, likely just "{}" or similar depending on settings
        val decodedDetail = json.decodeFromString<GeminiErrorDetail>(jsonString)
        assertEquals(detail, decodedDetail)
    }
}
