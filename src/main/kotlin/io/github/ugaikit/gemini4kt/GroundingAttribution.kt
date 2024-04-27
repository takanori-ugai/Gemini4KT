package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

/**
 * Represents the attribution of grounding, linking content to its source.
 *
 * @property sourceId The [AttributionSourceId] that uniquely identifies the source
 * of the content. This ID helps in tracing the origin of the grounded content.
 * @property content The [Content] that is being attributed to the source. It
 * encapsulates the actual content details, such as text or structured data,
 * derived from the source.
 */
@Serializable
data class GroundingAttribution(
    val sourceId: AttributionSourceId,
    val content: Content,
)

class GroundingAttributionBuilder {
    lateinit var sourceId: AttributionSourceId
    lateinit var content: Content

    fun sourceId(init: AttributionSourceIdBuilder.() -> Unit) {
        sourceId = AttributionSourceIdBuilder().apply(init).build()
    }

    fun content(init: ContentBuilder.() -> Unit) {
        content = ContentBuilder().apply(init).build()
    }

    fun build() = GroundingAttribution(sourceId, content)
}

fun groundingAttribution(init: GroundingAttributionBuilder.() -> Unit): GroundingAttribution = GroundingAttributionBuilder().apply(init).build()
