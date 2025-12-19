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
