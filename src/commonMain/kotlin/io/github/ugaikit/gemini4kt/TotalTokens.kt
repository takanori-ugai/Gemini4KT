package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

/**
 * Represents the total count of tokens, typically used to summarize the amount
 * of processed or generated content in terms of tokens.
 *
 * @property totalTokens An integer value indicating the total number of tokens.
 * This can be used for tracking, limiting, or assessing the size of content
 * based on its token count.
 */
@Serializable
data class TotalTokens(
    val totalTokens: Int,
)
