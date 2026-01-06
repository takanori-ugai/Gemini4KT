package io.github.ugaikit.gemini4kt.live

import io.github.oshai.kotlinlogging.KLogger
import io.ktor.websocket.Frame

internal fun decodeBinaryFrame(
    frame: Frame.Binary,
    logger: KLogger,
): String? {
    val bytes = frame.data
    logger.debug { "Received binary frame with size: ${bytes.size}" }
    return try {
        bytes.decodeToString()
    } catch (e: Exception) {
        logger.error(e) { "Failed to decode binary frame as UTF-8" }
        null
    }
}
