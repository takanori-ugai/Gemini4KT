package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.Part

/**
 * Prints available fields of each Part in the provided array to standard output.
 *
 * For each non-null part this prints the part's text, executable code (including language and code),
 * and code execution result (including outcome, output, and an image preview when present).
 *
 * @param parts Nullable array of Part objects to inspect; nothing is printed if `parts` is null.
 */
internal fun printExecutionParts(parts: Array<Part>?) {
    parts?.forEach { part ->
        part.text?.let { println("Text: $it") }
        part.executableCode?.let { executable ->
            println("Executable Code (${executable.language}):\n${executable.code}")
        }
        part.codeExecutionResult?.let { result ->
            println("Execution Result (${result.outcome}):")
            result.output?.let { println(it) }
            result.image?.let { image ->
                println("Image Output (${image.mimeType}): ${previewImageData(image.data)}")
            }
        }
    }
}

/**
 * Produce a shortened preview of image data.
 *
 * @param data The full image data string to preview.
 * @param maxLength Maximum number of characters to include in the preview; defaults to 60.
 * @return A string containing up to `maxLength` characters of `data`, followed by "..." if `data` was longer.
 */
private fun previewImageData(
    data: String,
    maxLength: Int = 60,
): String {
    val preview = data.take(maxLength)
    val suffix = if (data.length > maxLength) "..." else ""
    return "$preview$suffix"
}
