# Risk Bugs (Investigated)

Investigation date: 2026-07-02  
Scope: reviewed the listed risks against current `src/` code. Fixed items have been removed from this document.

Status keys:
- `Confirmed`: behavior exists in current code.
- `Partially confirmed`: risk exists, but original wording/details were outdated.
- `Confirmed in code (API-dependent impact)`: strict model shape is real; runtime break depends on API response variants.

---

12. **Medium - Non-JVM uploads buffer full file in memory**  
    Status: `Partially confirmed`  
    Evidence: [FileUploadProvider.kt:129-139,228-237,261-270 (JS)](file:///home/ugai/Gemini4KT/src/jsMain/kotlin/io/github/ugaikit/gemini4kt/FileUploadProvider.kt#L129-L139), [FileUploadProvider.kt:161-170,261-270,294-303 (Wasm)](file:///home/ugai/Gemini4KT/src/wasmJsMain/kotlin/io/github/ugaikit/gemini4kt/FileUploadProvider.kt#L161-L170), [FileUploadProvider.kt:183-190 (Native)](file:///home/ugai/Gemini4KT/src/nativeMain/kotlin/io/github/ugaikit/gemini4kt/FileUploadProvider.kt#L183-L190)  
    Finding: JS and Wasm targets still buffer full files in memory as `ByteArray` via Node `fs.readFileSync(path)`, and Native still reads the whole file with `readByteArray()`. The upload providers now reject files above a configurable size ceiling before reading, which limits the worst-case memory impact but does not eliminate the buffering behavior.  
    Recommendation: Stream file uploads where possible, or keep the size ceiling conservative and document it clearly.

13. **Medium - Model names are interpolated raw into request paths**  
    Status: `Confirmed in code (API-dependent impact)`  
    Evidence: [Gemini.kt:151](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/Gemini.kt#L151), [Gemini.kt:169](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/Gemini.kt#L169), [Gemini.kt:273](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/Gemini.kt#L273), [Gemini.kt:289](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/Gemini.kt#L289), [Gemini.kt:305](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/Gemini.kt#L305)  
    Finding: `generateContent`, `streamGenerateContent`, `countTokens`, `batchEmbedContents`, and `embedContent` all splice `model` directly into the URL path. If a caller passes a resource name like `models/gemma-4-31b-it`, the request becomes `.../models/models/gemma-4-31b-it:...` and targets the wrong endpoint. The code currently relies on callers using only short model IDs, but that convention is not enforced.  
    Recommendation: Normalize model identifiers before path construction or build the URL with explicit path segments so both short IDs and resource names work consistently.

14. **Medium - Live message payloads are logged verbatim**  
    Status: `Confirmed`  
    Evidence: [LiveSample.kt:86-99](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/samples/LiveSample.kt#L86), [MessageProcessor.kt:23-29](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/live/MessageProcessor.kt#L23-L29)  
    Finding: The live sample prints every decoded WebSocket message, and the handshake helper warns with the full message object when setup is not complete. Those payloads can contain user prompts, model outputs, tool calls, or audio-transcription data, which makes logs a durable exfiltration point in long-lived apps or shared CI output.  
    Recommendation: Log message metadata only, or redact payload contents before printing them.

15. **Low - Samples leak owned Gemini clients when callers do not supply one**  
    Status: `Confirmed`  
    Evidence: [FunctionExample1.kt:19-40](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/samples/FunctionExample1.kt#L19), [CountTokensSample.kt:16-20](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/samples/CountTokensSample.kt#L16), [GoogleSearchSample.kt:16-35](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/samples/GoogleSearchSample.kt#L16), [Cache.kt:27-68](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/samples/Cache.kt#L27)  
    Finding: Several sample entrypoints instantiate `Gemini(getApiKey())` when the caller passes `null`, but they never call `close()` on the owned client. Because `Gemini` owns an `HttpClient` by default, repeated sample runs leak sockets and other HTTP resources until process exit.  
    Recommendation: Wrap those sample bodies in `withGeminiClient(...)` or close the locally created client in `finally`.

16. **Medium - Interaction samples log raw payloads and tool results**  
    Status: `Confirmed`  
    Evidence: [InteractionSamples.kt:172-215](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/samples/InteractionSamples.kt#L172), [InteractionSamples.kt:220-280](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/samples/InteractionSamples.kt#L220)  
    Finding: The interaction sample prints full interaction metadata, generated text, function-call arguments, code-execution results, and fallback `toString()` output for unrecognized content. Those payloads can carry user prompts, model responses, tool inputs, and other sensitive data, so the sample becomes a durable log sink if copied into real applications or CI jobs.  
    Recommendation: Redact payload contents, or log only metadata and counts in the sample output.

17. **Medium - `InteractionGenerationConfig.speechConfig` uses an array where the surrounding API uses a single `SpeechConfig`**  
    Status: `Confirmed in code (API-dependent impact)`  
    Evidence: [Interaction.kt:345-361](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/interaction/Interaction.kt#L345), [GenerationConfig.kt:23-50](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/GenerationConfig.kt#L23), [LiveTypes.kt:365-383](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/live/LiveTypes.kt#L365)  
    Finding: `InteractionGenerationConfig` serializes `speech_config` as `Array<SpeechConfig>?`, but the rest of the SDK models speech generation as a single `SpeechConfig?`. If the Interaction API expects the same object shape, this field will emit the wrong JSON type and the server will reject or ignore the configuration.  
    Recommendation: Verify the Interaction API schema and, if it matches the rest of the SDK, change the field to a single `SpeechConfig?` and add a round-trip test.

18. **Medium - Live Music forwards the API key to whatever WebSocket endpoint the caller constructs**  
    Status: `Confirmed`  
    Evidence: [LiveMusic.kt:26-46](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/live/music/LiveMusic.kt#L26-L46), [LiveConnection.kt:61-65](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/live/LiveConnection.kt#L61-L65)  
    Finding: `LiveMusicOptions.baseUrl` and `apiVersion` are concatenated directly into the WebSocket URL, and `openLiveConnection` always adds `x-goog-api-key` to the request. That is fine for the default Google endpoint, but if an application threads untrusted config or user input into `LiveMusicOptions`, the SDK will send the API key to an attacker-controlled host or path.  
    Recommendation: Restrict `baseUrl` to trusted hosts, validate `apiVersion` against an allowlist, or make the custom-endpoint feature opt-in and clearly documented as trusted-only.

19. **Medium - Listener parse failures after handshake leave a dead session open**  
    Status: `Confirmed`  
    Evidence: [LiveConnection.kt:94-108](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/live/LiveConnection.kt#L94-L108)  
    Finding: If `processHandshakeMessage` throws after `setupComplete` has already been observed, the listener job logs the error and closes `incomingMessages`, but it does not close the WebSocket session or the owned `HttpClient`. The caller ends up with a `LiveConnection` whose `receive()` flow is dead while the socket and engine stay alive until someone explicitly closes the session.  
    Recommendation: Treat post-handshake decode failures as connection-fatal, close the session in the catch block, and propagate the failure to the owner so it can reconnect cleanly.

20. **Low - FileSearch creates a separate upload client and never closes it**  
    Status: `Confirmed`  
    Evidence: [FileSearch.kt:26-29,43,215-218](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/filesearch/FileSearch.kt#L26-L29), [FileUploadProvider.kt:12-17,31-49](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/FileUploadProvider.kt#L12-L17)  
    Finding: `FileSearch` always constructs `FileUploadProvider(apiKey)` with its own internal `HttpClient`, even when the caller supplies a shared client for the main `FileSearch` calls. `FileSearch.close()` only closes the primary client, so upload requests bypass the caller's transport configuration and the upload client stays alive until process exit.  
    Recommendation: Thread the caller's client into `FileUploadProvider`, or have `FileSearch.close()` close the owned upload provider too.

21. **Medium - FileSearch builds resource URLs and pagination queries by string concatenation**  
    Status: `Confirmed`  
    Evidence: [FileSearch.kt:74-77,88-99,111-116,126-130,158-159](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/filesearch/FileSearch.kt#L74-L77), [FileUploadProvider.kt:82-95](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/FileUploadProvider.kt#L82-L95)  
    Finding: `name`, `fileSearchStoreName`, and `pageToken` are interpolated directly into URL paths and query strings. That works for simple identifiers, but opaque pagination tokens and caller-supplied resource names can contain reserved characters, which can corrupt the request or splice in extra path/query components.  
    Recommendation: Build the request URI with encoded path/query parameters instead of string concatenation.

22. **Medium - Batch request URLs and pagination tokens are concatenated without encoding**  
    Status: `Confirmed`  
    Evidence: [Batch.kt:64-68,80-94,102-118,131-141](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/batch/Batch.kt#L64-L68), [BatchSample.kt:56-91](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/samples/BatchSample.kt#L56-L91)  
    Finding: `createBatch`, `getBatch`, `cancelBatch`, `deleteBatch`, `createBatchEmbeddings`, and `listBatches` all build URLs with raw string interpolation. That makes the client sensitive to reserved characters in model names, batch resource names, and `pageToken`, which can produce malformed requests or target the wrong endpoint when callers pass fully qualified resource names or opaque tokens.  
    Recommendation: Build batch URLs with encoded path segments and query parameters, and normalize model identifiers before appending RPC suffixes.

23. **Medium - Agent endpoints interpolate IDs and environment names directly into paths**  
    Status: `Confirmed`  
    Evidence: [GeminiAI.kt:150-206](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/GeminiAI.kt#L150-L206)  
    Finding: `downloadEnvironmentFiles`, `getAgent`, and `deleteAgent` splice caller-provided `envId` and `id` values directly into the request path. If a caller passes a fully qualified resource name or a value containing reserved characters, the SDK can generate malformed URLs or hit the wrong resource path.  
    Recommendation: Encode path segments with a URL builder instead of concatenating raw identifiers into the path.

24. **Medium - Error handling exposes full server bodies in thrown exceptions**  
    Status: `Confirmed`  
    Evidence: [Errors.kt:15-33](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/Errors.kt#L15-L33), [ResumableUpload.kt:44-45,71-72](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/ResumableUpload.kt#L44-L45)  
    Finding: When an API response is not valid Gemini JSON, `throwApiException()` and the resumable upload helpers embed the raw response body directly into the exception message. That can leak sensitive server-side details, echoed request data, or HTML error pages into logs, crash reports, or telemetry, and it also allows very large responses to be copied into memory and exception strings.  
    Recommendation: Limit surfaced error text to a bounded summary, and keep the raw body only in debug logging behind an explicit opt-in.

25. **Medium - JVM clients enforce a hard 60-second request timeout by default**  
    Status: `Confirmed`  
    Evidence: [HttpClient.kt:12-14](file:///home/ugai/Gemini4KT/src/jvmMain/kotlin/io/github/ugaikit/gemini4kt/HttpClient.kt#L12-L14), [HttpClientConfig.kt:14-25](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/HttpClientConfig.kt#L14-L25)  
    Finding: The JVM default `HttpClient` installs `HttpTimeout` with `requestTimeoutMillis = 60_000`. That makes every default JVM request fail after one minute, including slow generations, large uploads, and long-running streaming workflows that are otherwise supported by the API surface.  
    Recommendation: Make the timeout configurable or disable it for streaming-capable clients, and document the default only for short-lived unary calls.

26. **Low - JVM music sample buffers all generated PCM in memory before writing output**  
    Status: `Confirmed`  
    Evidence: [MusicGenerationRunner.kt:18-24,37-50](file:///home/ugai/Gemini4KT/src/jvmMain/kotlin/io/github/ugaikit/gemini4kt/samples/MusicGenerationRunner.kt#L18-L24), [MusicGenerationRunner.kt:41-50](file:///home/ugai/Gemini4KT/src/jvmMain/kotlin/io/github/ugaikit/gemini4kt/samples/MusicGenerationRunner.kt#L41-L50)  
    Finding: The JVM music runner appends every decoded audio chunk to a single `ByteArrayOutputStream` and only writes the WAV file after the session ends. For longer runs this can grow without bound and trigger high heap usage or OOM in the sample runner.  
    Recommendation: Stream audio to disk incrementally or cap the accumulated buffer size.

27. **Low - JS sample launcher prints and naively splits CLI arguments from the environment**  
    Status: `Confirmed`  
    Evidence: [Models.kt:8-19,50-53](file:///home/ugai/Gemini4KT/src/jsMain/kotlin/io/github/ugaikit/gemini4kt/samples/Models.kt#L8-L19)  
    Finding: The JS `main()` function echoes the resolved argument list to stdout and reads `CLI_ARGS` by splitting on spaces. That can leak values supplied through the environment into logs, and it breaks quoted arguments or any target name that itself contains whitespace.  
    Recommendation: Avoid printing the raw argument vector, and parse `CLI_ARGS` with shell-like quoting rules or a structured format instead of a plain space split.

28. **Medium - JS default clients have no request timeout at all**  
    Status: `Confirmed`  
    Evidence: [HttpClient.kt:8-14](file:///home/ugai/Gemini4KT/src/jsMain/kotlin/io/github/ugaikit/gemini4kt/HttpClient.kt#L8-L14), [HttpClientConfig.kt:14-25](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/HttpClientConfig.kt#L14-L25)  
    Finding: The JS `createHttpClient()` path does not install `HttpTimeout`, unlike the JVM client. A stalled network call or a hung streaming response can therefore keep the request open indefinitely, which is an availability problem for both browser and Node consumers.  
    Recommendation: Add a configurable timeout for JS clients, or document clearly that callers must wrap requests with their own cancellation and timeout logic.

29. **Low - Batch and FileSearch sample polls can hang forever when the API never reaches a terminal state**  
    Status: `Confirmed`  
    Evidence: [BatchSample.kt:61-74](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/samples/BatchSample.kt#L61-L74), [FileSearchSample.kt:111-118](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/samples/FileSearchSample.kt#L111-L118)  
    Finding: Both samples poll in an unbounded loop until the remote operation reports a terminal state. If the backend stalls, returns an unexpected status, or the tests only cover the success path, the sample never exits and the caller hangs indefinitely. This is an availability bug and a coverage gap because the current tests do not exercise the timeout or stuck-operation path.  
    Recommendation: Add a maximum retry count or elapsed-time deadline, and fail closed when the operation does not finish in time.

30. **Low - FileUploadSample hides failures and echoes exception details instead of propagating them**  
    Status: `Confirmed`  
    Evidence: [FileUploadSample.kt:25-64](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/samples/FileUploadSample.kt#L25-L64)  
    Finding: The sample wraps the entire flow in `catch (Exception)` and only prints the message and exception object before returning normally. That masks upload and generation failures from callers and tests, and it can still leak raw response details or local file-path information to stdout.  
    Recommendation: Let the failure propagate, or rethrow after emitting a bounded, redacted log line.

31. **Low - `MusicGenerationTest` is a placeholder that never exercises the sample flow**  
    Status: `Confirmed`  
    Evidence: [MusicGenerationTest.kt:10-22](file:///home/ugai/Gemini4KT/src/commonTest/kotlin/io/github/ugaikit/gemini4kt/samples/MusicGenerationTest.kt#L10-L22)  
    Finding: The test body is explicitly a placeholder and ends with `assertTrue(true)`, so it does not execute `MusicGeneration.run()` or verify any of the live-music lifecycle, stop, close, or error-handling behavior. That leaves the sample’s long-running receive loop and teardown path effectively uncovered, which creates false confidence in coverage.  
    Recommendation: Replace the placeholder with a mocked-session test that drives the sample through playback, shutdown, and failure cases.

32. **Low - Sample test fixtures leak mock HttpClient instances and never assert cleanup**  
    Status: `Confirmed`  
    Evidence: [SamplesTest.kt:24-34](file:///home/ugai/Gemini4KT/src/commonTest/kotlin/io/github/ugaikit/gemini4kt/samples/SamplesTest.kt#L24-L34), [ModelsTest.kt:44-56](file:///home/ugai/Gemini4KT/src/commonTest/kotlin/io/github/ugaikit/gemini4kt/samples/ModelsTest.kt#L44-L56), [GeminiAITest.kt:35-52](file:///home/ugai/Gemini4KT/src/commonTest/kotlin/io/github/ugaikit/gemini4kt/GeminiAITest.kt#L35-L52), [BatchClientTest.kt:38-48](file:///home/ugai/Gemini4KT/src/commonTest/kotlin/io/github/ugaikit/gemini4kt/batch/BatchClientTest.kt#L38-L48), [FileSearchTest.kt:45-55](file:///home/ugai/Gemini4KT/src/commonTest/kotlin/io/github/ugaikit/gemini4kt/filesearch/FileSearchTest.kt#L45-L55)  
    Finding: These helpers build `HttpClient(MockEngine)` instances for use in tests, but the clients are never closed after the assertions finish. That leaks resources during repeated test runs and, more importantly, means the suite does not verify that owned-vs-unowned client teardown works the way the production code expects.  
    Recommendation: Close the mock clients in `finally` blocks or test teardown, and add explicit teardown assertions where lifecycle behavior matters.

33. **Low - `GeminiLiveSessionTest` should assert the actual close frame, not just cancellation flags**  
    Status: `Confirmed`  
    Evidence: [GeminiLiveSessionTest.kt:187-201](file:///home/ugai/Gemini4KT/src/commonTest/kotlin/io/github/ugaikit/gemini4kt/live/GeminiLiveSessionTest.kt#L187-L201)  
    Finding: The current close test only checks `job.isCancelled` and `incoming.isClosedForSend`, while the `mockSession.outgoing` close frame is read and then ignored. That leaves the wire-level shutdown behavior unverified, so regressions in the WebSocket close handshake could still pass the suite.  
    Recommendation: Add an assertion on the outgoing close frame, or otherwise verify that the session emits the expected close control frame before completing.

34. **Low - Lifecycle tests need owned-client coverage, not only unowned-client coverage**  
    Status: `Confirmed`  
    Evidence: [GeminiAILifecycleTest.kt:35-43](file:///home/ugai/Gemini4KT/src/commonTest/kotlin/io/github/ugaikit/gemini4kt/GeminiAILifecycleTest.kt#L35-L43), [BatchLifecycleTest.kt:36-45](file:///home/ugai/Gemini4KT/src/commonTest/kotlin/io/github/ugaikit/gemini4kt/batch/BatchLifecycleTest.kt#L36-L45), [FileSearchLifecycleTest.kt:35-43](file:///home/ugai/Gemini4KT/src/commonTest/kotlin/io/github/ugaikit/gemini4kt/filesearch/FileSearchLifecycleTest.kt#L35-L43)  
    Finding: These lifecycle tests only prove that `close()` does not shut down a caller-supplied `HttpClient`. They do not prove the inverse path, where the SDK owns the client and must close it. That leaves resource cleanup untested for the code path most likely to leak sockets in real use.  
    Recommendation: Add companion tests that construct owned clients and assert that `close()` completes the underlying `HttpClient`.

35. **Low - `FunctionCallingConfigBuilder` still needs explicit coverage for the remaining mode branches**  
    Status: `Confirmed`  
    Evidence: [FunctionCallingConfig.kt:53-66](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/FunctionCallingConfig.kt#L53-L66), [FunctionCallingConfigBuilderTest.kt:8-37](file:///home/ugai/Gemini4KT/src/commonTest/kotlin/io/github/ugaikit/gemini4kt/FunctionCallingConfigBuilderTest.kt#L8-L37)  
    Finding: The builder tests cover `Mode.ANY` and `Mode.AUTO`, but not the `VALIDATED`, `NONE`, or `MODE_UNSPECIFIED` branches that drive different validation rules. Without those tests, a regression in the allowlist logic for the remaining modes could slip through undetected.  
    Recommendation: Add tests for the remaining mode families, including the allowed-function and empty-allowlist failure cases.

36. **Low - `SpeechConfig` should be tested for blank voice and speaker names, not only missing builders**  
    Status: `Confirmed`  
    Evidence: [SpeechConfig.kt:169-175](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/SpeechConfig.kt#L169-L175), [SpeechConfig.kt:245-255](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/SpeechConfig.kt#L245-L255), [SpeechConfigTest.kt:180-205](file:///home/ugai/Gemini4KT/src/commonTest/kotlin/io/github/ugaikit/gemini4kt/SpeechConfigTest.kt#L180-L205)  
    Finding: The current tests prove that the builders reject empty state, but they never exercise the `isNullOrBlank()` guards. A blank `voiceName` or `speaker` value would therefore bypass the suite even though the production code rejects it.  
    Recommendation: Add tests that set `voiceName = ""` and `speaker = ""` and assert the expected `IllegalArgumentException` messages.

37. **Low - `PartBuilder` should cover the `videoMetadata` precondition failure path**  
    Status: `Confirmed`  
    Evidence: [Part.kt:270-287](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/Part.kt#L270-L287), [PartTest.kt:143-166](file:///home/ugai/Gemini4KT/src/commonTest/kotlin/io/github/ugaikit/gemini4kt/PartTest.kt#L143-L166)  
    Finding: The tests cover the valid `fileData + videoMetadata` path, but not the guard that requires `videoMetadata` to be paired with either `inlineData` or `fileData`. That missing case leaves an important DSL invariant untested.  
    Recommendation: Add a test that builds a `Part` with only `videoMetadata` and asserts the builder throws the documented exception.

---

## Investigation Summary

- Confirmed: 23 (`#14`, `#15`, `#16`, `#18`, `#19`, `#20`, `#21`, `#22`, `#23`, `#24`, `#25`, `#26`, `#27`, `#28`, `#29`, `#30`, `#31`, `#32`, `#33`, `#34`, `#35`, `#36`, `#37`)
- Partially confirmed: 1 (`#12`)
- Disproved: 0
- Confirmed in code but API-response-dependent impact: 2 (`#13`, `#17`)

## Most Actionable First Fixes

1. Live logging redaction (`#14`, `#16`)
2. Sample client lifecycle cleanup (`#15`)
3. Model path normalization (`#13`)
4. Interaction speech config shape (`#17`)
5. Live Music endpoint trust boundary (`#18`)
6. Listener failure cleanup (`#19`)
7. FileSearch client sharing/cleanup (`#20`)
8. FileSearch URL encoding (`#21`)
9. Batch URL encoding (`#22`)
10. Agent URL encoding (`#23`)
11. Error-body exposure (`#24`)
12. JVM timeout default (`#25`)
13. JVM music sample buffering (`#26`)
14. JS CLI arg leakage/parsing (`#27`)
15. JS timeout absence (`#28`)
16. Sample polling loop timeout (`#29`)
17. Sample failure masking (`#30`)
18. Placeholder music sample test (`#31`)
19. Mock client cleanup in tests (`#32`)
20. Live session close-frame assertion (`#33`)
21. Owned-client lifecycle coverage (`#34`)
22. Function-calling mode coverage (`#35`)
23. Blank speech-config validation coverage (`#36`)
24. Part `videoMetadata` guard coverage (`#37`)
