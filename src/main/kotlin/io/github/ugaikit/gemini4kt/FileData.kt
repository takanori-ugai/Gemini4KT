package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

/**
 * Encapsulates data about a file, including its MIME type and a URI pointing to
 * its location.
 *
 * @property mimeType A string representing the MIME type of the file, which
 * describes the nature and format of the file content (e.g., "image/jpeg" for JPEG
 * images).
 * @property fileUri A string containing the Uniform Resource Identifier (URI) of
 * the file. This URI specifies where the file can be accessed or downloaded from,
 * providing a reference to the file's location.
 */
@Serializable
data class FileData(
    val mimeType: String,
    val fileUri: String,
)

class FileDataBuilder {
    var mimeType: String = ""
    var fileUri: String = ""

    fun build() = FileData(mimeType, fileUri)
}

fun fileData(init: FileDataBuilder.() -> Unit): FileData = FileDataBuilder().apply(init).build()
