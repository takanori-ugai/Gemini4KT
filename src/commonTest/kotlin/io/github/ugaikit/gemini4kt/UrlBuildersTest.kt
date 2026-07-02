package io.github.ugaikit.gemini4kt

import kotlin.test.Test
import kotlin.test.assertEquals

class UrlBuildersTest {
    @Test
    fun normalizeResourcePathSegmentsRemovesLeadingRootSegment() {
        val segments = normalizeResourcePathSegments("cachedContents/my-entry", "cachedContents")

        assertEquals(listOf("my-entry"), segments)
    }

    @Test
    fun normalizeResourcePathSegmentsKeepsNonLeadingRootSegment() {
        val segments = normalizeResourcePathSegments("projects/p1/cachedContents/my-entry", "cachedContents")

        assertEquals(listOf("projects", "p1", "cachedContents", "my-entry"), segments)
    }

    @Test
    fun normalizeResourcePathSegmentsNormalizesDuplicateAndMixedSlashes() {
        val segments = normalizeResourcePathSegments("//fileSearchStores///store-1//operations//op-1//")

        assertEquals(listOf("fileSearchStores", "store-1", "operations", "op-1"), segments)
    }

    @Test
    fun normalizeResourcePathSegmentsReturnsEmptyForBlankInput() {
        val segments = normalizeResourcePathSegments("///")

        assertEquals(emptyList(), segments)
    }

    @Test
    fun normalizeModelPathSegmentsStripsModelsPrefixWhenPresent() {
        val segments = normalizeModelPathSegments("models/gemini-2.5-flash")

        assertEquals(listOf("gemini-2.5-flash"), segments)
    }

    @Test
    fun buildUrlBuildsPathAndSkipsNullQueryValues() {
        val url =
            buildUrl(
                baseUrl = "https://example.com/v1beta",
                pathSegments = listOf("cachedContents", "entry-1"),
                queryParameters = linkedMapOf("pageSize" to "10", "pageToken" to null),
            )

        assertEquals("https://example.com/v1beta/cachedContents/entry-1?pageSize=10", url)
    }

    @Test
    fun buildUrlKeepsEmptyStringQueryValues() {
        val url =
            buildUrl(
                baseUrl = "https://example.com/v1beta",
                queryParameters = linkedMapOf("pageToken" to ""),
            )

        assertEquals("https://example.com/v1beta?pageToken=", url)
    }

    @Test
    fun buildModelUrlSupportsEmptyModelNameByTargetingModelsCollection() {
        val url =
            buildModelUrl(
                baseUrl = "https://example.com/v1beta",
                model = "",
                rpcName = "batchGenerateContent",
            )

        assertEquals("https://example.com/v1beta/models:batchGenerateContent", url)
    }
}
