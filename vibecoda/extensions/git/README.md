# Git integration for Vibecoda

**Notice:** This extension is bundled with Vibecoda. It can be disabled but not uninstalled.

## Features

See [Git support in Vibecoda](https://code.visualstudio.com/docs/editor/versioncontrol#_git-support) to learn about the features of this extension.

## API

The Git extension exposes an API, reachable by any other extension.

1. Copy `src/api/git.d.ts` to your extension's sources;
2. Include `git.d.ts` in your extension's compilation.
3. Get a hold of the API with the following snippet:

	```ts
	const gitExtension = vibecoda.extensions.getExtension<GitExtension>('vibecoda.git').exports;
	const git = gitExtension.getAPI(1);
	```
	**Note:** To ensure that the `vibecoda.git` extension is activated before your extension, add `extensionDependencies` ([docs](https://code.visualstudio.com/api/references/extension-manifest)) into the `package.json` of your extension:
	```json
	"extensionDependencies": [
		"vibecoda.git"
	]
	```
