package io.github.ugaikit.gemini4kt

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents inline data that can be included in a request or response, typically for media content.
 *
 * @property mime_type The MIME type of the content, indicating the type of media (e.g., "image/jpeg").
 * @property data The actual content data, encoded as a String. This is often base64 encoded data
 * for binary content like images.
 */
@Serializable
data class InlineData(
    @SerialName("mime_type")
    val mimeType: String,
    val data: String,
)

fun buildInlineData(init: InlineDataBuilder.() -> Unit): InlineData {
    val builder = InlineDataBuilder()
    builder.init()
    return builder.build()
}

class InlineDataBuilder {
    private var mimeType: String = ""
    private var data: String = ""

    fun mimeType(init: () -> String) = apply { mimeType = init() }

    fun data(init: () -> String) = apply { data = init() }

    fun build() =
        InlineData(
            mimeType = mimeType,
            data = data,
        )
}
