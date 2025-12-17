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
@JsExport
data class GroundingMetadata(
    val searchEntryPoint: SearchEntryPoint? = null,
    val webSearchQueries: Array<String> = emptyArray(),
    val groundingChunks: Array<GroundingChunk> = emptyArray(),
    val groundingSupports: Array<GroundingSupport> = emptyArray(),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as GroundingMetadata

        if (searchEntryPoint != other.searchEntryPoint) return false
        if (!webSearchQueries.contentEquals(other.webSearchQueries)) return false
        if (!groundingChunks.contentEquals(other.groundingChunks)) return false
        if (!groundingSupports.contentEquals(other.groundingSupports)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = searchEntryPoint?.hashCode() ?: 0
        result = 31 * result + webSearchQueries.contentHashCode()
        result = 31 * result + groundingChunks.contentHashCode()
        result = 31 * result + groundingSupports.contentHashCode()
        return result
    }
}

/**
 * Google search entry point.
 *
 * @property renderedContent Optional. Web content snippet that can be embedded in a web page or an app
 * webview.
 */
@Serializable
@JsExport
data class SearchEntryPoint(
    val renderedContent: String? = null,
)

/**
 * A chunk of content from a grounding source.
 *
 * @property web Optional. Grounding chunk from the web.
 */
@Serializable
@JsExport
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
@JsExport
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
@JsExport
data class GroundingSupport(
    val segment: Segment? = null,
    val groundingChunkIndices: Array<Int> = emptyArray(),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as GroundingSupport

        if (segment != other.segment) return false
        if (!groundingChunkIndices.contentEquals(other.groundingChunkIndices)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = segment?.hashCode() ?: 0
        result = 31 * result + groundingChunkIndices.contentHashCode()
        return result
    }
}

/**
 * Segment of the content.
 *
 * @property startIndex Optional. The index of a character in the content.
 * @property endIndex Optional. The index of a character in the content.
 * @property text Optional. The text of the segment.
 */
@Serializable
@JsExport
data class Segment(
    val startIndex: Int? = null,
    val endIndex: Int? = null,
    val text: String? = null,
)
