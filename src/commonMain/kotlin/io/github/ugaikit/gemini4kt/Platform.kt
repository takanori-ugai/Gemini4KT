package io.github.ugaikit.gemini4kt

/**
 * Expects a platform-specific implementation to retrieve the API key.
 *
 * @return The API key as a [String].
 */
internal expect fun getApiKey(): String

/**
 * Expects a platform-specific implementation to retrieve an image as a base64 encoded string.
 *
 * @return The base64 encoded image as a [String].
 */
internal expect fun getImage(): String
