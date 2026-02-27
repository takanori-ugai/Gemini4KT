package io.github.ugaikit.gemini4kt.samples

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Entry point for running sample snippets on the JS target.
 *
 * @param args The command-line arguments indicating which sample to execute.
 */
fun main(args: Array<String>) {
    // 1. まず引数を確認
    // 2. 空なら環境変数 "CLI_ARGS" を確認 (Node.jsの process.env を参照)
    val targetArgs =
        if (args.isNotEmpty()) {
            args
        } else {
            val envValue = js("process.env.CLI_ARGS") as? String
            // 空白で区切って配列にする
            envValue?.split(" ")?.toTypedArray() ?: emptyArray()
        }

    // デバッグ表示
    println("ターゲット引数(確定): ${targetArgs.joinToString(", ")}")
    if (targetArgs.isEmpty()) {
        println("実行するターゲットを指定してください (models, samples1, ...)")
        return
    }
    val target = targetArgs[0].lowercase()
    val action = sampleActions[target]
    if (action == null) {
        println("不明なターゲット: $target")
        return
    }

    GlobalScope.launch {
        action()
    }
}

/**
 * Available sample launchers keyed by the expected argument.
 */
private val sampleActions: Map<String, suspend () -> Unit> =
    mapOf(
        "models" to { Models.listModels() },
        "samples1" to { Samples1.run() },
        "functionexample1" to { FunctionExample1.run() },
        "functionexample2" to { FunctionExample2.run() },
        "functionexample3" to { FunctionExample3.run() },
        "batch" to { BatchSample.run() },
        "counttokens" to { CountTokensSample.run() },
        "embed" to { EmbedContent.run() },
        "stream" to { StreamGenerateContentSample.run() },
    )
