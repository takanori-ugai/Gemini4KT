package io.github.ugaikit.gemini4kt.filesearch

import kotlinx.serialization.Serializable

/**
 * Config for telling the service how to chunk the data.
 *
 * @property chunkSize The size of the chunks.
 * @property chunkOverlap The overlap between chunks.
 * @property whiteSpaceConfig Config for white space chunking.
 */
@Serializable
data class ChunkingConfig(
    val chunkSize: Int? = null,
    val chunkOverlap: Int? = null,
    val whiteSpaceConfig: WhiteSpaceConfig? = null,
)

/**
 * Config for white space chunking.
 *
 * @property maxTokensPerChunk The maximum number of tokens per chunk.
 * @property maxOverlapTokens The maximum number of overlap tokens.
 */
@Serializable
data class WhiteSpaceConfig(
    val maxTokensPerChunk: Int? = null,
    val maxOverlapTokens: Int? = null,
)
