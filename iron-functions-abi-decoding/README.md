# Iron Functions Ethereum ABI Decoding UDF Demo

This example shows how to use Flink Table/SQL API with Iron Functions (Rust).

Check `src/main/rust` folder to see the Rust project. That project can be packaged to a UDF JAR containing the
WebAssembly file and runtime. Consult project's `README.md` to see build instructions. Once the WebAssembly file is 
built, you can create a UDF JAR using the `ironfun` CLI tool:

```bash
ironfun package-udf --source-path . --package-name com.demo.abi --class-name AbiDecoder --include-license --uber-jar
```

---

**NOTE**: this project requires an active Irontools License. You can obtain it [here](https://irontools.dev/pricing/).
