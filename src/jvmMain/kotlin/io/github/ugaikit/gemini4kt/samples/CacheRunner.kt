package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.Gemini
import io.github.ugaikit.gemini4kt.getApiKey
import kotlinx.coroutines.runBlocking

object CacheRunner {
    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            Cache.run(Gemini(getApiKey()))
        }
    }
}
