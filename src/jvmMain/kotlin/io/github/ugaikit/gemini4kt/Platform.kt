package io.github.ugaikit.gemini4kt

import java.util.Properties

internal actual fun getApiKey(): String =
    Gemini::class.java.getResourceAsStream("/prop.properties").use { inputStream ->
        Properties()
            .apply {
                load(inputStream)
            }.getProperty("apiKey")
    }
