# Gemini4KT

We will not maintain this library anymore. We are moving to Google Gen AI Java SDK(https://github.com/googleapis/java-genai).

## Kotlin Wrapper Library for Gemini's RestAPI

This is a Kotlin wrapper library for Gemini's RestAPI, a lightweight library that does not require the Android SDK. Currently, it is JVM-only.

See [RestAPI specifications](https://ai.google.dev/tutorials/rest_quickstart?hl=en) and https://ai.google.dev/api/rest/ for more information.

## Running the Example

Set your Gemini API key as an environment variable before running the sample:

```bash
cp .env.example .env
# Edit .env and set GEMINI_API_KEY=your-real-key
source .env
./gradlew run
```

`.env` is ignored by git so your key stays local. Look at io.github.ugaikit.gemini4kt.ITTest.kt as sample usage.

## API Document
The API Document can be found at [https://takanori-ugai.github.io/Gemini4KT/](https://takanori-ugai.github.io/Gemini4KT/).

## Usage in Gradle
```gradle.kts
dependencies {
    implementation("io.github.ugaikit:gemini4kt:0.3.0")
}
```

## Multiplatform Support
[Google Generative AI SDK for Kotlin Multiplatform](https://github.com/PatilShreyas/generative-ai-kmp) supports
multiplatform.
