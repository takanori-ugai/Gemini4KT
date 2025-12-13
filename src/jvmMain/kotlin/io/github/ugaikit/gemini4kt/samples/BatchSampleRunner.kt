package io.github.ugaikit.gemini4kt.samples

import kotlinx.coroutines.runBlocking

object BatchSampleRunner {
    @JvmStatic
    fun main(args: Array<String>) =
        runBlocking {
            BatchSample.run()
        }
}
