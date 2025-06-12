# abi-decoding

## Build

Run

```bash
cargo build --target wasm32-wasip1 --release
```

to build this project. This will emit the WASM file in the `target/wasm32-wasip1/release` directory.

`wasm32-wasip1` is used instead of `wasm32-unknown-unknown` because one of the dependencies relies on the 
`getrandom` crate, which requires the `wasip1` target to work correctly in WASM. Ironfun CLI will automatically
use the `wasip1` target when looking for the WASM file.
