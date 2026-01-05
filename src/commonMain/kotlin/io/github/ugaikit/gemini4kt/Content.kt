package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Represents the content composed of multiple parts, optionally associated with a specific role.
 *
 * @property parts A list of [io.github.ugaikit.gemini4kt.Part] objects, representing the individual components
 * or sections of the content.
 * @property role An optional string indicating the role or function of this content within a larger context.
 * It can be `null` if the role is not specified or not applicable.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class Content(
    val parts: Array<Part>? = null,
    val role: String? = null,
)

/**
 * Represents the content builder.
 */
class ContentBuilder {
    /**
     * Holds the parts.
     */
    private var parts: MutableList<Part> = mutableListOf()

    /**
     * Holds the role.
     */
    var role: String? = null

    /**
     * Handles part.
     *
     * @param init The init.
     */
    fun part(init: PartBuilder.() -> Unit) {
        val builder = PartBuilder().apply(init)
        parts.add(builder.build())
    }

    /**
     * Handles build.
     */
    fun build() = Content(parts.toTypedArray(), role)
}

/**
 * Handles content.
 *
 * @param init The init.
 */
fun content(init: ContentBuilder.() -> Unit): Content = ContentBuilder().apply(init).build()
