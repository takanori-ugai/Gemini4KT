# Repository Guidelines

## Project Structure & Module Organization
- Core source lives in `src/commonMain/kotlin/io/github/ugaikit/gemini4kt` with per-target implementations in `src/jvmMain`, `src/jsMain`, `src/wasmJsMain`, `src/androidMain`, and native targets such as `src/linuxX64Main`.
- Shared tests sit in `src/commonTest/kotlin`; target-specific suites live under `src/jvmTest/kotlin`, `src/jsTest/kotlin`, and `src/wasmJsTest/kotlin`.
- Build artifacts go to `build/`; static analysis configs are in `config/detekt/detekt.yml`; API docs output to `docs/` via Dokka.

## Build, Test, and Development Commands
- `./gradlew clean build` — compile all targets and run checks.
- `./gradlew jvmTest` (or `jsTest`, `wasmJsTest`) — run unit tests per platform.
- `./gradlew run` — execute the JVM sample entrypoint (`ITTestKt`); set `GEMINI_API_KEY` first.
- `./gradlew ktlintCheck detekt` — Kotlin formatting and static analysis; `ktlintFormat` auto-fixes style.
- `./gradlew jacocoTestReport` — generate JVM coverage reports; `dokkaHtml` renders API docs.

## Coding Style & Naming Conventions
- Kotlin style is enforced by ktlint (4-space indent, trailing commas where useful) and detekt rules in `config/detekt/detekt.yml`.
- Keep public APIs under `io.github.ugaikit.gemini4kt`; match file paths to package structure.
- Classes/objects use PascalCase; functions and properties use camelCase; constants use UPPER_SNAKE_CASE.
- Prefer suspend functions and Ktor clients already configured in `HttpClient.kt`; avoid introducing platform-specific code in `commonMain`.

## Testing Guidelines
- Frameworks: `kotlin-test` for common tests; JUnit 5 and MockK in `jvmTest`.
- Name test files `*Test.kt` and mirror the source package being covered.
- Use Ktor `MockEngine` in `commonTest` for HTTP scenarios; avoid live network calls in CI.
- Run `./gradlew jvmTest jacocoTestReport` before publishing changes; keep coverage from regressing.

## Commit & Pull Request Guidelines
- Follow the existing log style: short, imperative summaries (e.g., “Add streaming response helper”); include scope when helpful.
- PRs should describe behavior changes, list key commands run, and link related issues. Include API doc or screenshot updates when user-facing changes occur.
- Ensure lint (`ktlintCheck detekt`) and tests pass locally; note any skipped checks with rationale.

## Security & Configuration Tips
- Never commit secrets. Provide `GEMINI_API_KEY` via environment variables or local Gradle properties excluded from VCS.
- Avoid logging request payloads containing user data; scrub keys before committing samples.
