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

33. **Low - `GeminiLiveSessionTest` should assert the actual close frame, not just cancellation flags**  
    Status: `Confirmed`  
    Evidence: [GeminiLiveSessionTest.kt:187-201](file:///home/ugai/Gemini4KT/src/commonTest/kotlin/io/github/ugaikit/gemini4kt/live/GeminiLiveSessionTest.kt#L187-L201)  
    Finding: The current close test only checks `job.isCancelled` and `incoming.isClosedForSend`, while the `mockSession.outgoing` close frame is read and then ignored. That leaves the wire-level shutdown behavior unverified, so regressions in the WebSocket close handshake could still pass the suite.  
    Recommendation: Add an assertion on the outgoing close frame, or otherwise verify that the session emits the expected close control frame before completing.

35. **Low - `FunctionCallingConfigBuilder` still needs explicit coverage for the remaining mode branches**  
    Status: `Confirmed`  
    Evidence: [FunctionCallingConfig.kt:53-66](file:///home/ugai/Gemini4KT/src/commonMain/kotlin/io/github/ugaikit/gemini4kt/FunctionCallingConfig.kt#L53-L66), [FunctionCallingConfigBuilderTest.kt:8-37](file:///home/ugai/Gemini4KT/src/commonTest/kotlin/io/github/ugaikit/gemini4kt/FunctionCallingConfigBuilderTest.kt#L8-L37)  
    Finding: The builder tests cover `Mode.ANY` and `Mode.AUTO`, but not the `VALIDATED`, `NONE`, or `MODE_UNSPECIFIED` branches that drive different validation rules. Without those tests, a regression in the allowlist logic for the remaining modes could slip through undetected.  
    Recommendation: Add tests for the remaining mode families, including the allowed-function and empty-allowlist failure cases.

---

## Investigation Summary

- Confirmed: 8 (`#25`, `#26`, `#27`, `#28`, `#29`, `#30`, `#33`, `#35`)
- Partially confirmed: 1 (`#12`)
- Disproved: 0
- Confirmed in code but API-response-dependent impact: 0

## Most Actionable First Fixes

1. Non-JVM uploads buffer full file in memory (`#12`)
2. JVM timeout default (`#25`)
3. JVM music sample buffering (`#26`)
4. JS CLI arg leakage/parsing (`#27`)
5. JS timeout absence (`#28`)
6. Sample polling loop timeout (`#29`)
7. Sample failure masking (`#30`)
8. Live session close-frame assertion (`#33`)
9. Function-calling mode coverage (`#35`)
