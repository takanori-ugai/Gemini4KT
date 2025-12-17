package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

/**
 * Represents a request to count the number of tokens in a list of content
 * items. This is typically used to ensure that input does not exceed model or
 * system limits for processing.
 *
 * @property contents A list of [Content] objects. Each [Content] item contains
 * text or data for which the token count will be calculated. The purpose is to
 * assess the total number of tokens across all provided content items, aiding in
 * managing input size constraints.
 */
@Serializable
@JsExport
data class CountTokensRequest(
    val contents: Array<Content>,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as CountTokensRequest

        if (!contents.contentEquals(other.contents)) return false

        return true
    }

    override fun hashCode(): Int = contents.contentHashCode()
}
