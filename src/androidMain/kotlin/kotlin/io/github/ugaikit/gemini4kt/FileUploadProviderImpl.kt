package kotlin.io.github.ugaikit.gemini4kt

import io.github.ugaikit.gemini4kt.FileUploadProvider
import io.github.ugaikit.gemini4kt.GeminiFile
import io.github.ugaikit.gemini4kt.filesearch.Operation
import io.github.ugaikit.gemini4kt.filesearch.UploadFileSearchStoreRequest
import io.ktor.client.HttpClient
import kotlinx.io.files.Path
import kotlinx.serialization.json.Json

actual class FileUploadProviderImpl actual constructor(
    apiKey: String,
    client: HttpClient?,
    json: Json,
) : FileUploadProvider {
    override suspend fun upload(
        file: Path,
        mimeType: String,
        displayName: String,
    ): GeminiFile {
        TODO("Not yet implemented for Native")
    }

    override suspend fun uploadToFileSearchStore(
        fileSearchStoreName: String,
        file: Path,
        mimeType: String,
        uploadRequest: UploadFileSearchStoreRequest,
    ): Operation {
        TODO("Not yet implemented for Native")
    }
}
