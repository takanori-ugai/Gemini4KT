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

async function runSamples() {
    console.log("--- Sample 1: generateContent(String) ---");
    try {
        // Sample 1: Passing a simple string prompt
        // Returns a Promise<String> (the text content)
        const textResponse = await client.generateContent("Hello, Gemini! Tell me a one-liner.");
        console.log("Response Text:", textResponse);
    } catch (e) {
        console.error("Error in Sample 1:", e);
    }
    console.log("\n");

    console.log("--- Sample 2: generateContent(JsonObject) ---");
    try {
        // Sample 2: Passing a plain JavaScript object (JSON)
        // Returns a Promise<GenerateContentResponse> (as a JS object)
        const jsonRequest = {
            contents: [{
                parts: [{ text: "Explain quantum computing in 10 words." }]
            }]
        };
        const jsonResponse = await client.generateContent(jsonRequest);
        console.log("Response Object:", JSON.stringify(jsonResponse, null, 2));
    } catch (e) {
        console.error("Error in Sample 2:", e);
    }
    console.log("\n");

    console.log("--- Sample 3: generateContent(GenerateContentRequest) ---");
    try {
        // Sample 3: Using the exported GenerateContentRequest class
        // Returns a Promise<GenerateContentResponse> (as a JS object)
        const part = new Part("Write a haiku about coding.");
        const content = new Content([part]);
        const request = new GenerateContentRequest([content]);

        const response = await client.generateContent(request);
        console.log("Response Object:", JSON.stringify(response, null, 2));

        // Accessing candidates
        if (response.candidates && response.candidates.length > 0) {
            const firstCandidate = response.candidates[0];
            if (firstCandidate.content && firstCandidate.content.parts && firstCandidate.content.parts.length > 0) {
                 console.log("Extracted Text:", firstCandidate.content.parts[0].text);
            }
        }
    } catch (e) {
        console.error("Error in Sample 3:", e);
    }
}

runSamples();
