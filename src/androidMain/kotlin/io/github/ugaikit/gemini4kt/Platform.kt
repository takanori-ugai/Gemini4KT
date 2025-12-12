package io.github.ugaikit.gemini4kt

internal actual fun getApiKey(): String = System.getenv("GEMINI_API_KEY") ?: ""
