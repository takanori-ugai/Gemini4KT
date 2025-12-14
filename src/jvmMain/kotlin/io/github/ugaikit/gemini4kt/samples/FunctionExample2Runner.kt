package io.github.ugaikit.gemini4kt.samples

import kotlinx.coroutines.runBlocking

object FunctionExample2Runner {
    @JvmStatic
    fun main(args: Array<String>) =
        runBlocking {
            FunctionExample2.run()
        }
}
