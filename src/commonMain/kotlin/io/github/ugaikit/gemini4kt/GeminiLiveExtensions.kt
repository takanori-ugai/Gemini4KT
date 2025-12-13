package io.github.ugaikit.gemini4kt

import io.github.ugaikit.gemini4kt.live.GeminiLive
import io.github.ugaikit.gemini4kt.live.LiveConnectConfig

/**
 * Creates a client for the Live API.
 *
 * @param model The model to be used for the live session.
 * @param config Optional configuration for the live session.
 * @return A [GeminiLive] client instance.
 */
fun Gemini.getLiveClient(
    model: String,
    config: LiveConnectConfig? = null,
): GeminiLive = GeminiLive(apiKey, model, config, json)
