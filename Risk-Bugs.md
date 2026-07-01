# Risk Bugs (Investigated)

Investigation date: 2026-06-30  
Scope: reviewed the listed risks against current `src/` code. Fixed items have been removed from this document.

Status keys:
- `Confirmed`: behavior exists in current code.
- `Partially confirmed`: risk exists, but original wording/details were outdated.
- `Confirmed in code (API-dependent impact)`: strict model shape is real; runtime break depends on API response variants.

---

4. **Medium - SSE parser too strict / line-based assumptions**  
   Status: `Confirmed`  
   Evidence: [Gemini.kt:211-221](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/Gemini.kt#L211-L221), [GeminiAI.kt:88-95](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/GeminiAI.kt#L88-L95)  
   Finding: `Gemini.streamGenerateContent()` only accepts lines starting with exactly `"data: "` (requires trailing space) and decodes each line independently. `GeminiAI.streamInteraction()` accepts `"data:"` but still processes JSON line-by-line. If a single JSON payload spans multiple lines, or if the server sends multi-line events or additional event metadata, parsing will fail or skip chunks.  
   Recommendation: Parse full Server-Sent Events (SSE) properly by aggregating multi-line data fields and delimiting events on blank lines.

5. **Medium - Live WebSocket queues unbounded; post-handshake decode errors dropped**  
   Status: `Confirmed`  
   Evidence: [GeminiLive.kt:106](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/live/GeminiLive.kt#L106), [LiveMusic.kt:121](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/live/music/LiveMusic.kt#L121), [MessageProcessor.kt:33-38](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/live/MessageProcessor.kt#L33-L38)  
   Finding: Both live WebSocket clients initialize incoming queues with `Channel.UNLIMITED`, introducing memory exhaustion (OOM) risks if incoming packets arrive faster than they are consumed. Furthermore, in `MessageProcessor`, any JSON deserialization exception thrown after the handshake has successfully completed is logged but swallowed, leaving the caller unaware of corrupted payloads.  
   Recommendation: Implement bounded channels with explicit backpressure strategies, and route post-handshake deserialization failures to the session's error flow.

6. **Medium - Non-JVM `getImage()` returns empty sample payload**  
   Status: `Confirmed`  
   Evidence: [Platform.kt:20 (Android)](file:///home/ugai/Gemini4KT/src/androidMain/kotlin/io/github/ugaikit/gemini4kt/Platform.kt#L20), [Platform.kt:28 (JS)](file:///home/ugai/Gemini4KT/src/jsMain/kotlin/io/github/ugaikit/gemini4kt/Platform.kt#L28), [Platform.kt:28 (Wasm)](file:///home/ugai/Gemini4KT/src/wasmJsMain/kotlin/io/github/ugaikit/gemini4kt/Platform.kt#L28), [Platform.kt:47 (Native)](file:///home/ugai/Gemini4KT/src/nativeMain/kotlin/io/github/ugaikit/gemini4kt/Platform.kt#L47), [InputWithImage.kt:25-41](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/samples/InputWithImage.kt#L25-L41), [InteractionSamples.kt:57-69](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/samples/InteractionSamples.kt#L57-L69), [CodeExecutionWithImageSample.kt:21-39](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/samples/CodeExecutionWithImageSample.kt#L21-L39)  
   Finding: On Android, JS, Wasm, and Native targets, `getImage()` is hardcoded to return `""`, causing image-reliant samples to send empty payloads and fail at runtime.  
   Recommendation: Require callers to supply image data explicitly or fail-fast with target limitation errors.

11. **Low - Public builders allow malformed payloads**  
    Status: `Confirmed`  
    Evidence: [GenerateContentRequest.kt:59-157](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/GenerateContentRequest.kt#L59-L157), [InlineData.kt:45-90](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/InlineData.kt#L45-L90), [FileData.kt:33-45](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/FileData.kt#L33-L45)  
    Finding: The builders lack constraints on required properties. E.g., `FileDataBuilder` leaves `mimeType` and `fileUri` as uninitialized `lateinit var`, leading to `UninitializedPropertyAccessException` crashes on build. `InlineDataBuilder` defaults required fields to `""`, resulting in malformed requests at runtime.  
    Recommendation: Add property checks inside `build()` to assert required properties and provide clear error messages.

12. **Medium - Non-JVM uploads buffer full file in memory**  
    Status: `Partially confirmed`  
    Evidence: [FileUploadProvider.kt:129-139,228-237,261-270 (JS)](file:///home/ugai/Gemini4KT/src/jsMain/kotlin/io/github/ugaikit/gemini4kt/FileUploadProvider.kt#L129-L139), [FileUploadProvider.kt:161-170,261-270,294-303 (Wasm)](file:///home/ugai/Gemini4KT/src/wasmJsMain/kotlin/io/github/ugaikit/gemini4kt/FileUploadProvider.kt#L161-L170), [FileUploadProvider.kt:183-190 (Native)](file:///home/ugai/Gemini4KT/src/nativeMain/kotlin/io/github/ugaikit/gemini4kt/FileUploadProvider.kt#L183-L190)  
    Finding: JS and Wasm targets still buffer full files in memory as `ByteArray` via Node `fs.readFileSync(path)`, and Native still reads the whole file with `readByteArray()`. The upload providers now reject files above a configurable size ceiling before reading, which limits the worst-case memory impact but does not eliminate the buffering behavior.  
    Recommendation: Stream file uploads where possible, or keep the size ceiling conservative and document it clearly.

13. **Low - Automatic binding/schema supports narrow type subset**  
    Status: `Confirmed`  
    Evidence: [AutomaticFunctionCallingJvm.kt:118-164](file:///home/ugai/Gemini4KT/src/jvmCommonMain/kotlin/io/github/ugaikit/gemini4kt/AutomaticFunctionCallingJvm.kt#L118-L164), [FunctionDsl.kt:16-38](file:///home/ugai/Gemini4KT/src/jvmCommonMain/kotlin/io/github/ugaikit/gemini4kt/FunctionDsl.kt#L16-L38)  
    Finding: Automatic schemas and reflection binders only support primitives, collections, maps with String keys, and raw JsonElements. Complex types, enums, or nested data structures fail with an `IllegalArgumentException` during function registration.  
    Recommendation: Support enums and nested serialization or document supported signatures.

29. **Low - `ThinkingConfig` always serializes default budget**  
    Status: `Confirmed`  
    Evidence: [ThinkingConfig.kt:21-24](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/ThinkingConfig.kt#L21-L24)  
    Finding: The property `thinkingBudget` is annotated with `@EncodeDefault(EncodeDefault.Mode.ALWAYS)`. This means it will always serialize `"thinking_budget": 1024` even if the user did not set it, overriding server-side defaults and forcing a low token budget.  
    Recommendation: Remove the `ALWAYS` serialization mode or make the budget property nullable.

38. **Low - Live connection setup is duplicated across `GeminiLive` and `LiveMusic`**  
    Status: `Confirmed`  
    Evidence: [GeminiLive.kt:81-170](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/live/GeminiLive.kt#L81-L170), [LiveMusic.kt:95-170](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/live/music/LiveMusic.kt#L95-L170)  
    Finding: Both live clients repeat the same connection choreography: wrap or clone an `HttpClient`, open a WebSocket, create an unbounded incoming channel, launch a listener coroutine, process handshake messages, send the initial setup frame, and unwind resources on failure. The duplication makes the two implementations harder to keep in sync and increases the chance of drift when one side changes error handling or session startup behavior.  
    Recommendation: Extract the shared WebSocket/session lifecycle into a small internal helper and keep only the endpoint-specific setup payloads in the two classes.

39. **Low - Sample entrypoints repeat client acquisition and request scaffolding**  
    Status: `Confirmed`  
    Evidence: [LiveSample.kt:52-160](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/samples/LiveSample.kt#L52-L160), [FileSearchSample.kt:28-80](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/samples/FileSearchSample.kt#L28-L80), [BatchSample.kt:20-87](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/samples/BatchSample.kt#L20-L87), [FileUploadSample.kt:15-58](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/samples/FileUploadSample.kt#L15-L58)  
    Finding: Several sample entrypoints duplicate the same scaffolding: resolve an API key, create a default client when one is not injected, build a one-off request object, run the API call, then print or poll results. This makes the sample code longer than necessary and obscures the actual usage pattern the examples are trying to teach.  
    Recommendation: Add small shared helpers for client creation, request assembly, and common polling/printing flows so the samples read as focused API examples instead of boilerplate.

40. **Low - JS helper methods duplicate single-prompt request/response handling**  
    Status: `Confirmed`  
    Evidence: [GeminiJs.kt:12-39](file:///home/ugai/Gemini4KT/src/jsMain/kotlin/io/github/ugaikit/gemini4kt/GeminiJs.kt#L12-L39), [GeminiJs.kt:49-79](file:///home/ugai/Gemini4KT/src/jsMain/kotlin/io/github/ugaikit/gemini4kt/GeminiJs.kt#L49-L79), [GeminiJs.kt:91-130](file:///home/ugai/Gemini4KT/src/jsMain/kotlin/io/github/ugaikit/gemini4kt/GeminiJs.kt#L91-L130)  
    Finding: The JS surface builds nearly identical single-prompt requests and then repeats the same first-candidate/first-part text extraction in multiple entry points. That duplication is mostly harmless at runtime, but it makes the exported API harder to maintain and spreads the same response-shaping logic across three places.  
    Recommendation: Route the exported JS helpers through a shared internal function that constructs the prompt request once and centralizes the “return the first text part” logic.

42. **Low - HTTP client setup repeats the same Ktor plugin wiring on every target**  
    Status: `Confirmed`  
    Evidence: [HttpClient.kt:15-23 (JVM)](file:///home/ugai/Gemini4KT/src/jvmMain/kotlin/io/github/ugaikit/gemini4kt/HttpClient.kt#L15-L23), [HttpClient.kt:15-23 (Android)](file:///home/ugai/Gemini4KT/src/androidMain/kotlin/io/github/ugaikit/gemini4kt/HttpClient.kt#L15-L23), [HttpClient.kt:15-23 (JS)](file:///home/ugai/Gemini4KT/src/jsMain/kotlin/io/github/ugaikit/gemini4kt/HttpClient.kt#L15-L23), [HttpClient.kt:15-23 (Wasm)](file:///home/ugai/Gemini4KT/src/wasmJsMain/kotlin/io/github/ugaikit/gemini4kt/HttpClient.kt#L15-L23), [Platform.kt:22-30 (Native)](file:///home/ugai/Gemini4KT/src/nativeMain/kotlin/io/github/ugaikit/gemini4kt/Platform.kt#L22-L30)  
    Finding: Each platform-specific `createHttpClient` implementation repeats the same `ContentNegotiation` JSON setup, and several targets also repeat the same timeout/logging defaults. The only real variation is the engine choice, so the current layout spreads a small but important configuration surface across five files.  
    Recommendation: Factor the shared plugin wiring into a helper such as `configureGeminiHttpClient`, then keep only the engine constructor in each `actual`.

43. **Low - Platform test shims duplicate a one-line reflection flag**  
    Status: `Confirmed`  
    Evidence: [PlatformTestUtils.kt:1-6 (JVM)](file:///home/ugai/Gemini4KT/src/jvmTest/kotlin/io/github/ugaikit/gemini4kt/samples/PlatformTestUtils.kt#L1-L6), [PlatformTestUtils.kt:1-6 (JS)](file:///home/ugai/Gemini4KT/src/jsTest/kotlin/io/github/ugaikit/gemini4kt/samples/PlatformTestUtils.kt#L1-L6), [PlatformTestUtils.kt:1-6 (Wasm)](file:///home/ugai/Gemini4KT/src/wasmJsTest/kotlin/io/github/ugaikit/gemini4kt/samples/PlatformTestUtils.kt#L1-L6), [PlatformTestUtils.kt:1-6 (Native)](file:///home/ugai/Gemini4KT/src/nativeTest/kotlin/io/github/ugaikit/gemini4kt/samples/PlatformTestUtils.kt#L1-L6), [PlatformTestUtils.kt:1-6 (Android device)](file:///home/ugai/Gemini4KT/src/androidDeviceTest/kotlin/io/github/ugaikit/gemini4kt/samples/PlatformTestUtils.kt#L1-L6), [PlatformTestUtils.kt:1-6 (Android host)](file:///home/ugai/Gemini4KT/src/androidHostTest/kotlin/io/github/ugaikit/gemini4kt/samples/PlatformTestUtils.kt#L1-L6)  
    Finding: Six test source sets each define the same file header and a single `supportsReflection` boolean literal. The duplication is small, but it adds unnecessary maintenance if more platform capability flags are introduced later.  
    Recommendation: Keep the target-specific `actual` declarations, but move the capability decision into a shared source-set constant or a more compact source-set hierarchy so the flag is declared in one place.

---

## Investigation Summary

- Confirmed: 9
- Partially confirmed: 1 (`#12`)
- Disproved: 0
- Confirmed in code but API-response-dependent impact: 0

## Most Actionable First Fixes

1. Upload robustness (`#12`)
2. Remaining low-priority builder and sample cleanup items (`#11`, `#13`, `#29`, `#38`, `#39`, `#40`, `#42`, `#43`)
