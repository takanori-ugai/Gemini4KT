package io.github.ugaikit.gemini4kt.samples

import kotlinx.coroutines.runBlocking

/**
 * Represents the embed content runner.
 */
object EmbedContentRunner {
    /**
     * Handles main.
     *
     * @param args The args.
     */
    @JvmStatic
    fun main(args: Array<String>) =
        runBlocking {
            EmbedContent.run()
        }
}
