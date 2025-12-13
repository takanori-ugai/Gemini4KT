package io.github.ugaikit.gemini4kt.samples

import kotlinx.coroutines.runBlocking

object FunctionExample3Runner {
    @JvmStatic
    fun main(args: Array<String>) =
        runBlocking {
            FunctionExample3.run()
        }
}
