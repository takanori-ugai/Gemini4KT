package io.github.ugaikit.gemini4kt.samples

import kotlinx.coroutines.runBlocking

object GoogleSearchSampleRunner {
    @JvmStatic
    fun main(args: Array<String>) =
        runBlocking {
            GoogleSearchSample.run()
        }
}
