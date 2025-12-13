package io.github.ugaikit.gemini4kt

import java.io.File
import java.util.Base64
import java.util.Properties

internal actual fun getApiKey(): String =
    Gemini::class.java.getResourceAsStream("/prop.properties").use { inputStream ->
        Properties()
            .apply {
                load(inputStream)
            }.getProperty("apiKey")
    }

internal actual fun getImage(): String {
    val image = File(Gemini::class.java.getResource("/scones.jpg").toURI())
    return Base64.getEncoder().encodeToString(image.readBytes())
}
