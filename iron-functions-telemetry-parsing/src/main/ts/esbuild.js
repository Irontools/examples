const esbuild = require("esbuild");
const { NodeModulesPolyfillPlugin } = require('@esbuild-plugins/node-modules-polyfill')

esbuild
    .build({
        entryPoints: ["src/main.ts"],
        outdir: "dist",
        bundle: true,
        sourcemap: true,
        plugins: [NodeModulesPolyfillPlugin()],
        minify: false,
        format: "cjs", // needs to be CJS for now
        target: ["es2020"], // can't go higher that this for now
    });
