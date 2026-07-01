# GEMINI.md - Gemini4KT Project

This document provides a comprehensive overview of the Gemini4KT project, its structure, and how to work with it.

## Project Overview

Gemini4KT is a lightweight, Kotlin Multiplatform wrapper library for the Gemini REST API. It allows developers to interact with the Gemini API without needing the full Android SDK. The project is built with Kotlin and Gradle, and it targets multiple platforms including JVM, JavaScript, Android, and native binaries (Linux, Windows, iOS).

**Note:** The project is deprecated and the maintainers are moving to the official Google Gen AI Java SDK.

The core of the library resides in the `Gemini.kt` file, which provides a client for making requests to the various Gemini API endpoints.

**Key Technologies:**

*   **Language:** Kotlin
*   **Build Tool:** Gradle
*   **Core Dependencies:**
    *   [Ktor](https://ktor.io/): For making HTTP requests to the Gemini API.
    *   [Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization): For JSON serialization and deserialization.
*   **Code Quality:**
    *   [Detekt](https://detekt.dev/): Static code analysis for Kotlin.
    *   [Spotless](https://github.com/diffplug/spotless): Code formatting.
    *   [ktlint](https://ktlint.github.io/): An anti-bikeshedding Kotlin linter with built-in formatter.
    *   [SpotBugs](https://spotbugs.github.io/): Static analysis tool to find bugs in Java code.

## Building and Running

### Prerequisites

*   Java Development Kit (JDK) version 11 or higher.
*   A Gemini API key.

### Setting up the Environment

1.  Clone the repository.
2.  Create a `.env` file in the root of the project by copying the `.env.example` file.
    ```bash
    cp .env.example .env
    ```
3.  Edit the `.env` file and add your Gemini API key:
    ```
    GEMINI_API_KEY=your-real-key
    ```

### Running the Example

To run the provided example, which demonstrates the library's usage, execute the following command from the project's root directory:

```bash
./gradlew run
```

This will execute the `main` function in `io.github.ugaikit.gemini4kt.ITTestKt`.

### Building the Library

To build the library and generate the necessary artifacts, you can use the following Gradle command:

```bash
./gradlew build
```

## Testing

The project uses [JUnit Platform](https://junit.org/junit5/docs/current/user-guide/#junit-platform) for testing. To run the tests, use the following command:

```bash
./gradlew test
```

The main test file, `io.github.ugaikit.gemini4kt.ITTest.kt`, also serves as a good source of usage examples.

## Development Conventions

The project enforces a set of development conventions to maintain code quality and consistency.

*   **Static Analysis:** [Detekt](https://detekt.dev/) is used for static analysis. The configuration can be found in `config/detekt/detekt.yml`.
*   **Code Formatting:** [Spotless](https://github.com/diffplug/spotless) and [ktlint](https://ktlint.github.io/) are used to enforce a consistent code style. It's recommended to run the formatter before committing any changes.
    ```bash
    ./gradlew spotlessApply ktlintFormat
    ```
*   **API Documentation:** The project uses [Dokka](https://github.com/Kotlin/dokka) to generate API documentation. You can generate the documentation by running:
    ```bash
    ./gradlew dokkaHtml
    ```
    The output will be in `build/dokka/html`.
