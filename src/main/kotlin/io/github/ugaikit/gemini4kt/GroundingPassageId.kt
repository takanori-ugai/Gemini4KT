package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

/**
 * Represents an identifier for a grounding passage, including its unique ID and
 * the index of the part within the passage.
 *
 * @property passageId A string uniquely identifying the passage. This ID is used
 * to reference the specific passage within a larger document or dataset.
 * @property partIndex An integer representing the index of the part within the
 * passage. This allows for precise identification of parts within passages.
 */
@Serializable
data class GroundingPassageId(
    val passageId: String,
    val partIndex: Int,
)

class GroundingPassageIdBuilder {
    lateinit var passageId: String
    var partIndex: Int = 0

    fun build(): GroundingPassageId =
        GroundingPassageId(
            passageId = passageId,
            partIndex = partIndex,
        )
}

fun groundingPassageId(init: GroundingPassageIdBuilder.() -> Unit): GroundingPassageId =
    GroundingPassageIdBuilder().apply(init).build()
