package io.github.ugaikit.gemini4kt

/**
 * Returns the first text part of the first candidate, or an empty string if missing.
 */
internal fun GenerateContentResponse.firstTextPartOrEmpty(): String =
    candidates
        .firstOrNull()
        ?.content
        ?.parts
        ?.firstOrNull()
        ?.text
        .orEmpty()
