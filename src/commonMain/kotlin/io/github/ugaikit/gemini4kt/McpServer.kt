package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * An MCPServer is a server that can be called by the model to perform actions.
 *
 * @property name The name of the MCPServer.
 * @property streamableHttpTransport A transport that can stream HTTP requests and responses.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class McpServer(
    val name: String? = null,
    val streamableHttpTransport: StreamableHttpTransport? = null,
)
