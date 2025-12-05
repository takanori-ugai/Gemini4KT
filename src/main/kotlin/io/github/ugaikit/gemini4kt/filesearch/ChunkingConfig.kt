package io.github.ugaikit.gemini4kt.filesearch

import kotlinx.serialization.Serializable

/**
 * Config for telling the service how to chunk the data.
 *
 * @property chunkSize The size of the chunks.
 * @property chunkOverlap The overlap between chunks.
 */
@Serializable
data class ChunkingConfig(
    val chunkSize: Int? = null,
    val chunkOverlap: Int? = null,
)
