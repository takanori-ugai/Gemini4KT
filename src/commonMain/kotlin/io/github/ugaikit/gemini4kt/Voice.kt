package io.github.ugaikit.gemini4kt

import io.github.ugaikit.gemini4kt.interaction.InteractionAudioContent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/** A catalog, designed, or replicated Gemini voice. */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class Voice(
    val id: String,
    @SerialName("display_name") val displayName: String? = null,
    val type: String? = null,
    val model: String? = null,
    val gender: String? = null,
    @SerialName("language_code") val languageCode: String? = null,
    val accent: String? = null,
    val pitch: String? = null,
    val persona: String? = null,
    val contexts: Array<String>? = null,
    val description: String? = null,
    @SerialName("sample_audio") val sampleAudio: InteractionAudioContent? = null,
)

/** Paginated response from `GET /v1beta/voices`. */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class ListVoicesResponse(
    val voices: Array<Voice>? = null,
    @SerialName("next_page_token") val nextPageToken: String? = null,
)

/** Optional filters accepted by `GET /v1beta/voices`. */
@OptIn(ExperimentalJsExport::class)
@JsExport
data class ListVoicesOptions(
    val languageCode: Array<String>? = null,
    val regionCode: Array<String>? = null,
    val accent: Array<String>? = null,
    val gender: Array<String>? = null,
    val pitch: Array<String>? = null,
    val persona: Array<String>? = null,
    val contexts: Array<String>? = null,
    val type: Array<String>? = null,
    val search: String? = null,
    val pageSize: Int? = null,
    val pageToken: String? = null,
)
