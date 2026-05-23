package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * A transport that can stream HTTP requests and responses.
 *
 * @property url The full URL for the MCPServer endpoint.
 * @property headers Optional: Fields for authentication headers, timeouts, etc.
 * @property timeout HTTP timeout for regular operations.
 * @property sseReadTimeout Timeout for SSE read operations.
 * @property terminateOnClose Whether to close the client session when the transport closes.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class StreamableHttpTransport(
    val url: String? = null,
    val headers: Map<String, String>? = null,
    val timeout: String? = null,
    val sseReadTimeout: String? = null,
    val terminateOnClose: Boolean? = null,
)
