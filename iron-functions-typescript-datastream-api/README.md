# Iron Functions TypeScript DataStream API Demo

This example shows how to use Flink DataStream API with Iron Functions. It showcases two useful features:

- Output unnesting. If the WebAssembly function returns an array of records, its output can be "flattened" to multiple
Flink records.
- Output type override. By default, Iron Functions expect the output type to be the same as the input one, but it can be
 easily overridden. This is especially useful when combined with output unnesting.

Check `src/main/ts` folder to see the TypeScript project. That project can be packaged to a WebAssembly file. 
Consult its `README.md` to see build instructions. Once the WebAssembly file is built, copy it to the `resources` 
folder, e.g. `src/main/resources/wasm/demo-telemetry.wasm` and start the Flink application.

---

**NOTE**: this project requires an active Irontools License. Contact us at hello@irontools.dev to obtain one.
