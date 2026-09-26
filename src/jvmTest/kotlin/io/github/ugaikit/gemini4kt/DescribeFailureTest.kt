package io.github.ugaikit.gemini4kt

import io.ktor.client.plugins.HttpRequestTimeoutException
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals

class DescribeFailureTest {
    @Test
    fun describesNestedRequestTimeouts() {
        val timeout = mockk<HttpRequestTimeoutException>()
        every { timeout.message } returns "request exceeded deadline"

        assertEquals(
            "HTTP request timed out: request exceeded deadline",
            describeFailure(IllegalStateException("request failed", timeout)),
        )
    }

    @Test
    fun identifiesInternalGeminiApiErrors() {
        val error = GeminiException(GeminiError(500, "backend failed", "INTERNAL"))

        assertEquals(
            "Gemini API internal error (HTTP 500, INTERNAL): backend failed",
            describeFailure(IllegalStateException("request failed", error)),
        )
    }

    @Test
    fun identifiesInternalGeminiErrorsByMessage() {
        val error = GeminiException(GeminiError(500, "Internal error in service", "UNKNOWN"))

        assertEquals(
            "Gemini API internal error (HTTP 500, UNKNOWN): Internal error in service",
            describeFailure(error),
        )
    }

    @Test
    fun describesOtherGeminiAndUnexpectedErrors() {
        val apiError = GeminiException(GeminiError(429, "quota exceeded", "RESOURCE_EXHAUSTED"))

        assertEquals(
            "Gemini API error (HTTP 429, RESOURCE_EXHAUSTED): quota exceeded",
            describeFailure(apiError),
        )
        assertEquals("IllegalArgumentException: invalid request", describeFailure(IllegalArgumentException("invalid request")))
    }
}
