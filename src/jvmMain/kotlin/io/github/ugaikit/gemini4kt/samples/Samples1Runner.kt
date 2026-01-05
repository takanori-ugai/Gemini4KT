package io.github.ugaikit.gemini4kt.samples

import kotlinx.coroutines.runBlocking

/**
 * Represents the samples1 runner.
 */
object Samples1Runner {
    /**
     * Handles main.
     *
     * @param args The args.
     */
    @JvmStatic
    fun main(args: Array<String>) =
        runBlocking {
            Samples1.run()
        }
}
