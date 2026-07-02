package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.Gemini
import io.github.ugaikit.gemini4kt.GeminiAI
import io.github.ugaikit.gemini4kt.batch.Batch
import io.github.ugaikit.gemini4kt.filesearch.FileSearch
import io.github.ugaikit.gemini4kt.getApiKey

/**
 * Runs a block with a Gemini client, creating and closing one when needed.
 */
internal suspend inline fun <T> withGeminiClient(
    gemini: Gemini?,
    crossinline block: suspend (Gemini) -> T,
): T {
    val client = gemini ?: Gemini(getApiKey())
    return try {
        block(client)
    } finally {
        if (gemini == null) {
            client.close()
        }
    }
}

/**
 * Runs a block with a Gemini AI client, creating and closing one when needed.
 */
internal suspend inline fun <T> withGeminiAIClient(
    client: GeminiAI?,
    crossinline block: suspend (GeminiAI) -> T,
): T {
    val resolvedClient = client ?: GeminiAI(apiKey = getApiKey())
    return try {
        block(resolvedClient)
    } finally {
        if (client == null) {
            resolvedClient.close()
        }
    }
}

/**
 * Runs a block with a Batch client, creating and closing one when needed.
 */
internal suspend inline fun <T> withBatchClient(
    batch: Batch?,
    crossinline block: suspend (Batch) -> T,
): T {
    val client = batch ?: Batch(getApiKey())
    return try {
        block(client)
    } finally {
        if (batch == null) {
            client.close()
        }
    }
}

/**
 * Runs a block with a FileSearch client, creating and closing one when needed.
 */
internal suspend inline fun <T> withFileSearchClient(
    fileSearch: FileSearch?,
    crossinline block: suspend (FileSearch) -> T,
): T {
    val client = fileSearch ?: FileSearch(getApiKey())
    return try {
        block(client)
    } finally {
        if (fileSearch == null) {
            client.close()
        }
    }
}
