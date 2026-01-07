// JS entrypoint using exported Kotlin classes (Gemini + GenerateContentRequest) with gemma-3-12b-it.
// Build the JS artifacts first: ./gradlew jsNodeProductionLibraryDistribution

async function loadKotlinExports() {
  // Dynamically import after wiring CLI args so the Kotlin JS main sees them.
  const pkg = await import("./build/js/packages/gemini4kt/kotlin/gemini4kt.js");
  return pkg.io?.github?.ugaikit?.gemini4kt ?? {};
}

async function main() {
  const cliArgs = process.argv.slice(2);

  // If the user provided args, forward them to the Kotlin sample launcher via CLI_ARGS
  // and let the Kotlin entrypoint handle the action (e.g., "models").
  if (cliArgs.length > 0) {
    process.env.CLI_ARGS = cliArgs.join(" ");
    await import("./build/js/packages/gemini4kt/kotlin/gemini4kt.js");
    return;
  }

  const {
    Gemini,
    GenerateContentRequest,
    Content,
    Part,
    SafetySetting,
    HarmCategory,
    Threshold,
  } = await loadKotlinExports();

  const apiKey = process.env.GEMINI_API_KEY;
  if (!apiKey) {
    throw new Error("Set GEMINI_API_KEY in your environment before running this sample.");
  }
  for (const [name, value] of [
    ["Gemini", Gemini],
    ["GenerateContentRequest", GenerateContentRequest],
    ["Content", Content],
    ["Part", Part],
    ["SafetySetting", SafetySetting],
  ]) {
    if (typeof value !== "function") {
      throw new Error(`${name} export not found in gemini4kt bundle.`);
    }
  }

  const prompt = "Write a story about a magic backpack.";
  const model = "gemma-3-12b-it";

  // Build a Kotlin-side request without thinkingConfig (gemma models do not support it).
  const contents = [new Content([new Part(prompt)])];
  const safetySettings = [new SafetySetting(HarmCategory.HARM_CATEGORY_HARASSMENT, Threshold.BLOCK_ONLY_HIGH)];
  const request = new GenerateContentRequest(contents, [], null, safetySettings);

  const client = new Gemini(apiKey);
  const responseJson = await client.generateContent(request, model);
  const parsed = typeof responseJson === "string" ? JSON.parse(responseJson) : responseJson;
  const text = parsed?.candidates?.[0]?.content?.parts?.[0]?.text ?? "";
  console.log(text.replace(/\n\n/g, "\n").trim());
}

main().catch((err) => {
  console.error(err);
  process.exitCode = 1;
});
