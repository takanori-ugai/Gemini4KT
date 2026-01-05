/**
 * Provides platform-specific implementations for the JVM platform.
 */
package io.github.ugaikit.gemini4kt

import java.io.File
import java.util.Base64
import java.util.Properties

/**
 * Retrieves the API key from a properties file for the JVM platform.
 *
 * @return The API key as a [String].
 */
internal actual fun getApiKey(): String {
    val envKey = System.getenv("GEMINI_API_KEY")
    if (!envKey.isNullOrBlank()) {
        return envKey
    }
    val stream = Gemini::class.java.getResourceAsStream("/prop.properties")
    if (stream != null) {
        return stream.use { inputStream ->
            Properties()
                .apply {
                    load(inputStream)
                }.getProperty("apiKey")
        }
    }
    throw RuntimeException("GEMINI_API_KEY environment variable not set and prop.properties not found.")
}

/**
 * Retrieves an image from resources and encodes it as a base64 string for the JVM platform.
 *
 * This function reads an image file from the resources and converts it to a base64 encoded string.
 *
 * @return The base64 encoded image as a [String] for the JVM platform.
 */
internal actual fun getImage(): String {
    /**
     * Holds the image.
     */
    val image = File(Gemini::class.java.getResource("/scones.jpg").toURI())
    return Base64.getEncoder().encodeToString(image.readBytes())
}
