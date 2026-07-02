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

---

## Investigation Summary

- Confirmed: 0
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
