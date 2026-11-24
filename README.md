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

### Gemini 4 Hydrogen and `xhigh` thinking

Use `model = "gemini-4-hydrogen"` with either generation API. Hydrogen supports
`low`, `medium`, `high` (the server default), and `xhigh`. Generate Content uses
`ThinkingConfig(thinkingLevel = ThinkingLevel.XHIGH)`; Interactions uses
`InteractionGenerationConfig(thinkingLevel = io.github.ugaikit.gemini4kt.interaction.ThinkingLevel.XHIGH)`:

```kotlin
val response = geminiAI.createInteraction(
    CreateInteractionRequest(
        model = "gemini-4-hydrogen",
        input = "Review this migration design for concurrency problems.",
        generationConfig = InteractionGenerationConfig(
            thinkingLevel = io.github.ugaikit.gemini4kt.interaction.ThinkingLevel.XHIGH,
        ),
    ),
)
```

When migrating from Gemini 3.8 Flash, omit `minimal`, legacy thinking budgets,
explicit `cachedContent`, and sampling overrides (`temperature`, `topP`, `topK`).
The shared 1,048,576-token context includes input, thinking, and output. Synchronous
requests are limited to 65,536 tokens per hop; use Interactions `background = true`
with a larger `maxOutputTokens` for longer outputs. The library leaves output caps
and thinking depth unset unless configured by the caller.

For background generation, poll with `getInteraction(id)` while the status is
`QUEUED` or `IN_PROGRESS`, then read `allOutputText` to concatenate text from all
model output steps. To resume a stored stream, collect
`resumeInteractionStream(id, lastEventId)` using the latest received `event_id`.

For foreground long decode, Gemini4KT exposes the REST continuation tokens so the
caller controls each request:

- **Interactions**: On `REQUIRES_CONTINUATION`, create the next request with the
  same model and generation settings, the hop's `continuationToken`, and its ID
  as `previousInteractionId`. Omit `input` for a stored interaction. For
  `store = false`, resend the original input and omit `previousInteractionId`.
- **Generate Content**: On `finishReason == "CONTINUATION"`, resend the same
  request with the candidate's `continuationToken` at the request top level,
  for example `request.copy(continuationToken = candidate.continuationToken)`.
  Combine output parts across hops in order to preserve thought signatures when replaying history.

