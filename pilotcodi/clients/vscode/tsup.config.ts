import { defineConfig, Options } from "tsup";
import fs from "fs-extra";
import path from "path";
import { getInstalledPath } from "get-installed-path";
import { copy } from "esbuild-plugin-copy";
import { polyfillNode } from "esbuild-plugin-polyfill-node";
import dedent from "dedent";

const banner = dedent`
  /**
   * PilotCodi VSCode Extension
   * https://github.com/pilotcodiml/pilotcodi/tree/main/clients/vscode
   * Copyright (c) 2023-2024 PilotCodiML, Inc.
   * Licensed under the Apache License 2.0.
   */`;

export default defineConfig(async (options: Options): Promise<Options[]> => {
  const pilotcodiAgentDist = path
    .join(await getInstalledPath("pilotcodi-agent", { local: true }), "dist")
    .replaceAll(path.sep, path.posix.sep);
  const copyPilotCodiAgentTask: Options = {
    name: "copy-pilotcodi-agent",
    entry: ["scripts/dummy.js"],
    clean: true,
    esbuildPlugins: [
      copy({
        assets: { from: `${pilotcodiAgentDist}/**`, to: path.join("dist", "pilotcodi-agent") },
        resolveFrom: "cwd",
      }),
    ],
    onSuccess: async () => {
      await fs.remove(path.join(__dirname, "dist/dummy.js"));
    },
  };
  const buildNodeTask: Options = {
    name: "node",
    entry: ["src/extension.ts"],
    outDir: "dist/node",
    platform: "node",
    target: "node18",
    sourcemap: true,
    loader: {
      ".html": "text",
    },
    define: {
      "process.env.IS_BROWSER": "false",
    },
    treeshake: {
      preset: "smallest",
      moduleSideEffects: "no-external",
    },
    external: ["vscode", "vscode-languageserver/browser"],
    banner: {
      js: banner,
    },
    onSuccess: options.env?.["LAUNCH_ON_SUCCESS"]
      ? `code --extensionDevelopmentPath=${__dirname} --disable-extensions`
      : undefined,
  };
  const buildBrowserTask: Options = {
    name: "browser",
    entry: ["src/extension.ts"],
    outDir: "dist/browser",
    platform: "browser",
    sourcemap: true,
    loader: {
      ".html": "text",
    },
    define: {
      "process.env.IS_BROWSER": "true",
    },
    treeshake: {
      preset: "smallest",
      moduleSideEffects: "no-external",
    },
    external: ["vscode", "vscode-languageserver/node"],
    esbuildPlugins: [
      polyfillNode({
        polyfills: {},
      }),
    ],
    banner: {
      js: banner,
    },
    onSuccess: options.env?.["LAUNCH_ON_SUCCESS"]
      ? `vscode-test-web --extensionDevelopmentPath=${__dirname} --browserType=chromium --port=3000`
      : undefined,
  };

  if (!options.platform) {
    return [copyPilotCodiAgentTask, buildNodeTask, buildBrowserTask];
  } else if (options.platform == "node") {
    return [copyPilotCodiAgentTask, buildNodeTask];
  } else if (options.platform == "browser") {
    return [copyPilotCodiAgentTask, buildBrowserTask];
  } else {
    throw new Error("Invalid platform.");
  }
});
