/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as httpRequest from 'request-light';
import * as vibecoda from 'vibecoda';
import { addJSONProviders } from './features/jsonContributions';
import { runSelectedScript, selectAndRunScriptFromFolder } from './commands';
import { NpmScriptsTreeDataProvider } from './npmView';
import { getScriptRunner, getPackageManager, invalidateTasksCache, NpmTaskProvider, hasPackageJson } from './tasks';
import { invalidateHoverScriptsCache, NpmScriptHoverProvider } from './scriptHover';
import { NpmScriptLensProvider } from './npmScriptLens';
import which from 'which';

let treeDataProvider: NpmScriptsTreeDataProvider | undefined;

function invalidateScriptCaches() {
	invalidateHoverScriptsCache();
	invalidateTasksCache();
	if (treeDataProvider) {
		treeDataProvider.refresh();
	}
}

export async function activate(context: vibecoda.ExtensionContext): Promise<void> {
	configureHttpRequest();
	context.subscriptions.push(vibecoda.workspace.onDidChangeConfiguration(e => {
		if (e.affectsConfiguration('http.proxy') || e.affectsConfiguration('http.proxyStrictSSL')) {
			configureHttpRequest();
		}
	}));

	const npmCommandPath = await getNPMCommandPath();
	context.subscriptions.push(addJSONProviders(httpRequest.xhr, npmCommandPath));
	registerTaskProvider(context);

	treeDataProvider = registerExplorer(context);

	context.subscriptions.push(vibecoda.workspace.onDidChangeConfiguration((e) => {
		if (e.affectsConfiguration('npm.exclude') || e.affectsConfiguration('npm.autoDetect') || e.affectsConfiguration('npm.scriptExplorerExclude') || e.affectsConfiguration('npm.runSilent') || e.affectsConfiguration('npm.packageManager') || e.affectsConfiguration('npm.scriptRunner')) {
			invalidateTasksCache();
			if (treeDataProvider) {
				treeDataProvider.refresh();
			}
		}
		if (e.affectsConfiguration('npm.scriptExplorerAction')) {
			if (treeDataProvider) {
				treeDataProvider.refresh();
			}
		}
	}));

	registerHoverProvider(context);

	context.subscriptions.push(vibecoda.commands.registerCommand('npm.runSelectedScript', runSelectedScript));

	if (await hasPackageJson()) {
		vibecoda.commands.executeCommand('setContext', 'npm:showScriptExplorer', true);
	}

	context.subscriptions.push(vibecoda.commands.registerCommand('npm.runScriptFromFolder', selectAndRunScriptFromFolder));
	context.subscriptions.push(vibecoda.commands.registerCommand('npm.refresh', () => {
		invalidateScriptCaches();
	}));
	context.subscriptions.push(vibecoda.commands.registerCommand('npm.scriptRunner', (args) => {
		if (args instanceof vibecoda.Uri) {
			return getScriptRunner(args, context, true);
		}
		return '';
	}));
	context.subscriptions.push(vibecoda.commands.registerCommand('npm.packageManager', (args) => {
		if (args instanceof vibecoda.Uri) {
			return getPackageManager(args, context, true);
		}
		return '';
	}));
	context.subscriptions.push(new NpmScriptLensProvider());

	context.subscriptions.push(vibecoda.window.registerTerminalQuickFixProvider('ms-vibecoda.npm-command', {
		provideTerminalQuickFixes({ outputMatch }) {
			if (!outputMatch) {
				return;
			}

			const lines = outputMatch.regexMatch[1];
			const fixes: vibecoda.TerminalQuickFixTerminalCommand[] = [];
			for (const line of lines.split('\n')) {
				// search from the second char, since the lines might be prefixed with
				// "npm ERR!" which comes before the actual command suggestion.
				const begin = line.indexOf('npm', 1);
				if (begin === -1) {
					continue;
				}

				const end = line.lastIndexOf('#');
				fixes.push({ terminalCommand: line.slice(begin, end === -1 ? undefined : end - 1) });
			}

			return fixes;
		},
	}));
}

async function getNPMCommandPath(): Promise<string | undefined> {
	if (vibecoda.workspace.isTrusted && canRunNpmInCurrentWorkspace()) {
		try {
			return await which(process.platform === 'win32' ? 'npm.cmd' : 'npm');
		} catch (e) {
			return undefined;
		}
	}
	return undefined;
}

function canRunNpmInCurrentWorkspace() {
	if (vibecoda.workspace.workspaceFolders) {
		return vibecoda.workspace.workspaceFolders.some(f => f.uri.scheme === 'file');
	}
	return false;
}

let taskProvider: NpmTaskProvider;
function registerTaskProvider(context: vibecoda.ExtensionContext): vibecoda.Disposable | undefined {
	if (vibecoda.workspace.workspaceFolders) {
		const watcher = vibecoda.workspace.createFileSystemWatcher('**/package.json');
		watcher.onDidChange((_e) => invalidateScriptCaches());
		watcher.onDidDelete((_e) => invalidateScriptCaches());
		watcher.onDidCreate((_e) => invalidateScriptCaches());
		context.subscriptions.push(watcher);

		const workspaceWatcher = vibecoda.workspace.onDidChangeWorkspaceFolders((_e) => invalidateScriptCaches());
		context.subscriptions.push(workspaceWatcher);

		taskProvider = new NpmTaskProvider(context);
		const disposable = vibecoda.tasks.registerTaskProvider('npm', taskProvider);
		context.subscriptions.push(disposable);
		return disposable;
	}
	return undefined;
}

function registerExplorer(context: vibecoda.ExtensionContext): NpmScriptsTreeDataProvider | undefined {
	if (vibecoda.workspace.workspaceFolders) {
		const treeDataProvider = new NpmScriptsTreeDataProvider(context, taskProvider!);
		const view = vibecoda.window.createTreeView('npm', { treeDataProvider: treeDataProvider, showCollapseAll: true });
		context.subscriptions.push(view);
		return treeDataProvider;
	}
	return undefined;
}

function registerHoverProvider(context: vibecoda.ExtensionContext): NpmScriptHoverProvider | undefined {
	if (vibecoda.workspace.workspaceFolders) {
		const npmSelector: vibecoda.DocumentSelector = {
			language: 'json',
			scheme: 'file',
			pattern: '**/package.json'
		};
		const provider = new NpmScriptHoverProvider(context);
		context.subscriptions.push(vibecoda.languages.registerHoverProvider(npmSelector, provider));
		return provider;
	}
	return undefined;
}

function configureHttpRequest() {
	const httpSettings = vibecoda.workspace.getConfiguration('http');
	httpRequest.configure(httpSettings.get<string>('proxy', ''), httpSettings.get<boolean>('proxyStrictSSL', true));
}

export function deactivate(): void {
}
