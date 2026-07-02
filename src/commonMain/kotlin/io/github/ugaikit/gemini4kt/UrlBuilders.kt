package io.github.ugaikit.gemini4kt

import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments

internal fun buildUrl(
    baseUrl: String,
    pathSegments: List<String> = emptyList(),
    queryParameters: Map<String, String?> = emptyMap(),
): String =
    URLBuilder(baseUrl)
        .apply {
            if (pathSegments.isNotEmpty()) {
                appendPathSegments(*pathSegments.toTypedArray())
            }
            queryParameters.forEach { (key, value) ->
                if (value != null) {
                    parameters.append(key, value)
                }
            }
        }.buildString()

internal fun buildModelUrl(
    baseUrl: String,
    model: String,
    rpcName: String,
): String = buildUrl(baseUrl, listOf("models") + normalizeModelPathSegments(model)) + ":$rpcName"

internal fun normalizeModelPathSegments(model: String): List<String> = normalizeResourcePathSegments(model, "models")

internal fun normalizeResourcePathSegments(
    resourceName: String,
    rootSegment: String? = null,
): List<String> {
    val segments =
        resourceName
            .trim('/')
            .split('/')
            .filter { it.isNotBlank() }
    return if (rootSegment != null && segments.firstOrNull() == rootSegment) {
        segments.drop(1)
    } else {
        segments
    }
}
