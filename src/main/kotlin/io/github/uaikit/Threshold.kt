package io.github.uaikit

/**
 * Enumerates the possible thresholds for content blocking or filtering based on safety assessments.
 *
 * This enumeration defines different levels of sensitivity for content filtering mechanisms, allowing
 * for a range of responses from no action to blocking content that meets or exceeds a certain probability
 * of being harmful. The thresholds are used in conjunction with [HarmCategory] settings to determine how
 * content should be handled based on its assessed risk of harm.
 *
 * @property BLOCK_NONE Indicates that no content should be blocked, regardless of its assessed risk.
 * @property BLOCK_ONLY_HIGH Indicates that only content assessed as having a high probability of harm
 * should be blocked.
 * @property BLOCK_MEDIUM_AND_ABOVE Indicates that content assessed as having a medium or higher
 * probability of harm should be blocked.
 * @property BLOCK_LOW_AND_ABOVE Indicates that content assessed as having a low, medium,
 * or high probability of harm should be blocked.
 * @property HARM_BLOCK_THRESHOLD_UNSPECIFIED Indicates that the threshold for blocking has not
 * been specified, which may default to the most permissive or restrictive setting depending
 * on context.
 */
enum class Threshold {
    BLOCK_NONE,
    BLOCK_ONLY_HIGH,
    BLOCK_MEDIUM_AND_ABOVE,
    BLOCK_LOW_AND_ABOVE,
    HARM_BLOCK_THRESHOLD_UNSPECIFIED,
}
