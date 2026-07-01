package io.github.ugaikit.gemini4kt

import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readLine

/**
 * Consumes a Server-Sent Events stream and emits each event payload through [onPayload].
 *
 * The parser accepts `data:` lines with or without a trailing space, aggregates multi-line
 * payloads, and flushes the payload when a blank line terminates the event.
 */
internal suspend fun ByteReadChannel.consumeServerSentEvents(onPayload: suspend (String) -> Unit) {
    val currentEvent = StringBuilder()

    suspend fun flushEvent() {
        if (currentEvent.isNotEmpty()) {
            onPayload(currentEvent.toString())
            currentEvent.setLength(0)
        }
    }

    while (!isClosedForRead) {
        val line = readLine() ?: break
        when {
            line.isEmpty() -> flushEvent()
            line.startsWith("data:") -> {
                val payload = line.removePrefix("data:").removePrefix(" ")
                if (currentEvent.isNotEmpty()) {
                    currentEvent.append('\n')
                }
                currentEvent.append(payload)
            }
            line.startsWith(":") -> Unit
            else -> Unit
        }
    }

    flushEvent()
}
