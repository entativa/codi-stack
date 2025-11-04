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


class FileReferencesCommand implements Command {

	public static readonly context = 'tsSupportsFileReferences';
	public static readonly minVersion = API.v420;

	public readonly id = 'typescript.findAllFileReferences';

	public constructor(
		private readonly client: ITypeScriptServiceClient
	) { }

	public async execute(resource?: vibecoda.Uri) {
		if (this.client.apiVersion.lt(FileReferencesCommand.minVersion)) {
			vibecoda.window.showErrorMessage(vibecoda.l10n.t("Find file references failed. Requires TypeScript 4.2+."));
			return;
		}

		resource ??= vibecoda.window.activeTextEditor?.document.uri;
		if (!resource) {
			vibecoda.window.showErrorMessage(vibecoda.l10n.t("Find file references failed. No resource provided."));
			return;
		}

		const document = await vibecoda.workspace.openTextDocument(resource);
		if (!isSupportedLanguageMode(document)) {
			vibecoda.window.showErrorMessage(vibecoda.l10n.t("Find file references failed. Unsupported file type."));
			return;
		}

		const openedFiledPath = this.client.toOpenTsFilePath(document);
		if (!openedFiledPath) {
			vibecoda.window.showErrorMessage(vibecoda.l10n.t("Find file references failed. Unknown file type."));
			return;
		}

		await vibecoda.window.withProgress({
			location: vibecoda.ProgressLocation.Window,
			title: vibecoda.l10n.t("Finding file references")
		}, async (_progress, token) => {

			const response = await this.client.execute('fileReferences', {
				file: openedFiledPath
			}, token);
			if (response.type !== 'response' || !response.body) {
				return;
			}

			const locations: vibecoda.Location[] = response.body.refs.map(reference =>
				typeConverters.Location.fromTextSpan(this.client.toResource(reference.file), reference));

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


export function register(
	client: ITypeScriptServiceClient,
	commandManager: CommandManager
) {
	function updateContext(overrideValue?: boolean) {
		vibecoda.commands.executeCommand('setContext', FileReferencesCommand.context, overrideValue ?? client.apiVersion.gte(FileReferencesCommand.minVersion));
	}
	updateContext();

	commandManager.register(new FileReferencesCommand(client));
	return vibecoda.Disposable.from(
		client.onTsServerStarted(() => updateContext()),
		new vibecoda.Disposable(() => updateContext(false)),
	);
}
