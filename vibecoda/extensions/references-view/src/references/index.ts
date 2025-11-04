/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as vibecoda from 'vibecoda';
import { SymbolsTree } from '../tree';
import { FileItem, ReferenceItem, ReferencesModel, ReferencesTreeInput } from './model';

export function register(tree: SymbolsTree, context: vibecoda.ExtensionContext): void {

	function findLocations(title: string, command: string) {
		if (vibecoda.window.activeTextEditor) {
			const input = new ReferencesTreeInput(title, new vibecoda.Location(vibecoda.window.activeTextEditor.document.uri, vibecoda.window.activeTextEditor.selection.active), command);
			tree.setInput(input);
		}
	}

	context.subscriptions.push(
		vibecoda.commands.registerCommand('references-view.findReferences', () => findLocations('References', 'vibecoda.executeReferenceProvider')),
		vibecoda.commands.registerCommand('references-view.findImplementations', () => findLocations('Implementations', 'vibecoda.executeImplementationProvider')),
		// --- legacy name
		vibecoda.commands.registerCommand('references-view.find', (...args: any[]) => vibecoda.commands.executeCommand('references-view.findReferences', ...args)),
		vibecoda.commands.registerCommand('references-view.removeReferenceItem', removeReferenceItem),
		vibecoda.commands.registerCommand('references-view.copy', copyCommand),
		vibecoda.commands.registerCommand('references-view.copyAll', copyAllCommand),
		vibecoda.commands.registerCommand('references-view.copyPath', copyPathCommand),
	);


	// --- references.preferredLocation setting

	let showReferencesDisposable: vibecoda.Disposable | undefined;
	const config = 'references.preferredLocation';
	function updateShowReferences(event?: vibecoda.ConfigurationChangeEvent) {
		if (event && !event.affectsConfiguration(config)) {
			return;
		}
		const value = vibecoda.workspace.getConfiguration().get<string>(config);

		showReferencesDisposable?.dispose();
		showReferencesDisposable = undefined;

		if (value === 'view') {
			showReferencesDisposable = vibecoda.commands.registerCommand('editor.action.showReferences', async (uri: vibecoda.Uri, position: vibecoda.Position, locations: vibecoda.Location[]) => {
				const input = new ReferencesTreeInput(vibecoda.l10n.t('References'), new vibecoda.Location(uri, position), 'vibecoda.executeReferenceProvider', locations);
				tree.setInput(input);
			});
		}
	}
	context.subscriptions.push(vibecoda.workspace.onDidChangeConfiguration(updateShowReferences));
	context.subscriptions.push({ dispose: () => showReferencesDisposable?.dispose() });
	updateShowReferences();
}

const copyAllCommand = async (item: ReferenceItem | FileItem | unknown) => {
	if (item instanceof ReferenceItem) {
		copyCommand(item.file.model);
	} else if (item instanceof FileItem) {
		copyCommand(item.model);
	}
};

function removeReferenceItem(item: FileItem | ReferenceItem | unknown) {
	if (item instanceof FileItem) {
		item.remove();
	} else if (item instanceof ReferenceItem) {
		item.remove();
	}
}


async function copyCommand(item: ReferencesModel | ReferenceItem | FileItem | unknown) {
	let val: string | undefined;
	if (item instanceof ReferencesModel) {
		val = await item.asCopyText();
	} else if (item instanceof ReferenceItem) {
		val = await item.asCopyText();
	} else if (item instanceof FileItem) {
		val = await item.asCopyText();
	}
	if (val) {
		await vibecoda.env.clipboard.writeText(val);
	}
}

async function copyPathCommand(item: FileItem | unknown) {
	if (item instanceof FileItem) {
		if (item.uri.scheme === 'file') {
			vibecoda.env.clipboard.writeText(item.uri.fsPath);
		} else {
			vibecoda.env.clipboard.writeText(item.uri.toString(true));
		}
	}
}
