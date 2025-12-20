package io.github.ugaikit.gemini4kt

import java.io.File
import java.util.Base64
import java.util.Properties

/**
 * Retrieves the API key from a properties file for the JVM platform.
 *
 * @return The API key as a [String].
 */
internal actual fun getApiKey(): String =
    Gemini::class.java.getResourceAsStream("/prop.properties").use { inputStream ->
        Properties()
            .apply {
                load(inputStream)
            }.getProperty("apiKey")
    }

/**
 * Retrieves an image from resources and encodes it as a base64 string for the JVM platform.
 *
 * @return The base64 encoded image as a [String].
 */
internal actual fun getImage(): String {
    val image = File(Gemini::class.java.getResource("/scones.jpg").toURI())
    return Base64.getEncoder().encodeToString(image.readBytes())
}
