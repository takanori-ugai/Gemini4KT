package io.github.ugaikit.gemini4kt.samples

import kotlinx.coroutines.runBlocking

object EmbedContentRunner {
    @JvmStatic
    fun main(args: Array<String>) =
        runBlocking {
            EmbedContent.run()
        }
}
