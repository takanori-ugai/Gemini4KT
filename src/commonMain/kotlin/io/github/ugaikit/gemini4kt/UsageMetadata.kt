package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

@Serializable
data class UsageMetadata(
    val promptTokenCount: Int? = null,
    val candidatesTokenCount: Int? = null,
    val totalTokenCount: Int,
    val promptTokensDetails: List<ModalityTokenCount>? = null,
    val toolUsePromptTokenCount: Int? = null,
    val toolUsePromptTokensDetails: List<ModalityTokenCount>? = null,
    val thoughtsTokenCount: Int? = null,
)

@Serializable
data class ModalityTokenCount(
    val modality: Modality,
    val tokenCount: Int,
)
