package io.github.ugaikit.gemini4kt

/**
 * Enumerates categories of harm that content may be classified under, providing
 * a structured way to identify and categorize potentially harmful or sensitive
 * content.
 *
 * @property HARM_CATEGORY_UNSPECIFIED Used as a default value when the harm category
 * is not specified or determined.
 * @property HARM_CATEGORY_DEROGATORY Refers to content that is derogatory or demeaning
 * towards individuals or groups.
 * @property HARM_CATEGORY_TOXICITY Identifies content that is toxic or harmful in
 * nature, potentially causing distress or harm to viewers.
 * @property HARM_CATEGORY_VIOLENCE Categorizes content that depicts or promotes
 * violence.
 * @property HARM_CATEGORY_SEXUAL Relates to content that includes sexual themes or
 * references.
 * @property HARM_CATEGORY_MEDICAL Pertains to content that involves medical or health-
 * related information, which could be sensitive.
 * @property HARM_CATEGORY_HARASSMENT Encompasses content that could be considered as
 * harassment or bullying.
 * @property HARM_CATEGORY_HATE_SPEECH Identifies content that contains hate speech,
 * targeting individuals or groups based on specific characteristics.
 * @property HARM_CATEGORY_SEXUALLY_EXPLICIT Refers to content that is sexually explicit
 * or pornographic in nature.
 * @property HARM_CATEGORY_DANGEROUS_CONTENT Categorizes content that is dangerous,
 * promoting activities or behaviors that could lead to harm.
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
