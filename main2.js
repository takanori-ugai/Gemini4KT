// JS entrypoint that uses the exported Kotlin JS APIs (via @JsExport).
import pkg from "./build/js/packages/gemini4kt/kotlin/gemini4kt.js";

async function main() {
  const apiKey = process.env.GEMINI_API_KEY;
  const cliArgs = process.argv.slice(2);
  const prompt = cliArgs.join(" ").trim() || "Write a story about a magic backpack.";
  const model = "gemini-2.5-flash-lite";

  const { runSample1 } = pkg.io?.github?.ugaikit?.gemini4kt ?? {};
  if (typeof runSample1 !== "function") {
    throw new Error("runSample1 export not found in gemini4kt bundle. Rebuild JS artifacts first.");
  }
  if (!apiKey) {
    throw new Error("Set GEMINI_API_KEY in your environment before running this sample.");
  }

  const text = await runSample1(apiKey, prompt, model);
  console.log(text.replace(/\n\n/g, "\n"));
}

main().catch((err) => {
  console.error(err);
  process.exitCode = 1;
});
