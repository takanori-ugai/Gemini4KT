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
// The path depends on the build task used.
// For jsNodeProductionLibraryDistribution, it is typically in 'build/js/packages/gemini4kt'.
// This relies on package.json to find the main file.
const gemini4kt = require('./build/js/packages/gemini4kt');

// Access the exported GeminiClient class.
// The library structure follows the Kotlin package hierarchy.
const GeminiClient = gemini4kt.io.github.ugaikit.gemini4kt.GeminiClient;

const apiKey = process.env.GEMINI_API_KEY;

if (!apiKey) {
    console.error("Error: Please set the GEMINI_API_KEY environment variable.");
    process.exit(1);
}

// Instantiate the client
const client = new GeminiClient(apiKey);

console.log("Sending request to Gemini...");

// Call the async method
// Note: generateContentAsync returns a standard JavaScript Promise.
client.generateContentAsync("Hello, Gemini! Tell me a short joke.")
    .then(response => {
        console.log("--- Response from Gemini ---");
        console.log(response);
        console.log("----------------------------");
    })
    .catch(error => {
        console.error("--- Error ---");
        console.error(error);
        console.error("-------------");
    });
