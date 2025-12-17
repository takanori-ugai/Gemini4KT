package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

/**
 * Represents the content composed of multiple parts, optionally associated with a specific role.
 *
 * @property parts A list of [io.github.ugaikit.gemini4kt.Part] objects, representing the individual components
 * or sections of the content.
 * @property role An optional string indicating the role or function of this content within a larger context.
 * It can be `null` if the role is not specified or not applicable.
 */
@Serializable
@JsExport
data class Content(
    val parts: Array<Part>? = null,
    val role: String? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Content

        if (parts != null) {
            if (other.parts == null) return false
            if (!parts.contentEquals(other.parts)) return false
        } else if (other.parts != null) return false
        if (role != other.role) return false

        return true
    }

    override fun hashCode(): Int {
        var result = parts?.contentHashCode() ?: 0
        result = 31 * result + (role?.hashCode() ?: 0)
        return result
    }
}

class ContentBuilder {
    private var parts: MutableList<Part> = mutableListOf()
    var role: String? = null

    fun part(init: PartBuilder.() -> Unit) {
        val builder = PartBuilder().apply(init)
        parts.add(builder.build())
    }

    fun build() = Content(parts.toTypedArray(), role)
}

fun content(init: ContentBuilder.() -> Unit): Content = ContentBuilder().apply(init).build()
