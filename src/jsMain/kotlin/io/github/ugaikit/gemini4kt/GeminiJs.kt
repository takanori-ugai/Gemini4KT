package io.github.ugaikit.gemini4kt

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Handles generate text internal.
 *
 * @param prompt The prompt.
 * @param model The model.
 */
private suspend fun Gemini.generateTextInternal(
    prompt: String,
    model: String,
): String {
    /**
     * Holds the request.
     */
    val request =
        GenerateContentRequest(
            contents =
                arrayOf(
                    Content(
                        parts = arrayOf(Part(text = prompt)),
                    ),
                ),
        )

    /**
     * Holds the response.
     */
    val response = generateContent(request, model)
    return response.candidates
        .firstOrNull()
        ?.content
        ?.parts
        ?.firstOrNull()
        ?.text
        .orEmpty()
}

/**
 * Handles generate text.
 *
 * @param apiKey The api key.
 * @param prompt The prompt.
 * @param model The model.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
suspend fun generateText(
    apiKey: String,
    prompt: String,
    model: String = "gemini-pro",
): String {
    val client = Gemini(apiKey)
    return try {
        client.generateTextInternal(prompt, model)
    } finally {
        client.close()
    }
}

/**
 * Represents the gemini js client.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
class GeminiJsClient(
    apiKey: String,
) {
    /**
     * Holds the client.
     */
    private val client = Gemini(apiKey)

    /**
     * Handles generate text.
     *
     * @param prompt The prompt.
     * @param model The model.
     */
    suspend fun generateText(
        prompt: String,
        model: String = "gemini-pro",
    ): String = client.generateTextInternal(prompt, model)

    /**
     * Releases the underlying Gemini client.
     */
    fun close() {
        client.close()
    }
}

/**
 * Handles run sample1.
 *
 * @param apiKey The api key.
 * @param prompt The prompt.
 * @param model The model.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
suspend fun runSample1(
    apiKey: String,
    prompt: String = "Write a story about a magic backpack.",
    model: String = "gemma-4-31b-it",
): String {
    /**
     * Holds the client.
     */
    val client = Gemini(apiKey)

    /**
     * Holds the request.
     */
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

    /**
     * Holds the response.
     */
    return try {
        val response = client.generateContent(request, model = model)
        response.candidates
            .firstOrNull()
            ?.content
            ?.parts
            ?.firstOrNull()
            ?.text
            .orEmpty()
    } finally {
        client.close()
    }
}
