// Example expanded from src/commonMain/kotlin/io/github/ugaikit/gemini4kt/samples/Samples1.kt
// Build the JS artifacts first: ./gradlew jsNodeProductionLibraryDistribution
import pkg from "./build/js/packages/gemini4kt/kotlin/gemini4kt.js";
const { runSample1 } = pkg;

async function main() {
  const apiKey = process.env.GEMINI_API_KEY;
  if (!apiKey) {
    throw new Error("Set GEMINI_API_KEY in your environment before running this sample.");
  }

  const prompt = "Write a story about a magic backpack.";
  const model = "gemini-2.5-flash-lite"; // matches Sample1.kt

  // runSample1 applies the safety settings and thinking config from Sample1.kt
  const text = await runSample1(apiKey, prompt, model);
  console.log(text.trim());
}

main().catch((err) => {
  console.error(err);
  process.exitCode = 1;
});
