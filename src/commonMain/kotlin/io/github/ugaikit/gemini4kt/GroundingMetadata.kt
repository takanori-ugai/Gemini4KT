package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

/**
 * Metadata returned to the client when grounding is enabled.
 *
 * @property searchEntryPoint Optional. Google search entry for the following-up web searches.
 * @property webSearchQueries Optional. Web search queries for the following-up web searches.
 * @property groundingChunks Optional. List of supporting references retrieved from specified grounding sources.
 * @property groundingSupports Optional. List of grounding support.
 */
@Serializable
data class GroundingMetadata(
    val searchEntryPoint: SearchEntryPoint? = null,
    val webSearchQueries: List<String> = emptyList(),
    val groundingChunks: List<GroundingChunk> = emptyList(),
    val groundingSupports: List<GroundingSupport> = emptyList(),
)

/**
 * Google search entry point.
 *
 * @property renderedContent Optional. Web content snippet that can be embedded in a web page or an app
 * webview.
 */
@Serializable
data class SearchEntryPoint(
    val renderedContent: String? = null,
)

/**
 * A chunk of content from a grounding source.
 *
 * @property web Optional. Grounding chunk from the web.
 */
@Serializable
data class GroundingChunk(
    val web: Web? = null,
)

/**
 * Chunk from the web.
 *
 * @property uri Optional. URI of the web chunk.
 * @property title Optional. Title of the web chunk.
 */
@Serializable
data class Web(
    val uri: String? = null,
    val title: String? = null,
)

/**
 * Grounding support.
 *
 * @property segment Optional. Segment of the content.
 * @property groundingChunkIndices Optional. A list of indices (into 'grounding_chunk') specifying the
 * grounding sources that support the Claim.
 */
@Serializable
data class GroundingSupport(
    val segment: Segment? = null,
    val groundingChunkIndices: List<Int> = emptyList(),
)

/**
 * Segment of the content.
 *
 * @property startIndex Optional. The index of a character in the content.
 * @property endIndex Optional. The index of a character in the content.
 * @property text Optional. The text of the segment.
 */
@Serializable
data class Segment(
    val startIndex: Int? = null,
    val endIndex: Int? = null,
    val text: String? = null,
)
