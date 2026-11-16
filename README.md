# Gemini4KT

We will not maintain this library anymore. We are moving to Google Gen AI Java SDK(https://github.com/googleapis/java-genai).

## Kotlin Wrapper Library for Gemini's RestAPI

This is a Kotlin Multiplatform wrapper library for Gemini's RestAPI, a lightweight library that does not require the Android SDK.

See [RestAPI specifications](https://ai.google.dev/gemini-api/docs/quickstart?hl=en) and https://ai.google.dev/gemini-api/reference/rest for more information.

## Running the Example

Set your Gemini API key as an environment variable before running the sample:

```bash
cp .env.example .env
# Edit .env and set GEMINI_API_KEY=your-real-key
source .env
./gradlew jvmRun
```

`.env` is ignored by git so your key stays local. Look at `src/commonTest/kotlin/io/github/ugaikit/gemini4kt/samples` for sample usage.

### Generate Content request storage

For paid-tier projects created on or after October 26, 2026, the Generate Content API
stores requests by default. Older projects retain their existing setting. Gemini4KT
leaves `store` unspecified by default so the project's setting applies.

Set `store = false` to disable storage for an individual request, or `store = true`
to explicitly enable it:

```kotlin
val request = generateContentRequest {
    content { part { text { "Hello" } } }
    store = false
}
val response = gemini.generateContent(request)
```

The constructor also accepts `GenerateContentRequest(contents = contents, store = false)`.
The same setting applies when the request is passed to `streamGenerateContent`, used
for automatic function calling, or included in a batch with `batchItemRequest { request(request) }`.
For file-backed batches, include `"store": false` in each Generate Content request in
the input JSONL file; Gemini4KT does not rewrite uploaded files.

You can also change the project default in the Logs and Datasets dashboard's Settings
panel. See [Logs and datasets](https://aistudio.google.com/docs/logs-datasets#configure-logging)
and the [October 26 release notes](https://aistudio.google.com/docs/changelog#10-26-2026).

### Managed agent environment migration

`Agent.baseEnvironment` and `CreateAgentRequest.baseEnvironment` accept either an inline
`AgentEnvironment` or an `AgentEnvironmentReference`. Code that needs inline environment fields
such as `sources`, `type`, or `network` can use `agent.inlineEnvironment` or
`request.inlineEnvironment` and should handle `null` when the API returns an environment ID.

## API Document
The API documentation is available at [https://takanori-ugai.github.io/Gemini4KT/](https://takanori-ugai.github.io/Gemini4KT/).

## Usage in Gradle
```gradle.kts
kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation("io.github.ugaikit:gemini4kt:0.8.0")
            }
        }
    }
}
```
