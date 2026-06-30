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

14. **High - One-of protocol shapes not enforced in builders**  
    Status: `Confirmed`  
    Evidence: [Part.kt:69-269](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/Part.kt#L69-L269), [Tool.kt:46-190](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/Tool.kt#L46-L190), [SpeechConfig.kt:74-110](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/SpeechConfig.kt#L74-L110), [AttributionSourceId.kt:28-60](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/AttributionSourceId.kt#L28-L60)  
    Finding: Models representing mutually exclusive one-of properties (like `Part` and `Tool`) allow setting multiple or zero fields in their builders. The API will subsequently reject these requests.  
    Recommendation: Validate one-of invariants in `build()` and throw descriptive errors when violated.

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

---

## Investigation Summary

- Confirmed: 18
- Partially confirmed: 1 (`#12`)
- Confirmed in code but API-response-dependent impact: 2 (`#15`, `#30`)

## Most Actionable First Fixes

1. Live WebSocket backpressure and error propagation (`#5`)
2. Image payload availability on non-JVM targets (`#6`)
3. Upload robustness (`#12`, `#24`)
4. DSL/builders validation for one-of and required fields (`#11`, `#14`, `#16`, `#27`, `#28`)
5. API response shape hardening (`#15`, `#30`)
