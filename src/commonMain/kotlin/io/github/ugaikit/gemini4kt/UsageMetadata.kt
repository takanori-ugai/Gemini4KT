package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

/**
 * Represents the usage metadata.
 *
 * @property promptTokenCount The prompt token count.
 * @property cachedContentTokenCount The cached content token count.
 * @property candidatesTokenCount The candidates token count.
 * @property totalTokenCount The total token count.
 * @property promptTokensDetails The prompt tokens details.
 * @property cacheTokensDetails The cache tokens details.
 * @property candidatesTokensDetails The candidates tokens details.
 * @property toolUsePromptTokenCount The tool use prompt token count.
 * @property toolUsePromptTokensDetails The tool use prompt tokens details.
 * @property thoughtsTokenCount The thoughts token count.
 */
@Serializable
data class UsageMetadata(
    val promptTokenCount: Int? = null,
    val cachedContentTokenCount: Int? = null,
    val candidatesTokenCount: Int? = null,
    val totalTokenCount: Int,
    val promptTokensDetails: List<ModalityTokenCount>? = null,
    val cacheTokensDetails: List<ModalityTokenCount>? = null,
    val candidatesTokensDetails: List<ModalityTokenCount>? = null,
    val toolUsePromptTokenCount: Int? = null,
    val toolUsePromptTokensDetails: List<ModalityTokenCount>? = null,
    val thoughtsTokenCount: Int? = null,
)

/**
 * Represents the modality token count.
 *
 * @property modality The modality.
 * @property tokenCount The token count.
 */
@Serializable
data class ModalityTokenCount(
    val modality: Modality,
    val tokenCount: Int,
)
