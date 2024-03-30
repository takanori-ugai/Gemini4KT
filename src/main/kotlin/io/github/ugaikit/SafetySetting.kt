package io.github.ugaikit

import kotlinx.serialization.Serializable

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
@Serializable
data class SafetySetting(
    val category: HarmCategory,
    val threshold: Threshold,
)
