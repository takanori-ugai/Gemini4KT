package io.github.ugaikit.gemini4kt.samples

import kotlinx.coroutines.runBlocking

object CountTokensRunner {
    @JvmStatic
    fun main(args: Array<String>) =
        runBlocking {
            CountTokensSample.run()
        }
}
