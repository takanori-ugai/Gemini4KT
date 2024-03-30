package io.github.uaikit

/**
 * Enum class representing different categories of harmful content.
 *
 * @property HARM_CATEGORY_HARASSMENT Represents content that may be considered harassment.
 * @property HARM_CATEGORY_HATE_SPEECH Represents content that may contain hate speech.
 * @property HARM_CATEGORY_SEXUALLY_EXPLICIT Represents content that is sexually explicit.
 * @property HARM_CATEGORY_DANGEROUS_CONTENT Represents content that may be considered dangerous.
 */
enum class HarmCategory {
    HARM_CATEGORY_UNSPECIFIED,
    HARM_CATEGORY_DEROGATORY,
    HARM_CATEGORY_TOXICITY,
    HARM_CATEGORY_VIOLENCE,
    HARM_CATEGORY_SEXUAL,
    HARM_CATEGORY_MEDICAL,
    HARM_CATEGORY_HARASSMENT,
    HARM_CATEGORY_HATE_SPEECH,
    HARM_CATEGORY_SEXUALLY_EXPLICIT,
    HARM_CATEGORY_DANGEROUS_CONTENT,
}
