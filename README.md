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

### Gemini API CLI for scripts and CI

[Gemini API CLI (`gemini-api`)](https://aistudio.google.com/docs/cli) is Google's
stateless command-line client for the Interactions API. Use it to call Gemini from
an AI coding agent, a shell script, or a CI job. Use Gemini4KT when calling the API
from Kotlin code. `gemini-api` is a separate tool from the interactive Gemini CLI
(`@google/gemini-cli`) and is not required to build or use this library.

Install a specific release on macOS or Linux, using an actual tag from the
[CLI releases](https://github.com/google-gemini/gemini-api-cli/releases) in place of
`<release-tag>`:

```bash
curl -fsSL https://raw.githubusercontent.com/google-gemini/gemini-api-cli/main/scripts/install.sh \
  | GEMINI_VERSION='<release-tag>' bash
```

Keep the release pinned in scripts and CI because the CLI is beta. Provide
`GEMINI_API_KEY` through the environment, as for this repository's Kotlin samples.
Inspect the available commands and request schema, then preview a request offline:

```bash
gemini-api --usage
gemini-api generate --schema
gemini-api generate "Explain Kotlin coroutines briefly." \
  --stream=false -o json --body '{"store":false}' --dry-run
```

Remove `--dry-run` to make an API request. Use `--stream=false -o json` for a single
JSON result in scripts. These examples generate text and media, analyze a local
file, and start a background managed agent:

```bash
gemini-api generate "Explain Kotlin coroutines briefly." \
  --stream=false -o json --body '{"store":false}'
gemini-api image "A small blue robot reading a book" --out ./robot.jpg
gemini-api analyze -i ./report.pdf "Summarize the key findings."
gemini-api agent run "Compare Kotlin HTTP clients and cite sources." \
  --agent deep-research-preview-04-2026 --background --stream=false -o json
# Save the returned interaction id and pass it to:
# gemini-api agent status <interaction-id> -o json
```

Commands keep no hidden local conversation state. Continue an interaction explicitly
with `--previous-interaction-id`. Local statelessness does not disable server-side
request logging; use `--body '{"store":false}'` where supported to opt out of storage.
For advanced options, inspect the command's `--schema` before supplying `--body`.

In a GitHub Actions job with a pinned CLI release installed on `PATH`, a step can
inject the API key from repository secrets and capture structured output:

```yaml
- name: Generate text with Gemini API CLI
  env:
    GEMINI_API_KEY: ${{ secrets.GEMINI_API_KEY }}
  run: |
    mkdir -p artifacts
    gemini-api generate "Explain Kotlin coroutines briefly." \
      --stream=false -o json --body '{"store":false}' > artifacts/gemini-response.json
```

Check the command's exit status: `0` means success, `1` a runtime or API error,
`2` invalid usage, and `3` an authentication error. Standard HTTP, SSE streaming,
and asynchronous polling are supported. For bidirectional real-time audio or video,
use this library's `GeminiLive` client.

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
