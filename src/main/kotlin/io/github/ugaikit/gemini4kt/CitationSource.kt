package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

/**
 * Represents a source of citation, detailing the location within the text and
 * providing reference information such as a URI and license.
 *
 * @property startIndex The starting index in the text where the citation begins.
 * This is an integer value indicating the position of the first character of the
 * cited content.
 * @property endIndex The ending index in the text where the citation ends. This
 * is an integer value indicating the position of the last character of the cited
 * content. Together with [startIndex], it defines the exact span of the cited
 * section in the text.
 * @property uri A string containing the Uniform Resource Identifier (URI) where
 * more information about the cited content can be found. This could be a link to
 * a webpage, digital document, or any online resource.
 * @property license A string specifying the license under which the cited content
 * is made available. This provides information on how the content can be used or
 * reproduced.
 */
@Serializable
data class CitationSource(
    val startIndex: Int,
    val endIndex: Int,
    val uri: String,
    val license: String,
)

class CitationSourceBuilder {
    var startIndex: Int = 0
    var endIndex: Int = 0
    lateinit var uri: String
    lateinit var license: String

    fun build(): CitationSource =
        CitationSource(
            startIndex = startIndex,
            endIndex = endIndex,
            uri = uri,
            license = license,
        )
}

fun citationSource(init: CitationSourceBuilder.() -> Unit): CitationSource = CitationSourceBuilder().apply(init).build()
