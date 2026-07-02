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
import io.github.ugaikit.gemini4kt.firstTextPartOrEmpty
import io.github.ugaikit.gemini4kt.tool
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.io.files.Path

/**
 * Represents the file search sample.
 */
object FileSearchSample {
    /**
     * Handles run.
     *
     * @param filePath The file path.
     * @param geminiInstance The gemini instance.
     * @param fileSearchInstance The file search instance.
     */
    suspend fun run(
        filePath: String,
        geminiInstance: Gemini? = null,
        fileSearchInstance: FileSearch? = null,
    ) {
        var storeName: String? = null
        withGeminiClient(geminiInstance) { gemini ->
            withFileSearchClient(fileSearchInstance) { fileSearch ->
                try {
                    // 1. Create FileSearchStore
                    val store =
                        fileSearch.createFileSearchStore(
                            FileSearchStore(displayName = "your-fileSearchStore-name"),
                        )
                    storeName = store.name
                    println("Created FileSearchStore: ${store.name}")

                    // 2. Upload file
                    val resolvedStoreName = storeName ?: return@withFileSearchClient
                    uploadFileToStore(fileSearch, resolvedStoreName, filePath)

                    // 3. Generate Content
                    val generateContentRequest =
                        GenerateContentRequest(
                            contents = arrayOf(Content(parts = arrayOf(Part(text = "What does the fox do?")))),
                            tools =
                                arrayOf(
                                    tool {
                                        fileSearch {
                                            fileSearchStoreName(resolvedStoreName)
                                        }
                                    },
                                ),
                        )

                    val response =
                        gemini.generateContent(
                            model = "gemma-4-31b-it",
                            inputJson = generateContentRequest,
                        )

                    println(response.firstTextPartOrEmpty())
                } finally {
                    val currentStoreName = storeName
                    if (currentStoreName != null) {
                        fileSearch.deleteFileSearchStore(currentStoreName, force = true)
                    }
                }
            }
        }
    }

    /**
     * Handles upload file to store.
     *
     * @param fileSearch The file search.
     * @param storeName The store name.
     * @param filePath The file path.
     */
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
        val completed =
            withTimeoutOrNull(60_000) {
                while (operation.done != true) {
                    println("Waiting for operation to complete...")
                    delay(5000L)
                    operation = fileSearch.getFileSearchStoreOperation(operation.name!!)
                }
                true
            }
        if (completed == null) {
            error("Timed out waiting for file search upload completion.")
        }
        println("Upload complete.")
    }
}
