# Iron Functions TypeScript Table/SQL API Demo

This example shows how to use Flink Table/SQL API with Iron Functions.

Check `src/main/ts` folder to see the TypeScript project. That project can be packaged to a UDF JAR containing the
WebAssembly file and runtime. Consult project's `README.md` to see build instructions. Once the WebAssembly file is 
built, you can create a UDF JAR using the `ironfun` CLI tool:

```bash
ironfun package-udf --source-path . --package-name com.demo.geo --class-name GeoDistance --include-license --uber-jar
```

---

**NOTE**: this project requires an active Irontools License. Contact us at hello@irontools.dev to obtain one.
