// sample.js
//
// This sample demonstrates how to use the Gemini4KT library in a Node.js environment.
//
// Prerequisites:
// 1. Build the project to generate the JS library:
//    ./gradlew jsNodeProductionLibraryDistribution
//    (or ./gradlew jsBrowserProductionLibraryDistribution)
//
// 2. Set the GEMINI_API_KEY environment variable.
//    export GEMINI_API_KEY="your_api_key_here"
//
// 3. Run this script:
//    node sample.js

// Import the generated Kotlin/JS library.
const gemini4kt = require('./build/js/packages/gemini4kt');

// Access the exported classes.
// The library structure follows the Kotlin package hierarchy.
const GeminiClient = gemini4kt.io.github.ugaikit.gemini4kt.GeminiClient;
const GenerateContentRequest = gemini4kt.io.github.ugaikit.gemini4kt.GenerateContentRequest;
const Content = gemini4kt.io.github.ugaikit.gemini4kt.Content;
const Part = gemini4kt.io.github.ugaikit.gemini4kt.Part;

const apiKey = process.env.GEMINI_API_KEY;

if (!apiKey) {
    console.error("Error: Please set the GEMINI_API_KEY environment variable.");
    process.exit(1);
}

// Instantiate the client
const client = new GeminiClient(apiKey);

console.log("Sending request to Gemini...");

// Create a request using the exported data classes.
// Note: Kotlin lists are typically mapped to JS arrays in constructor arguments for @JsExport classes
// if using the latest K/JS IR compiler and standard configurations, OR sometimes they require specific list types.
// For data classes with default values, we might need to be careful with arguments.
// However, `contents` is the first argument and is mandatory.
// The structure is: GenerateContentRequest(contents, tools, toolConfig, safetySettings, systemInstruction, generationConfig, cachedContent)

const part = new Part("Hello, Gemini! Tell me a short joke.");
// Create a Content object. The first argument is 'parts' (List<Part>?), second is 'role' (String?).
// We pass an array for the list.
const content = new Content([part]);

// Create the request object.
// We only provide the first argument 'contents' (List<Content>).
// The other arguments have default values (null or empty list), so we can omit them if the JS export handles default arguments correctly.
// If not, we might need to pass undefined or null for subsequent arguments.
// Standard Kotlin/JS with @JsExport usually supports default arguments in the constructor if they are @JsExport-ed properly.
const request = new GenerateContentRequest([content]);

// Call the async method that accepts a GenerateContentRequest
// Note: We use the 'generateContent' method we exposed which takes the request object.
// The returned value is a Promise.
client.generateContent(request)
    .then(response => {
        console.log("--- Response from Gemini ---");
        // The response is a Kotlin object structure serialized to JSON object by our wrapper,
        // so it should be a plain JS object now (thanks to JSON.parse).
        console.log(JSON.stringify(response, null, 2));
        console.log("----------------------------");

        // Accessing candidates
        if (response.candidates && response.candidates.length > 0) {
            const firstCandidate = response.candidates[0];
             if (firstCandidate.content && firstCandidate.content.parts && firstCandidate.content.parts.length > 0) {
                 console.log("Text: " + firstCandidate.content.parts[0].text);
             }
        }
    })
    .catch(error => {
        console.error("--- Error ---");
        console.error(error);
        console.error("-------------");
    });
