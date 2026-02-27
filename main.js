// JS entrypoint using exported Kotlin classes (Gemini + GenerateContentRequest) with gemma-3-12b-it.
// Build the JS artifacts first: ./gradlew jsNodeProductionLibraryDistribution

async function main() {
  const cliArgs = process.argv.slice(2);

  if (cliArgs.length === 0) {
    console.log("No target specified. Example: node main.js models");
  } else {
    // Forward args to the Kotlin sample launcher via CLI_ARGS.
    process.env.CLI_ARGS = cliArgs.join(" ");
  }

  await import("./build/js/packages/gemini4kt/kotlin/gemini4kt.js");
}

main().catch((err) => {
  console.error(err);
  process.exitCode = 1;
});
