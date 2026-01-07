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
    private var mimeType: String = ""

    /**
     * Holds the data.
     */
    private var data: String = ""

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
     * Handles data.
     *
     * @param init The init.
     */
    fun data(init: () -> String) = apply { data = init() }

    /**
     * Handles display name.
     *
     * @param init The init.
     */
    fun displayName(init: () -> String?) = apply { displayName = init() }

    /**
     * Handles build.
     */
    fun build() =
        InlineData(
            mimeType = mimeType,
            data = data,
            displayName = displayName,
        )
}
