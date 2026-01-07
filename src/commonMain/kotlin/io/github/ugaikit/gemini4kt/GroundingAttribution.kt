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

/**
 * Represents the grounding attribution builder.
 */
class GroundingAttributionBuilder {
    /**
     * Holds the source id.
     */
    lateinit var sourceId: AttributionSourceId

    /**
     * Holds the content.
     */
    lateinit var content: Content

    /**
     * Handles source id.
     *
     * @param init The init.
     */
    fun sourceId(init: AttributionSourceIdBuilder.() -> Unit) {
        sourceId = AttributionSourceIdBuilder().apply(init).build()
    }

    /**
     * Handles content.
     *
     * @param init The init.
     */
    fun content(init: ContentBuilder.() -> Unit) {
        content = ContentBuilder().apply(init).build()
    }

    /**
     * Handles build.
     */
    fun build() = GroundingAttribution(sourceId, content)
}

/**
 * Handles grounding attribution.
 *
 * @param init The init.
 */
fun groundingAttribution(init: GroundingAttributionBuilder.() -> Unit): GroundingAttribution =
    GroundingAttributionBuilder()
        .apply(init)
        .build()
