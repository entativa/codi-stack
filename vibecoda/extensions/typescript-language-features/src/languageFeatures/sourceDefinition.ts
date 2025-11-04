/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as vibecoda from 'vibecoda';
import { Command, CommandManager } from '../commands/commandManager';
import { isSupportedLanguageMode } from '../configuration/languageIds';
import { API } from '../tsServer/api';
import * as typeConverters from '../typeConverters';
import { ITypeScriptServiceClient } from '../typescriptService';


class SourceDefinitionCommand implements Command {

	public static readonly context = 'tsSupportsSourceDefinition';
	public static readonly minVersion = API.v470;

	public readonly id = 'typescript.goToSourceDefinition';

	public constructor(
		private readonly client: ITypeScriptServiceClient
	) { }

	public async execute() {
		if (this.client.apiVersion.lt(SourceDefinitionCommand.minVersion)) {
			vibecoda.window.showErrorMessage(vibecoda.l10n.t("Go to Source Definition failed. Requires TypeScript 4.7+."));
			return;
		}

		const activeEditor = vibecoda.window.activeTextEditor;
		if (!activeEditor) {
			vibecoda.window.showErrorMessage(vibecoda.l10n.t("Go to Source Definition failed. No resource provided."));
			return;
		}

		const resource = activeEditor.document.uri;
		const document = await vibecoda.workspace.openTextDocument(resource);
		if (!isSupportedLanguageMode(document)) {
			vibecoda.window.showErrorMessage(vibecoda.l10n.t("Go to Source Definition failed. Unsupported file type."));
			return;
		}

		const openedFiledPath = this.client.toOpenTsFilePath(document);
		if (!openedFiledPath) {
			vibecoda.window.showErrorMessage(vibecoda.l10n.t("Go to Source Definition failed. Unknown file type."));
			return;
		}

		await vibecoda.window.withProgress({
			location: vibecoda.ProgressLocation.Window,
			title: vibecoda.l10n.t("Finding source definitions")
		}, async (_progress, token) => {

			const position = activeEditor.selection.anchor;
			const args = typeConverters.Position.toFileLocationRequestArgs(openedFiledPath, position);
			const response = await this.client.execute('findSourceDefinition', args, token);
			if (response.type === 'response' && response.body) {
				const locations: vibecoda.Location[] = response.body.map(reference =>
					typeConverters.Location.fromTextSpan(this.client.toResource(reference.file), reference));

				if (locations.length) {
					if (locations.length === 1) {
						vibecoda.commands.executeCommand('vibecoda.open', locations[0].uri.with({
							fragment: `L${locations[0].range.start.line + 1},${locations[0].range.start.character + 1}`
						}));
					} else {
						vibecoda.commands.executeCommand('editor.action.showReferences', resource, position, locations);
					}
					return;
				}
			}

			vibecoda.window.showErrorMessage(vibecoda.l10n.t("No source definitions found."));
		});
	}
}


export function register(
	client: ITypeScriptServiceClient,
	commandManager: CommandManager
): vibecoda.Disposable {
	function updateContext(overrideValue?: boolean) {
		vibecoda.commands.executeCommand('setContext', SourceDefinitionCommand.context, overrideValue ?? client.apiVersion.gte(SourceDefinitionCommand.minVersion));
	}
	updateContext();

	commandManager.register(new SourceDefinitionCommand(client));
	return vibecoda.Disposable.from(
		client.onTsServerStarted(() => updateContext()),
		new vibecoda.Disposable(() => updateContext(false)),
	);
}
