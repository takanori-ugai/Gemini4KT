package io.github.uaikit

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a part of content, which can either be text or inline data.
 *
 * This class is designed to accommodate different types of content within a single structure,
 * allowing for flexibility in representing content elements that might include plain text,
 * images, or other binary data as inline content.
 *
 * @property text The text content of the part. It is nullable to allow for parts that contain only inline data.
 * @property inline_data An [InlineData] object representing binary data (e.g., an image) included inline.
 *                       It is nullable to allow for parts that contain only text.
 */
@Serializable
data class Part(
    val text: String? = null,
    @SerialName("inline_data")
    val inlineData: InlineData? = null,
)
