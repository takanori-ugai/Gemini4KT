# Risk Bugs (Investigated)

Investigation date: 2026-06-30  
Scope: reviewed all 30 listed risks against current `src/` code. Fixed items have been removed from this document.

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

7. **Low - Automatic binding rejects member functions; message implies support**  
   Status: `Confirmed`  
   Evidence: [AutomaticFunctionCallingJvm.kt:71-73](file:///home/ugai/Gemini4KT/src/jvmCommonMain/kotlin/io/github/ugaikit/gemini4kt/AutomaticFunctionCallingJvm.kt#L71-L73), [AutomaticFunctionCallingJvmTest.kt:70-88](file:///home/ugai/Gemini4KT/src/jvmTest/kotlin/io/github/ugaikit/gemini4kt/AutomaticFunctionCallingJvmTest.kt#L70-L88)  
   Finding: The binding logic checks `require(function.instanceParameter == null)` and throws an error saying only top-level or bound functions are supported. However, class member functions cannot be registered even if bound, and the message confuses developers.  
   Recommendation: Add class instance binding support or rewrite the exception message to state that member functions are unsupported.

11. **Low - Public builders allow malformed payloads**  
    Status: `Confirmed`  
    Evidence: [GenerateContentRequest.kt:59-157](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/GenerateContentRequest.kt#L59-L157), [InlineData.kt:45-90](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/InlineData.kt#L45-L90), [FileData.kt:33-45](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/FileData.kt#L33-L45)  
    Finding: The builders lack constraints on required properties. E.g., `FileDataBuilder` leaves `mimeType` and `fileUri` as uninitialized `lateinit var`, leading to `UninitializedPropertyAccessException` crashes on build. `InlineDataBuilder` defaults required fields to `""`, resulting in malformed requests at runtime.  
    Recommendation: Add property checks inside `build()` to assert required properties and provide clear error messages.

12. **Medium - Non-JVM uploads buffer full file in memory**  
    Status: `Partially confirmed`  
    Evidence: [FileUploadProvider.kt:129-139,228-237,261-270 (JS)](file:///home/ugai/Gemini4KT/src/jsMain/kotlin/io/github/ugaikit/gemini4kt/FileUploadProvider.kt#L129-L139), [FileUploadProvider.kt:161-170,261-270,294-303 (Wasm)](file:///home/ugai/Gemini4KT/src/wasmJsMain/kotlin/io/github/ugaikit/gemini4kt/FileUploadProvider.kt#L161-L170), [FileUploadProvider.kt:183-190 (Native)](file:///home/ugai/Gemini4KT/src/nativeMain/kotlin/io/github/ugaikit/gemini4kt/FileUploadProvider.kt#L183-L190)  
    Finding: JS and Wasm targets buffer full files in memory as `ByteArray` via Node `fs.readFileSync(path)`. The risk is partially confirmed because it also affects the Native target (Okio `fs.source(file).buffered().readByteArray()`), making it a platform-wide issue for non-JVM environments.  
    Recommendation: Stream file uploads where possible, or document and enforce maximum file size limits.

13. **Low - Automatic binding/schema supports narrow type subset**  
    Status: `Confirmed`  
    Evidence: [AutomaticFunctionCallingJvm.kt:118-164](file:///home/ugai/Gemini4KT/src/jvmCommonMain/kotlin/io/github/ugaikit/gemini4kt/AutomaticFunctionCallingJvm.kt#L118-L164), [FunctionDsl.kt:16-38](file:///home/ugai/Gemini4KT/src/jvmCommonMain/kotlin/io/github/ugaikit/gemini4kt/FunctionDsl.kt#L16-L38)  
    Finding: Automatic schemas and reflection binders only support primitives, collections, maps with String keys, and raw JsonElements. Complex types, enums, or nested data structures fail with an `IllegalArgumentException` during function registration.  
    Recommendation: Support enums and nested serialization or document supported signatures.

14. **High - `Tool` one-of protocol shape not enforced in builder**  
    Status: `Partially confirmed`  
    Evidence: [Tool.kt:46-190](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/Tool.kt#L46-L190)  
    Finding: `ToolBuilder` still allows multiple or zero tool configuration blocks, so the API can receive invalid mutually exclusive tool combinations. The `Part`, `SpeechConfig`, and `AttributionSourceId` builders now enforce their one-of invariants, so the original finding is narrower than the note implied.  
    Recommendation: Add validation to `ToolBuilder.build()` and reject invalid combinations before serialization.

15. **Medium - `GenerateContentResponse` strictness + first-candidate helpers**  
    Status: `Confirmed in code (API-dependent impact)`  
    Evidence: [GenerateContentResponse.kt:22-29](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/GenerateContentResponse.kt#L22-L29), [GenerateContentResponse.kt:33-50](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/GenerateContentResponse.kt#L33-L50)  
    Finding: `candidates` is non-nullable and required. If the server response omits the candidates field (e.g. if the entire response is blocked due to safety), Kotlinx Serialization fails to deserialize it. The convenience methods `getText()` and `getThought()` only process the first part of the first candidate.  
    Recommendation: Make `candidates` nullable or default to an empty list; expose helpers to inspect all candidates.

16. **Medium - Batch DSL only models inline request mode**  
    Status: `Confirmed`  
    Evidence: [BatchInputConfig.kt:14-20](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/batch/BatchInputConfig.kt#L14-L20), [BatchDsl.kt:92-113](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/batch/BatchDsl.kt#L92-L113)  
    Finding: `BatchInputConfigBuilder` lacks properties and methods for `gcsSource` or `fileName` fields, rendering the DSL incapable of building configurations that rely on Google Cloud Storage or the File API. It also fails to validate exclusivity rules.  
    Recommendation: Expand `BatchInputConfigBuilder` with methods for GCS and File API, and validate input mode exclusivity.

17. **High - Automatic function binding breaks on `Unit` returns**  
    Status: `Confirmed`  
    Evidence: [AutomaticFunctionCallingJvm.kt:95-105](file:///home/ugai/Gemini4KT/src/jvmCommonMain/kotlin/io/github/ugaikit/gemini4kt/AutomaticFunctionCallingJvm.kt#L95-L105), [AutomaticFunctionCallingJvm.kt:184-210](file:///home/ugai/Gemini4KT/src/jvmCommonMain/kotlin/io/github/ugaikit/gemini4kt/AutomaticFunctionCallingJvm.kt#L184-L210)  
    Finding: If a registered function has a return type of `Unit`, it returns the `kotlin.Unit` object. Since `anyToJsonElement` does not expect `Unit` in its pattern match, it triggers the fallback `else` branch and throws `IllegalArgumentException: Unsupported return type for automatic binding: class kotlin.Unit`, crashing the tool call execution.  
    Recommendation: Explicitly match `Unit` or null in `anyToJsonElement` and return an empty or acknowledged tool result.

18. **High - Extension functions accepted but not invokable**  
    Status: `Confirmed`  
    Evidence: [AutomaticFunctionCallingJvm.kt:71-76](file:///home/ugai/Gemini4KT/src/jvmCommonMain/kotlin/io/github/ugaikit/gemini4kt/AutomaticFunctionCallingJvm.kt#L71-L76), [AutomaticFunctionCallingJvm.kt:94](file:///home/ugai/Gemini4KT/src/jvmCommonMain/kotlin/io/github/ugaikit/gemini4kt/AutomaticFunctionCallingJvm.kt#L94)  
    Finding: The registration logic checks for `instanceParameter == null` but fails to check for `extensionReceiverParameter`. Extension functions are accepted, but invoking them throws `IllegalArgumentException` in `callSuspendBy` because the extension receiver parameter is never supplied in the arguments map.  
    Recommendation: Reject extension functions during binding registration or define a contract to supply the receiver.

19. **Medium - Map schema under-specified vs runtime binder**  
    Status: `Confirmed`  
    Evidence: [FunctionDsl.kt:29-36](file:///home/ugai/Gemini4KT/src/jvmCommonMain/kotlin/io/github/ugaikit/gemini4kt/FunctionDsl.kt#L29-L36), [AutomaticFunctionCallingJvm.kt:139-155](file:///home/ugai/Gemini4KT/src/jvmCommonMain/kotlin/io/github/ugaikit/gemini4kt/AutomaticFunctionCallingJvm.kt#L139-L155)  
    Finding: A `Map` parameter is serialized to the Gemini API as `Schema(type = "object")` with no property or type specifications. However, the runtime binder attempts to deserialize map values recursively into the declared Map value type. If the model returns unexpected types or malformed structures (which it is free to do as the schema is unrestricted), deserialization fails.  
    Recommendation: Generate an `additionalProperties` schema for maps, or enforce runtime resilience when parsing.

22. **Medium - JVM sample entrypoints duplicate unsafe property loading**  
    Status: `Confirmed`  
    Evidence: [TextToImage.kt:79-85](file:///home/ugai/Gemini4KT/src/jvmMain/kotlin/io/github/ugaikit/gemini4kt/samples/TextToImage.kt#L79-L85), [ITTest.kt:874-882](file:///home/ugai/Gemini4KT/src/jvmMain/kotlin/io/github/ugaikit/gemini4kt/ITTest.kt#L874-L882)  
    Finding: Multiple main entry points duplicate the logic to load `/prop.properties` and call `.use` on a potentially null stream. If the property file does not exist, they fail immediately with a NullPointerException before checking the system environment variable or fallback keys.  
    Recommendation: Standardize API key resolution in samples using a shared helper that falls back safely.

24. **High - JS upload path assumes CommonJS `require('fs')`**  
    Status: `Confirmed`  
    Evidence: [FileUploadProvider.kt:53-59 (JS)](file:///home/ugai/Gemini4KT/src/jsMain/kotlin/io/github/ugaikit/gemini4kt/FileUploadProvider.kt#L53-L59), [FileUploadProvider.kt:33-48 (Wasm)](file:///home/ugai/Gemini4KT/src/wasmJsMain/kotlin/io/github/ugaikit/gemini4kt/FileUploadProvider.kt#L33-L48)  
    Finding: The JS implementation uses `js("require('fs')")` and Wasm uses `@kotlin.js.JsModule("fs")`. These statements assume a Node.js CommonJS environment and fail in ESM environments, browsers, or standard Kotlin JS web targets.  
    Recommendation: Separate Node.js-specific upload logic and fail fast with meaningful error messages when running in browsers.

25. **Medium - JS function-calling APIs compile but throw at runtime**  
    Status: `Confirmed`  
    Evidence: [Gemini.kt:111-120](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/Gemini.kt#L111-L120), [AutomaticFunctionBinding.kt:5-8 (JS)](file:///home/ugai/Gemini4KT/src/jsMain/kotlin/io/github/ugaikit/gemini4kt/AutomaticFunctionBinding.kt#L5-L8), [FunctionDsl.kt:10-13 (JS)](file:///home/ugai/Gemini4KT/src/jsMain/kotlin/io/github/ugaikit/gemini4kt/FunctionDsl.kt#L10-L13)  
    Finding: The KFunction overload of `generateContent` is exposed in `commonMain`, compiling on JS, but calls `buildAutomaticFunctionBinding` which is hardcoded to throw `UnsupportedOperationException("Direct Kotlin function binding is not supported in JS.")`.  
    Recommendation: Separate the JS API signature or mark the overload as unavailable on JS targets.

26. **Medium - Cached-content URLs built via string concatenation**  
    Status: `Confirmed`  
    Evidence: [Gemini.kt:255-259](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/Gemini.kt#L255-259), [Gemini.kt:273](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/Gemini.kt#L273), [Gemini.kt:285](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/Gemini.kt#L285)  
    Finding: URLs for fetching and deleting cached contents are built using raw string interpolation (`"$bUrl/$name"`). If `name` contains spaces, slashes, or query characters, the URL becomes invalid. `pageToken` is also appended without encoding.  
    Recommendation: Construct paths and query parameters using Ktor URLBuilder or encode parameters before appending.

27. **Medium - `GenerationConfig` schema fields are overlapping/non-exclusive**  
    Status: `Confirmed`  
    Evidence: [GenerationConfig.kt:51-55](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/GenerationConfig.kt#L51-L55), [GenerationConfig.kt:122-133](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/GenerationConfig.kt#L122-L133), [GenerationConfig.kt:217-239](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/GenerationConfig.kt#L217-L239)  
    Finding: `GenerationConfig` contains `responseSchema`, `responseJsonSchema`, and `_responseJsonSchema` (`underscoreResponseJsonSchema`). Callers can define multiple of these schemas in the builder, which might result in conflicts when serialized to the API.  
    Recommendation: Enforce mutual exclusivity checks inside `build()`.

28. **Medium - `FunctionCallingConfig` allows contradictory/empty policy states**  
    Status: `Confirmed`  
    Evidence: [FunctionCallingConfig.kt:34-54](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/FunctionCallingConfig.kt#L34-L54)  
    Finding: The builder allows configuring `mode = Mode.ANY` with an empty allowed function list (which is rejected by the API), or specifying allowed functions when `mode` is `AUTO` or `NONE`.  
    Recommendation: Validate consistency between the mode and allowed function names.

29. **Low - `ThinkingConfig` always serializes default budget**  
    Status: `Confirmed`  
    Evidence: [ThinkingConfig.kt:21-24](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/ThinkingConfig.kt#L21-L24)  
    Finding: The property `thinkingBudget` is annotated with `@EncodeDefault(EncodeDefault.Mode.ALWAYS)`. This means it will always serialize `"thinking_budget": 1024` even if the user did not set it, overriding server-side defaults and forcing a low token budget.  
    Recommendation: Remove the `ALWAYS` serialization mode or make the budget property nullable.

30. **Medium - `PromptFeedback` cannot represent missing `safetyRatings` field**  
    Status: `Confirmed in code (API-dependent impact)`  
    Evidence: [PromptFeedback.kt:17-19](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/PromptFeedback.kt#L17-L19)  
    Finding: `safetyRatings` is declared as a non-nullable list. If the server response contains a `promptFeedback` object but omits the `safetyRatings` field, Kotlinx Serialization throws a parsing exception.  
    Recommendation: Declare `safetyRatings` as nullable or assign an empty default list.

31. **Medium - `GeminiAI` leaks owned HTTP clients**  
    Status: `Confirmed`  
    Evidence: [GeminiAI.kt:30-49](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/GeminiAI.kt#L30-L49)  
    Finding: `GeminiAI` eagerly creates a `HttpClient` when one is not injected, but the class exposes no ownership flag or `close()` method. Callers that rely on the default client keep sockets, engine threads, and dispatchers open until process shutdown.  
    Recommendation: Add ownership tracking and a public `close()` method, mirroring the lifecycle handling already used by `Gemini`, `Batch`, and `FileSearch`.

32. **Medium - `FileUploadProvider` leaks owned HTTP clients on every target**  
    Status: `Confirmed`  
    Evidence: [FileUploadProvider.kt:12-16](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/FileUploadProvider.kt#L12-L16), [GeminiExtensions.kt:37-49 (JVM)](file:///home/ugai/Gemini4KT/src/jvmMain/kotlin/io/github/ugaikit/gemini4kt/GeminiExtensions.kt#L37-L49), [FileUploadProvider.kt:37-49 (JS)](file:///home/ugai/Gemini4KT/src/jsMain/kotlin/io/github/ugaikit/gemini4kt/FileUploadProvider.kt#L37-L49), [FileUploadProvider.kt:35-47 (Wasm)](file:///home/ugai/Gemini4KT/src/wasmJsMain/kotlin/io/github/ugaikit/gemini4kt/FileUploadProvider.kt#L35-L47), [FileUploadProvider.kt:42-50 (Native)](file:///home/ugai/Gemini4KT/src/nativeMain/kotlin/io/github/ugaikit/gemini4kt/FileUploadProvider.kt#L42-L50)  
    Finding: The expect class has no lifecycle API, while every actual implementation creates a `HttpClient` when `client` is null. That leaves the provider with no way to dispose internally created clients, so repeated uploads can leak resources.  
    Recommendation: Add `ownsHttpClient` plus `close()`, or require callers to inject and manage the client explicitly.

33. **Medium - Live session wrappers clone injected clients but never own the clone**  
    Status: `Confirmed`  
    Evidence: [GeminiLive.kt:85-92](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/live/GeminiLive.kt#L85-L92), [LiveMusic.kt:99-106](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/live/music/LiveMusic.kt#L99-L106)  
    Finding: `client?.config { install(WebSockets) }` returns a new `HttpClient`, but ownership is tracked only via `client == null`. When a caller injects a client, the configured clone is still created but never closed.  
    Recommendation: Track ownership of the configured clone or avoid cloning the injected client.

34. **Medium - Live debug logging exposes raw payloads**  
    Status: `Confirmed`  
    Evidence: [GeminiLive.kt:125-182](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/live/GeminiLive.kt#L125-L182), [GeminiLive.kt:267-267](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/live/GeminiLive.kt#L267-L267), [LiveMusic.kt:130-169](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/live/music/LiveMusic.kt#L130-L169), [LiveMusic.kt:281-281](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/live/music/LiveMusic.kt#L281-L281)  
    Finding: The live clients log full inbound and outbound JSON messages at debug level. Those payloads can include user prompts, tool arguments, model output, and other sensitive content.  
    Recommendation: Redact payloads or log only metadata such as message type and size.

35. **Medium - `ComputerUse` / `InteractionTool` collection type change is source-breaking**  
    Status: `Confirmed`  
    Evidence: [ComputerUse.kt:17-20](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/ComputerUse.kt#L17-L20), [InteractionTool.kt:28-35](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/interaction/InteractionTool.kt#L28-L35)  
    Finding: `excludedPredefinedFunctions` moved from `Array<String>` to `List<String>` in two public data classes. That breaks source compatibility for downstream Kotlin callers and changes the JS interop surface for consumers that construct or destructure these models directly.  
    Recommendation: Treat this as a breaking API change in release notes, or add compatibility overloads/wrappers before shipping a minor release.

36. **Medium - Sample entrypoints create default clients and never close them**  
    Status: `Confirmed`  
    Evidence: [FileUploadSample.kt:21-35](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/samples/FileUploadSample.kt#L21-L35), [BatchSample.kt:20-29](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/samples/BatchSample.kt#L20-L29), [LiveSample.kt:71-79](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/samples/LiveSample.kt#L71-L79), [FileSearchSample.kt:25-36](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/samples/FileSearchSample.kt#L25-L36)  
    Finding: Several sample helpers create default `Gemini`, `Batch`, or `FileSearch` instances when no client is injected, but only close the subordinate session/store resources. The created clients are left open, so repeated sample execution or embedding these helpers in a long-lived process leaks HTTP clients and their underlying engine resources.  
    Recommendation: Close any internally created client in a `finally` block, or require callers to manage the client lifecycle explicitly.

37. **Medium - JS export wrappers and one-shot helpers create clients without disposal**  
    Status: `Confirmed`  
    Evidence: [GeminiJs.kt:49-55](file:///home/ugai/Gemini4KT/src/jsMain/kotlin/io/github/ugaikit/gemini4kt/GeminiJs.kt#L49-L55), [GeminiJs.kt:62-79](file:///home/ugai/Gemini4KT/src/jsMain/kotlin/io/github/ugaikit/gemini4kt/GeminiJs.kt#L62-L79), [GeminiJs.kt:91-130](file:///home/ugai/Gemini4KT/src/jsMain/kotlin/io/github/ugaikit/gemini4kt/GeminiJs.kt#L91-L130), [Gemini.kt:438-464](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/Gemini.kt#L438-L464)  
    Finding: The JS-facing helpers construct `Gemini` internally but never call `close()`. `GeminiJsExport` also wraps a `Gemini` delegate without exposing disposal, and `GeminiJsClient` has no lifecycle API at all. In JS contexts this leaves open clients with no way for callers to release them.  
    Recommendation: Add a `close()` surface to JS wrappers and close one-shot clients in a `finally` block.

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

- Confirmed: 27
- Partially confirmed: 2 (`#12`, `#14`)
- Confirmed in code but API-response-dependent impact: 2 (`#15`, `#30`)

## Most Actionable First Fixes

1. Live WebSocket backpressure and error propagation (`#5`)
2. Image payload availability on non-JVM targets (`#6`)
3. Upload robustness (`#12`, `#24`)
4. DSL/builders validation for one-of and required fields (`#11`, `#14`, `#16`, `#27`, `#28`)
5. API response shape hardening (`#15`, `#30`)
