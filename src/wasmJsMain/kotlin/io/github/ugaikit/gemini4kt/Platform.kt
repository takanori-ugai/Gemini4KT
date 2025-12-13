package io.github.ugaikit.gemini4kt

internal actual fun getApiKey(): String {
    // Environment variables are not directly accessible in Wasm/JS in the same way.
    // For now, we can return an empty string or specific value.
    // In a real browser app, this might come from configuration or user input.
    return ""
}

internal actual fun getImage(): String = ""
