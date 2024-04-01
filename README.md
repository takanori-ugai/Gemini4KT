# Gemini4KT

## Kotlin Wrapper Library for Gemini's RestAPI

This is a Kotlin wrapper library for Gemini's RestAPI, a lightweight library that does not require the Android SDK. Currently, it is JVM-only.

See [RestAPI specifications](https://ai.google.dev/tutorials/rest_quickstart?hl=en).

## Running the Example

To run the example, set your Gemini API key as an environment variable and execute the Gradle run command:

```bash
export GEMINI_API_KEY=your-api-key
./gradlew run
```

Look at io.github.ugaikit.gemini4kt.ITTest.kt as sample usage.

## API Document
The API Document can be found at [https://takanori-ugai.github.io/Gemini4KT/](https://takanori-ugai.github.io/Gemini4KT/).

## Usage in Gradle
```gradle.kts
dependencies {
    implementation("io.github.ugaikit:gemini4kt:0.2.0")
}
```

## Multiplatform Support
[Google Generative AI SDK for Kotlin Multiplatform](https://github.com/PatilShreyas/generative-ai-kmp) supports
multiplatform.