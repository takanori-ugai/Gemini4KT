package io.github.ugaikit.gemini4kt

internal actual fun getApiKey(): String {
    // Environment variables are not directly accessible in JS in the same way.
    // In a Node.js environment, we could use process.env, but for now returning empty string
    // to match WasmJs implementation or simple default.
    return ""
}

internal actual fun getImage(): String {
    return ""
}
