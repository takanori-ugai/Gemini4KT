package io.github.ugaikit.gemini4kt.filesearch

import kotlinx.serialization.Serializable

/**
 * Custom metadata to be associated with the data.
 *
 * @property key The key of the metadata.
 * @property stringValue The string value of the metadata.
 */
@Serializable
data class CustomMetadata(
    val key: String,
    val stringValue: String? = null,
)
