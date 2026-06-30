package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

/**
 * Metadata returned to the client when grounding is enabled.
 *
 * @property searchEntryPoint Optional. Google search entry for the following-up web searches.
 * @property webSearchQueries Optional. Web search queries for the following-up web searches.
 * @property groundingChunks Optional. List of supporting references retrieved from specified grounding sources.
 * @property groundingSupports Optional. List of grounding support.
 * @property imageSearchQueries Optional. Image search queries used for grounding.
 * @property retrievalMetadata Optional. Metadata related to retrieval in the grounding flow.
 * @property googleMapsWidgetContextToken Optional. Resource name of the Google Maps widget context token.
 */
@Serializable
data class GroundingMetadata(
    val searchEntryPoint: SearchEntryPoint? = null,
    val webSearchQueries: List<String> = emptyList(),
    val groundingChunks: List<GroundingChunk> = emptyList(),
    val groundingSupports: List<GroundingSupport> = emptyList(),
    val imageSearchQueries: List<String> = emptyList(),
    val retrievalMetadata: RetrievalMetadata? = null,
    val googleMapsWidgetContextToken: String? = null,
)

/**
 * Google search entry point.
 *
 * @property renderedContent Optional. Web content snippet that can be embedded in a web page or an app webview.
 * @property sdkBlob Optional. Base64 encoded JSON representing array of &lt;search term, search url&gt; tuple.
 */
@Serializable
data class SearchEntryPoint(
    val renderedContent: String? = null,
    val sdkBlob: String? = null,
)

/**
 * A chunk of content from a grounding source.
 *
 * @property web Optional. Grounding chunk from the web.
 * @property image Optional. Grounding chunk from image search.
 * @property retrievedContext Optional. Grounding chunk from context retrieved by the file search tool.
 * @property maps Optional. Grounding chunk from Google Maps.
 */
@Serializable
data class GroundingChunk(
    val web: Web? = null,
    val image: Image? = null,
    val retrievedContext: RetrievedContext? = null,
    val maps: Maps? = null,
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
 * @property groundingChunkIndices Optional. A list of indices (into 'grounding_chunk')
 *   specifying the grounding sources that support the Claim.
 * @property confidenceScores Optional. Confidence score of the support references.
 * @property renderedParts Optional. Indices into the parts field of the candidate's content.
 */
@Serializable
data class GroundingSupport(
    val segment: Segment? = null,
    val groundingChunkIndices: List<Int> = emptyList(),
    val confidenceScores: List<Double> = emptyList(),
    val renderedParts: List<Int> = emptyList(),
)

/**
 * Segment of the content.
 *
 * @property startIndex Optional. The index of a character in the content.
 * @property endIndex Optional. The index of a character in the content.
 * @property text Optional. The text of the segment.
 * @property partIndex Optional. The index of a Part object within its parent Content object.
 */
@Serializable
data class Segment(
    val startIndex: Int? = null,
    val endIndex: Int? = null,
    val text: String? = null,
    val partIndex: Int? = null,
)
