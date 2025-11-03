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

/**
 * Builder class for constructing a FileData object.
 */
class FileDataBuilder {
    /**
     * The MIME type of the file.
     */
    var mimeType: String = ""

    /**
     * The URI of the file.
     */
    var fileUri: String = ""

    /**
     * Builds and returns a FileData object with the current state of the builder.
     *
     * @return A new instance of FileData.
     */
    fun build() = FileData(mimeType, fileUri)
}

/**
 * DSL function to create a FileData object using a builder pattern.
 *
 * @param init A lambda function to initialize the FileDataBuilder.
 * @return A new instance of FileData.
 */
fun fileData(init: FileDataBuilder.() -> Unit): FileData = FileDataBuilder().apply(init).build()
