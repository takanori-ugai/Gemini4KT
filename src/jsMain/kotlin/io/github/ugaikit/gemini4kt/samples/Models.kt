package io.github.ugaikit.gemini4kt.samples

/**
 * Entry point for running sample snippets on the JS target.
 *
 * @param args The command-line arguments indicating which sample to execute.
 */
suspend fun main(args: Array<String>) {
    // 1. まず引数を確認
    // 2. 空なら環境変数 "CLI_ARGS" を確認 (Node.jsの process.env を参照)
    val targetArgs =
        if (args.isNotEmpty()) {
            args
        } else {
            readCliArgs()
        }

    println("Selected target: ${targetArgs.firstOrNull() ?: "<none>"}")
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

    action()
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

private fun readCliArgs(): Array<String> {
    val envValue =
        js("(typeof process !== 'undefined' && process.env && process.env.CLI_ARGS) ? process.env.CLI_ARGS : null") as String?
    return envValue?.let(::parseCliArgs) ?: emptyArray()
}

private fun parseCliArgs(raw: String): Array<String> {
    val args = mutableListOf<String>()
    val current = StringBuilder()
    var inQuotes = false
    var quoteChar = '\u0000'
    var escaping = false
    var tokenStarted = false

    raw.forEach { ch ->
        when {
            escaping -> {
                current.append(ch)
                escaping = false
                tokenStarted = true
            }
            ch == '\\' -> escaping = true
            inQuotes && ch == quoteChar -> inQuotes = false
            !inQuotes && (ch == '\'' || ch == '"') -> {
                inQuotes = true
                quoteChar = ch
                tokenStarted = true
            }
            !inQuotes && ch.isWhitespace() -> {
                if (tokenStarted) {
                    args.add(current.toString())
                    current.clear()
                    tokenStarted = false
                }
            }
            else -> {
                current.append(ch)
                tokenStarted = true
            }
        }
    }

    if (tokenStarted) {
        args.add(current.toString())
    }

    return args.toTypedArray()
}
