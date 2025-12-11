package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

/**
 * Configures the image generation parameters.
 *
 * @property aspectRatio The aspect ratio of the generated image (e.g., "16:9", "4:3", "1:1").
 * @property imageSize The size of the generated image (e.g., "2K").
 */
@Serializable
data class ImageConfig(
    val aspectRatio: String? = null,
    val imageSize: String? = null,
)

class ImageConfigBuilder {
    var aspectRatio: String? = null
    var imageSize: String? = null

    fun aspectRatio(init: () -> String) = apply { aspectRatio = init() }

    fun imageSize(init: () -> String) = apply { imageSize = init() }

    fun build() =
        ImageConfig(
            aspectRatio = aspectRatio,
            imageSize = imageSize,
        )
}

fun imageConfig(init: ImageConfigBuilder.() -> Unit): ImageConfig = ImageConfigBuilder().apply(init).build()
