package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Represents a Google Search tool.
 *
 * @property timeRangeFilter Optional. Time range filter for results.
 * @property searchTypes Optional. Search types (e.g. web, image) enabled.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class GoogleSearch(
    val timeRangeFilter: Interval? = null,
    val searchTypes: SearchTypes? = null,
)

/**
 * Represents the Google Search tool builder.
 */
class GoogleSearchBuilder {
    /**
     * Holds the time range filter.
     */
    var timeRangeFilter: Interval? = null

    /**
     * Holds the search types.
     */
    var searchTypes: SearchTypes? = null

    /**
     * Handles build.
     */
    fun build() = GoogleSearch(timeRangeFilter, searchTypes)
}

/**
 * Handles google search config.
 *
 * @param init The init.
 */
fun googleSearch(init: GoogleSearchBuilder.() -> Unit): GoogleSearch = GoogleSearchBuilder().apply(init).build()
