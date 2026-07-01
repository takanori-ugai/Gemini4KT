package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Represents inline data that can be included in a request or response, typically for media content.
 *
 * @property mime_type The MIME type of the content, indicating the type of media (e.g., "image/jpeg").
 * @property data The actual content data, encoded as a String. This is often base64 encoded data
 * for binary content like images.
 * @property displayName Optional name that can be referenced from structured responses.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class InlineData(
    val mimeType: String,
    val data: String,
    val displayName: String? = null,
)

/**
 * Handles inline data.
 *
 * @param init The init.
 */
fun inlineData(init: InlineDataBuilder.() -> Unit): InlineData {
    /**
     * Holds the builder.
     */
    val builder = InlineDataBuilder()
    builder.init()
    return builder.build()
}

/**
 * Represents the inline data builder.
 */
class InlineDataBuilder {
    /**
     * Holds the mime type.
     */
    private var mimeType: String? = null

    /**
     * Holds the data.
     */
    private var data: String? = null

    /**
     * Holds the display name.
     */
    private var displayName: String? = null

    /**
     * Handles mime type.
     *
     * @param init The init.
     */
    fun mimeType(init: () -> String) = apply { mimeType = init() }

    /**
     * Set the inline content data for the builder.
     *
     * @param init A lambda that produces the content string (often base64-encoded).
     * @return This builder instance for chaining.
     */
    fun data(init: () -> String) = apply { data = init() }

    /**
     * Set the builder's optional display name.
     *
     * @param init A supplier that returns the display name, or `null` to leave it unset.
     * @return This builder instance for chaining.
     */
    fun displayName(init: () -> String?) = apply { displayName = init() }

    /**
     * Constructs an InlineData instance from the builder's current values.
     *
     * @return An InlineData configured with the builder's `mimeType`, `data`, and `displayName`.
     */
    fun build() =
        InlineData(
            mimeType = requireNotNull(mimeType) { "mimeType must be set before building InlineData." },
            data = requireNotNull(data) { "data must be set before building InlineData." },
            displayName = displayName,
        )
}
