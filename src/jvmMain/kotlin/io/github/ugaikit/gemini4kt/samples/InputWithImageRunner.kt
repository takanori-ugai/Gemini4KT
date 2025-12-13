package io.github.ugaikit.gemini4kt.samples

import kotlinx.coroutines.runBlocking

object InputWithImageRunner {
    @JvmStatic
    fun main(args: Array<String>) =
        runBlocking {
            InputWithImage.run(args)
        }
}
