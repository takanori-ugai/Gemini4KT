package io.github.ugaikit.gemini4kt

/**
 * Enumerates the possible modes of operation or configuration settings, providing
 * predefined options for various functionalities.
 *
 * @property MODE_UNSPECIFIED Represents an unspecified mode, typically used as a
 * default when no specific mode has been set.
 * @property AUTO Indicates that the operation or setting should be determined
 * automatically based on context or internal logic.
 * @property ANY Suggests that any available mode can be used, allowing for
 * flexibility in operation or configuration.
 * @property NONE Specifies that no particular mode is to be applied, possibly
 * disabling the functionality or leaving it in a neutral state.
 */
enum class Mode {
    MODE_UNSPECIFIED,
    AUTO,
    ANY,
    NONE,
}
