package io.github.ugaikit

/**
 * Enum class representing different categories of harmful content.
 *
 * @property HARM_CATEGORY_HARASSMENT Represents content that may be considered harassment.
 * @property HARM_CATEGORY_HATE_SPEECH Represents content that may contain hate speech.
 * @property HARM_CATEGORY_SEXUALLY_EXPLICIT Represents content that is sexually explicit.
 * @property HARM_CATEGORY_DANGEROUS_CONTENT Represents content that may be considered dangerous.
 */
enum class HarmProbability {
    HARM_PROBABILITY_UNSPECIFIED,
    NEGLIGIBLE,
    LOW,
    MEDIUM,
    HIGH,
}
