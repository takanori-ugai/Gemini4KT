package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Defines a safety setting for filtering or evaluating content based on a specific harm category.
 *
 * This data class specifies how content should be assessed and potentially filtered according to its
 * likelihood of falling into a particular harm category, such as harassment or hate speech. The setting
 * includes both the category of harm to be considered and the threshold for action or concern.
 *
 * @property category The [HarmCategory] indicating the type of potential harm this setting applies to.
 * @property threshold The [Threshold] indicating the minimum level of probability at which content
 *                     in the specified harm category should be considered actionable or concerning.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class SafetySetting(
    val category: HarmCategory,
    val threshold: Threshold,
)

/**
 * Represents the safety setting builder.
 */
class SafetySettingBuilder {
    /**
     * Holds the category.
     */
    lateinit var category: HarmCategory

    /**
     * Holds the threshold.
     */
    lateinit var threshold: Threshold

    /**
     * Handles build.
     */
    fun build() = SafetySetting(category, threshold)
}

/**
 * Handles safety setting.
 *
 * @param init The init.
 */
fun safetySetting(init: SafetySettingBuilder.() -> Unit): SafetySetting = SafetySettingBuilder().apply(init).build()
