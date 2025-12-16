package io.github.ugaikit.gemini4kt

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.js.Promise

@OptIn(DelicateCoroutinesApi::class)
@JsExport
@JsName("GeminiClient") // How it will appear in JS
class GeminiJsWrapper(
    apiKey: String,
) {
    private val client = Gemini(apiKey)

    private val json =
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            explicitNulls = false
        }

    // Wrapper function: Returns Promise<String> instead of suspend String
    // This IS supported by @JsExport
    fun generateContentAsync(prompt: String): Promise<String> =
        GlobalScope.promise {
            val request =
                GenerateContentRequest(
                    contents =
                        listOf(
                            Content(parts = listOf(Part(text = prompt))),
                        ),
                )
            val response = client.generateContent(request)
            response.candidates
                .firstOrNull()
                ?.content
                ?.parts
                ?.firstOrNull()
                ?.text ?: ""
        }

    @JsName("generateContent")
    fun generateContent(
        input: dynamic,
        model: String = "gemma-3-12b-it",
    ): Promise<dynamic> =
        GlobalScope.promise {
            when (input) {
                is String -> {
                    val request =
                        GenerateContentRequest(
                            contents =
                                listOf(
                                    Content(parts = listOf(Part(text = input))),
                                ),
                        )
                    val response = client.generateContent(request, model)
                    response.candidates
                        .firstOrNull()
                        ?.content
                        ?.parts
                        ?.firstOrNull()
                        ?.text ?: ""
                }
                is GenerateContentRequest -> {
                    val response = client.generateContent(input, model)
                    val responseJsonString = json.encodeToString(response)
                    JSON.parse(responseJsonString)
                }
                else -> {
                    val requestJsonString = JSON.stringify(input)
                    val generateContentRequest = json.decodeFromString<GenerateContentRequest>(requestJsonString)
                    val response = client.generateContent(generateContentRequest, model)
                    val responseJsonString = json.encodeToString(response)
                    JSON.parse(responseJsonString)
                }
            }
        }
}
