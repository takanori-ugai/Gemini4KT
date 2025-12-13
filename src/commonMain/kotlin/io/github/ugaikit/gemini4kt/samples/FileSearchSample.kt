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
import io.github.ugaikit.gemini4kt.getApiKey
import io.github.ugaikit.gemini4kt.tool
import kotlinx.coroutines.delay
import kotlinx.io.files.Path

object FileSearchSample {
    suspend fun run(filePath: String) {
        val apiKey = getApiKey()
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
            uploadFileToStore(fileSearch, store.name!!, filePath)

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

    private suspend fun uploadFileToStore(
        fileSearch: FileSearch,
        storeName: String,
        filePath: String,
    ) {
        var operation =
            fileSearch.uploadToFileSearchStore(
                fileSearchStoreName = storeName,
                file = Path(filePath),
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
}
