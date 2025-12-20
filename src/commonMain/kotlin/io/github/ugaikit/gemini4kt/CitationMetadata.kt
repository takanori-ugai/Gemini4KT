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

/**
 * Represents the citation metadata builder.
 */
class CitationMetadataBuilder {
    /**
     * Holds the citation sources.
     */
    private val citationSources: MutableList<CitationSource> = mutableListOf()

    /**
     * Handles citation source.
     *
     * @param init The init.
     */
    fun citationSource(init: CitationSourceBuilder.() -> Unit) {
        citationSources.add(CitationSourceBuilder().apply(init).build())
    }

    /**
     * Handles build.
     */
    fun build(): CitationMetadata =
        CitationMetadata(
            citationSources = citationSources,
        )
}

/**
 * Handles citation metadata.
 *
 * @param init The init.
 */
fun citationMetadata(init: CitationMetadataBuilder.() -> Unit): CitationMetadata = CitationMetadataBuilder().apply(init).build()
