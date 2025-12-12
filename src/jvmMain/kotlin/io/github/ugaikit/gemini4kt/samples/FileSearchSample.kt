package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.Content
import io.github.ugaikit.gemini4kt.Gemini
import io.github.ugaikit.gemini4kt.GenerateContentRequest
import io.github.ugaikit.gemini4kt.Part
import io.github.ugaikit.gemini4kt.filesearch.ChunkingConfig
import io.github.ugaikit.gemini4kt.filesearch.FileSearch
import io.github.ugaikit.gemini4kt.filesearch.FileSearchStore
import io.github.ugaikit.gemini4kt.filesearch.UploadFileSearchStoreRequest
import io.github.ugaikit.gemini4kt.filesearch.WhiteSpaceConfig
import io.github.ugaikit.gemini4kt.tool
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.io.File
import java.util.Properties

fun main() {
    runBlocking {
        val apiKey =
            Gemini::class.java.getResourceAsStream("/prop.properties").use { inputStream ->
                Properties()
                    .apply {
                        load(inputStream)
                    }.getProperty("apiKey")
            }
        if (apiKey == null) {
            println("API key not found.")
            return@runBlocking
        }

        val fileSearch = FileSearch(apiKey)
        val gemini = Gemini(apiKey)

        // 1. Create FileSearchStore
        val store =
            fileSearch.createFileSearchStore(
                FileSearchStore(displayName = "your-fileSearchStore-name"),
            )
        println("Created FileSearchStore: ${store.name}")

        try {
            // 2. Upload file
            uploadFileToStore(fileSearch, store.name!!)

            // 3. Generate Content
            val generateContentRequest =
                GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = "What does the fox do?")))),
                    tools =
                        listOf(
                            tool {
                                fileSearch {
                                    fileSearchStoreName(store.name!!)
                                }
                            },
                        ),
                )

            val response =
                gemini.generateContent(
                    model = "gemini-2.5-flash",
                    inputJson = generateContentRequest,
                )

            println(
                response.candidates
                    .firstOrNull()
                    ?.content
                    ?.parts
                    ?.firstOrNull()
                    ?.text,
            )
        } finally {
            // Clean up
            fileSearch.deleteFileSearchStore(store.name!!, force = true)
        }
    }
}

private suspend fun uploadFileToStore(
    fileSearch: FileSearch,
    storeName: String,
) {
    val file = File("sample.txt")
    if (!file.exists()) {
        file.writeText("The quick brown fox jumps over the lazy dog.")
    }

    var operation =
        fileSearch.uploadToFileSearchStore(
            fileSearchStoreName = storeName,
            file = file,
            mimeType = "text/plain",
            uploadRequest =
                UploadFileSearchStoreRequest(
                    displayName = "file-name",
                    chunkingConfig =
                        ChunkingConfig(
                            whiteSpaceConfig =
                                WhiteSpaceConfig(
                                    maxTokensPerChunk = 200,
                                    maxOverlapTokens = 20,
                                ),
                        ),
                ),
        )

    // 3. Poll operation
    while (operation.done != true) {
        println("Waiting for operation to complete...")
        val delayTime = 5000L
        delay(delayTime)
        operation = fileSearch.getFileSearchStoreOperation(operation.name!!)
    }
    println("Upload complete.")
}
