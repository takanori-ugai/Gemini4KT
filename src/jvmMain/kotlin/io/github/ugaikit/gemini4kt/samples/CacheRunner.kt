package io.github.ugaikit.gemini4kt.samples

import kotlinx.coroutines.runBlocking

/**
 * Represents the cache runner.
 */
object CacheRunner {
    /**
     * Handles main.
     *
     * @param args The args.
     */
    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            Cache.run()
        }
    }
}
