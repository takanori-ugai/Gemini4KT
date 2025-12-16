package io.github.ugaikit.gemini4kt

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise
import kotlin.js.Promise

@OptIn(DelicateCoroutinesApi::class)
@JsExport
@JsName("GeminiClient") // How it will appear in JS
class GeminiJsWrapper(apiKey: String) {
    private val client = Gemini(apiKey)

    // Wrapper function: Returns Promise<String> instead of suspend String
    // This IS supported by @JsExport
    fun generateContentAsync(prompt: String): Promise<String> =
        GlobalScope.promise {
            val request = GenerateContentRequest(
                contents = listOf(
                    Content(parts = listOf(Part(text = prompt)))
                )
            )
            val response = client.generateContent(request)
            response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
        }
}
