# Iron Functions OpenTelemetry Logs Parsing UDF Demo

This example shows how to use Flink Table/SQL API with Iron Functions (Golang).

Check `src/main/go` folder to see the Golang project. That project can be packaged to a UDF JAR containing the
WebAssembly file and runtime. Consult project's `README.md` to see build instructions. Once the WebAssembly file is 
built, you can create a UDF JAR using the `ironfun` CLI tool:

```bash
ironfun package-udf --source-path . --package-name com.demo.otel --class-name LogParser --include-license --uber-jar
```

---

**NOTE**: this project requires an active Irontools License. You can obtain it [here](https://irontools.dev/pricing/).
