package io.github.ugaikit.gemini4kt

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

private suspend fun Gemini.generateTextInternal(
    prompt: String,
    model: String,
): String {
    val request =
        GenerateContentRequest(
            contents =
                arrayOf(
                    Content(
                        parts = arrayOf(Part(text = prompt)),
                    ),
                ),
        )
    val response = generateContent(request, model)
    return response.candidates
        .firstOrNull()
        ?.content
        ?.parts
        ?.firstOrNull()
        ?.text
        .orEmpty()
}

@OptIn(ExperimentalJsExport::class)
@JsExport
suspend fun generateText(
    apiKey: String,
    prompt: String,
    model: String = "gemini-pro",
): String = Gemini(apiKey).generateTextInternal(prompt, model)

@OptIn(ExperimentalJsExport::class)
@JsExport
class GeminiJsClient(
    apiKey: String,
) {
    private val client = Gemini(apiKey)

    suspend fun generateText(
        prompt: String,
        model: String = "gemini-pro",
    ): String = client.generateTextInternal(prompt, model)
}

@OptIn(ExperimentalJsExport::class)
@JsExport
suspend fun runSample1(
    apiKey: String,
    prompt: String = "Write a story about a magic backpack.",
    model: String = "gemini-2.5-flash-lite",
): String {
    val client = Gemini(apiKey)
    val request =
        GenerateContentRequest(
            contents = arrayOf(Content(parts = arrayOf(Part(text = prompt)))),
            safetySettings =
                arrayOf(
                    SafetySetting(
                        category = HarmCategory.HARM_CATEGORY_HARASSMENT,
                        threshold = Threshold.BLOCK_ONLY_HIGH,
                    ),
                ),
            generationConfig =
                GenerationConfig(
                    thinkingConfig = ThinkingConfig(-1),
                ),
        )
    val response = client.generateContent(request, model = model)
    return response.candidates
        .firstOrNull()
        ?.content
        ?.parts
        ?.firstOrNull()
        ?.text
        .orEmpty()
}
