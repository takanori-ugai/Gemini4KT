package io.github.ugaikit.gemini4kt

import io.github.ugaikit.gemini4kt.batch.batchItemRequest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class GenerateContentStoreTest {
    @Test
    fun unspecifiedStoreIsOmittedEvenWhenEncodingDefaults() {
        val request = GenerateContentRequest(contents = emptyArray())
        for (encodeDefaults in listOf(false, true)) {
            val json = Json { this.encodeDefaults = encodeDefaults }
            assertFalse(json.encodeToJsonElement(request).jsonObject.containsKey("store"))
        }
        assertNull(Json.decodeFromString<GenerateContentRequest>("""{"contents":[]}""").store)
    }

    @Test
    fun explicitStoreValuesAreSerializedAndDecoded() {
        for (store in listOf(false, true)) {
            for (encodeDefaults in listOf(false, true)) {
                val json = Json { this.encodeDefaults = encodeDefaults }
                val request = GenerateContentRequest(contents = emptyArray(), store = store)
                val encoded = json.encodeToString(request)
                assertEquals(JsonPrimitive(store), json.parseToJsonElement(encoded).jsonObject["store"])
                assertEquals(store, json.decodeFromString<GenerateContentRequest>(encoded).store)
            }
        }
    }

    @Test
    fun builderExposesStorageOverrideAndDefaultsToProjectSetting() {
        for (store in listOf(null, false, true)) {
            val request =
                generateContentRequest {
                    content { part { text { "Hello" } } }
                    this.store = store
                }
            assertEquals(store, request.store)
            assertEquals(store, request.copy(contents = arrayOf(Content(parts = arrayOf(Part(text = "Next"))))).store)
        }
    }

    @Test
    fun batchItemsPreserveStorageOverrides() {
        for (store in listOf(null, false, true)) {
            val item =
                batchItemRequest {
                    request(GenerateContentRequest(contents = emptyArray(), store = store))
                }
            if (store == null) {
                assertFalse(item.request.jsonObject.containsKey("store"))
            } else {
                assertEquals(JsonPrimitive(store), item.request.jsonObject["store"])
            }
        }
    }
}
