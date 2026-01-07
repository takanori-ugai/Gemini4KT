package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.Part

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

private fun previewImageData(
    data: String,
    maxLength: Int = 60,
): String {
    val preview = data.take(maxLength)
    val suffix = if (data.length > maxLength) "..." else ""
    return "$preview$suffix"
}
