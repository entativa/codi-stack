/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as vibecoda from 'vibecoda';
import type * as lsp from 'vibecoda-languageserver-types';
import { MdLanguageClient } from '../client/client';
import { Command, CommandManager } from '../commandManager';


export class FindFileReferencesCommand implements Command {

	public readonly id = 'markdown.findAllFileReferences';

	constructor(
		private readonly _client: MdLanguageClient,
	) { }

	public async execute(resource?: vibecoda.Uri) {
		resource ??= vibecoda.window.activeTextEditor?.document.uri;
		if (!resource) {
			vibecoda.window.showErrorMessage(vibecoda.l10n.t("Find file references failed. No resource provided."));
			return;
		}

		await vibecoda.window.withProgress({
			location: vibecoda.ProgressLocation.Window,
			title: vibecoda.l10n.t("Finding file references")
		}, async (_progress, token) => {
			const locations = (await this._client.getReferencesToFileInWorkspace(resource, token)).map(loc => {
				return new vibecoda.Location(vibecoda.Uri.parse(loc.uri), convertRange(loc.range));
			});

			const config = vibecoda.workspace.getConfiguration('references');
			const existingSetting = config.inspect<string>('preferredLocation');

			await config.update('preferredLocation', 'view');
			try {
				await vibecoda.commands.executeCommand('editor.action.showReferences', resource, new vibecoda.Position(0, 0), locations);
			} finally {
				await config.update('preferredLocation', existingSetting?.workspaceFolderValue ?? existingSetting?.workspaceValue);
			}
		});
	}
}

export function convertRange(range: lsp.Range): vibecoda.Range {
	return new vibecoda.Range(range.start.line, range.start.character, range.end.line, range.end.character);
}

export function registerFindFileReferenceSupport(
	commandManager: CommandManager,
	client: MdLanguageClient,
): vibecoda.Disposable {
	return commandManager.register(new FindFileReferencesCommand(client));
}
