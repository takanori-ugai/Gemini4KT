package io.github.ugaikit.gemini4kt.live

import io.github.oshai.kotlinlogging.KLogger
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.serialization.json.Json

/**
 * Shared helper to decode incoming text frames, complete the handshake when appropriate, and
 * forward decoded messages to the provided channel.
 */
internal suspend inline fun <reified T> processHandshakeMessage(
    text: String,
    handshakeCompleted: CompletableDeferred<Unit>,
    incomingMessages: Channel<T>,
    json: Json,
    logger: KLogger,
    crossinline isSetupComplete: (T) -> Boolean,
) {
    try {
        val message = json.decodeFromString<T>(text)

        // Check for handshake completion on the first relevant message
        if (!handshakeCompleted.isCompleted) {
            if (isSetupComplete(message)) {
                handshakeCompleted.complete(Unit)
            } else {
                logger.warn { "Received message before SetupComplete: $message" }
            }
        }

        incomingMessages.send(message)
    } catch (e: Exception) {
        logger.error(e) { "Failed to parse message" }
        if (!handshakeCompleted.isCompleted) {
            handshakeCompleted.completeExceptionally(e)
            return
        }
        throw e
    }
}
