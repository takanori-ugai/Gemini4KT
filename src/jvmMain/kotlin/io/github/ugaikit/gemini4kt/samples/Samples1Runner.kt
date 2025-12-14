package io.github.ugaikit.gemini4kt.samples

import kotlinx.coroutines.runBlocking

object Samples1Runner {
    @JvmStatic
    fun main(args: Array<String>) =
        runBlocking {
            Samples1.run()
        }
}
