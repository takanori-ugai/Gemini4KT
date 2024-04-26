package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

/**
 * Contains metadata about citations within a document, including references to
 * the sources of these citations.
 *
 * @property citationSources A list of [CitationSource] objects. Each object
 * provides detailed information about a specific source of citation, including
 * its location within the text and reference details such as a URI and license.
 * This allows for comprehensive tracking and referencing of all sources cited
 * within a document.
 */
@Serializable
data class CitationMetadata(
    val citationSources: List<CitationSource>,
)
