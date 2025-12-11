package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

/**
 * Represents a chunk of data retrieved by a semantic retriever, containing both
 * the source from which the chunk was extracted and the chunk content itself.
 *
 * @property source The origin or source identifier from which this chunk of data
 * was retrieved. This could be a document ID, URL, or any other form of source
 * identification.
 * @property chunk The actual content of the chunk extracted from the source. This
 * is typically a subset of the content, selected based on semantic relevance or
 * other criteria used by the retriever.
 */
@Serializable
data class SemanticRetrieverChunk(
    val source: String,
    val chunk: String,
)
