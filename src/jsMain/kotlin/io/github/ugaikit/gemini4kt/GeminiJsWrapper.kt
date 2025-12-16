package io.github.ugaikit.gemini4kt

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise
import kotlin.js.Promise

@OptIn(DelicateCoroutinesApi::class)
@JsExport
@JsName("GeminiClient") // How it will appear in JS
class GeminiJsWrapper {
    private val client = Gemini()

    // Wrapper function: Returns Promise<String> instead of suspend String
    // This IS supported by @JsExport
    fun generateContentAsync(prompt: String): Promise<String> =
        GlobalScope.promise {
            client.generateContent(prompt)
        }
}
