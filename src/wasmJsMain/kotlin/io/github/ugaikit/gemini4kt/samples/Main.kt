package io.github.ugaikit.gemini4kt.samples

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

fun main() {
    GlobalScope.launch {
        Models.listModels()
    }
}