See the long decode guides for [Interactions](https://aistudio.google.com/docs/long-decode)
and [Generate Content](https://aistudio.google.com/docs/long-decode?generate-content=true).

The Managed Agents Antigravity default is selected by the service and now uses
Hydrogen. Existing `antigravity-preview-09-2026` requests inherit it when no model
override is supplied; `InteractionAgentConfig.model` and `AgentConfig.model` also
accept `gemini-4-hydrogen` explicitly. See the
[Hydrogen migration guide](https://aistudio.google.com/docs/whats-new-gemini-4-hydrogen)
and [model card and pricing](https://aistudio.google.com/docs/models/gemini-4-hydrogen).

### Decisions API (Gemini Decision 1)

`GeminiAI.createDecision` evaluates typed questions over shared input and returns
calibrated probabilities without generated text tokens:

```kotlin
import io.github.ugaikit.gemini4kt.decisions.*

val decision = geminiAI.createDecision(
    CreateDecisionRequest(
        input = "The item arrived broken and I need a replacement.",
        questions = mapOf(
            "damaged" to DecisionQuestion.Noul("Did the item arrive damaged?"),
            "route" to DecisionQuestion.Choice(
                "Which team should handle this?",
                mapOf("support" to "Product problems", "billing" to "Payment problems"),
            ),
            "severity" to DecisionQuestion.Score(
                "How severe is this issue?",
                listOf("Minor", "Workaround available", "Blocked"),
            ),
        ),
    ),
)
val damaged = decision.answers.getValue("damaged") as DecisionAnswer.Noul
println("Probability of yes: ${damaged.noul}; confidence: ${damaged.confidence}")
```

The default model is `gemini-decision-1`. `DecisionInput(InteractionContent(...))`
or `DecisionInput(listOf(...))` accepts text, image, audio, video, and PDF document
blocks using inline data or URI references. Question instructions also accept
`DecisionInput`, restricted to text blocks. Binary questions optionally accept
`NoulCriteria`; categorical answers expose `choice` and `probabilities`; rubric
answers expose `score`, `probabilities`, and `legend`. `decision.usage` reports token
counts, including zero output tokens. API errors use the existing `GeminiException`.
For media URIs, use the Files API or public/signed HTTPS URLs; the Decisions API
does not accept YouTube URLs or direct `gs://` references.

See the [Decisions API guide](https://aistudio.google.com/docs/decisions-api),
[pricing](https://aistudio.google.com/docs/pricing#gemini-decision), and
[rate limits](https://aistudio.google.com/docs/rate-limits#gemini-decision).

### Gemini Omni 1.2 Flash video generation

Use `OmniVideoSample.request()` with `GeminiAI.createInteraction` to generate a
video with `gemini-omni-1.2-flash`:

```kotlin
val video = geminiAI.createInteraction(
    OmniVideoSample.request("Generate a 15-second video of a marble rolling along a wooden track."),
)
// Once the first interaction has completed, continue with an edit or extension:
val extended = geminiAI.createInteraction(
    OmniVideoSample.request(
        prompt = "Extend the video by 5 seconds as the marble reaches the finish line.",
        task = InteractionVideoTask.EXTEND,
        previousInteractionId = video.id,
    ),
)
```

The sample's optional `media` accepts `InteractionContent` images or video. Pass
up to six reference images for reference-to-video, or first/last images for
interpolation. `InteractionGenerationConfig.videoConfig` and `InteractionVideoTask`
provide optional task overrides for text-to-video, image-to-video,
reference-to-video, editing, and extension. Otherwise the model infers the task.
Describe durations and media roles in natural language: general generation supports
up to 15 seconds; interpolation and each extension support 3–10 seconds, with
extended videos up to 30 seconds total. Generated media is available in
`interaction.outputVideo`.

The service automatically routes `gemini-omni-1.1-flash` to 1.2 starting December 3,
2026. New examples use 1.2 explicitly. Pricing and paid-tier quota changes are
service-side; see the [Omni guide](https://aistudio.google.com/docs/omni),
[pricing](https://aistudio.google.com/docs/pricing#gemini-omni-1.2-flash), and
[rate limits](https://aistudio.google.com/docs/rate-limits).

### Office files and multimodal function results

Office and OpenDocument MIME types work with `InlineData`, `FileData`, and
Interactions `InteractionContent(type = "document", ...)` using `data` or `uri`.
The expanded formats include:

| Format | MIME type | Model input |
| --- | --- | --- |
| DOC | `application/msword` | Rendered page images |
| PPT | `application/vnd.ms-powerpoint` | Rendered slide images |
| ODP | `application/vnd.oasis.opendocument.presentation` | Rendered slide images |
| ODS | `application/vnd.oasis.opendocument.spreadsheet` | First sheet's data and formulas |

DOCX, DOTX, ODT, PPTX, XLSX, and XLS retain their existing processing behavior.
MIME types remain strings, so uploads accept the expanded formats without a new
file-type restriction. See [Office and OpenDocument files](https://aistudio.google.com/docs/document-processing#office-documents).

Generate Content `FunctionResponse.parts` accepts inline or URI-based documents,
audio, and video as well as image, PDF, and text results. Both `InlineData` and
`FileData` support `displayName`. Video parts can specify `MediaProcessing.STATIC`
or `MediaProcessing.AGENTIC`:

```kotlin
val result = FunctionResponse(
    name = "read_video",
    response = buildJsonObject { },
    parts = arrayOf(
        Part(
            fileData = FileData("video/mp4", uploadedVideoUri, displayName = "walkthrough"),
            mediaProcessing = MediaProcessing.AGENTIC,
        ),
    ),
)
```

For Interactions, put typed media blocks into a `function_result` step's JSON
`result`. Existing image and text blocks continue to work alongside the new types:

```kotlin
val resultStep = InteractionStep(
    type = "function_result",
    name = "read_files",
    callId = functionCallId,
    result = Json.encodeToJsonElement(
        arrayOf(
            InteractionContent(type = "document", uri = documentUri, mimeType = "application/vnd.ms-powerpoint"),
            InteractionContent(type = "audio", data = audioBase64, mimeType = "audio/wav"),
            InteractionContent(
                type = "video", uri = videoUri, mimeType = "video/mp4",
                processing = InteractionVideoProcessing.AGENTIC,
            ),
        ),
    ),
)
```

Submit the step through `InteractionInput.StepList`. Interactions video results
use the v1beta endpoint already used by this library. See
[multimodal function responses](https://aistudio.google.com/docs/function-calling#multimodal).

### Computer Use (GA)

Computer Use is available through both APIs. For Generate Content, configure the
existing tool DSL with a browser, mobile, or desktop environment:

```kotlin
val request = generateContentRequest {
    content { part { text { "Search for Gemini API documentation." } } }
    tool {
        computerUse {
            environment = Environment.ENVIRONMENT_BROWSER
            enablePromptInjectionDetection = true
        }
    }
    store = false
}
val response = gemini.generateContent(request, model = "gemini-4-hydrogen")
```

For Interactions, use `InteractionTool(type = "computer_use", environment = "browser",
enablePromptInjectionDetection = true)` in `CreateInteractionRequest.tools`.
Use `"mobile"` or `"desktop"` for the other environments. Both tool types expose
`disabledSafetyPolicies`; Generate Content uses uppercase policy names such as
`DATA_MODIFICATION`, while Interactions uses lowercase names such as `data_modification`.
Leaving the option unset retains the API's safety policies.

`ComputerUseSample.run()` demonstrates Interactions and
`ComputerUseSample.runGenerateContent()` demonstrates Generate Content, using
`gemini-4-hydrogen` by default. Both accept a model override. These samples request
actions; applications supply screenshots, execute allowed actions, handle
`safety_decision` confirmations, and return action results. See the
[Computer Use guide](https://aistudio.google.com/docs/computer-use) for supported
models and the action loop.

### Voice remixing for Gemini 3.8 TTS

Create an independent stored voice from a prebuilt voice name, a catalog voice ID,
or an existing designed/remixed `voice_...` ID:

```kotlin
val remixed = geminiAI.createRemixedVoice(
    baseVoice = "Puck",
    instructions = "Make the delivery softer and slightly lower in pitch.",
    displayName = "Soft narrator",
)
val speech = geminiAI.generateSpeech(
    text = "Welcome to the next chapter.",
    voice = remixed.id,
    model = "gemini-3.8-flash-lite-tts",
)
```

Remixing uses `POST /v1beta/voices` with `gemini-3.8-flash-tts` and `store=true`.
It creates a new voice without modifying the source. The returned ID supports both
`gemini-3.8-flash-tts` and `gemini-3.8-flash-lite-tts` for synthesis. The response
includes `sampleAudio` when available, `prompted.baseVoice` and `prompted.input`
for lineage, and `expireTime` for retention metadata. The existing `getVoice`,
`listVoices`, and `deleteVoice` methods also work with remixed voices. See the
[Voice remixing guide](https://aistudio.google.com/docs/voice-remixing).

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
