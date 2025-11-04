/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as vibecoda from 'vibecoda';
import { MdLanguageClient } from '../client/client';
import * as proto from '../client/protocol';

enum OpenMarkdownLinks {
	beside = 'beside',
	currentGroup = 'currentGroup',
}

export class MdLinkOpener {

	constructor(
		private readonly _client: MdLanguageClient,
	) { }

	public async resolveDocumentLink(linkText: string, fromResource: vibecoda.Uri): Promise<proto.ResolvedDocumentLinkTarget> {
		return this._client.resolveLinkTarget(linkText, fromResource);
	}

	public async openDocumentLink(linkText: string, fromResource: vibecoda.Uri, viewColumn?: vibecoda.ViewColumn): Promise<void> {
		const resolved = await this._client.resolveLinkTarget(linkText, fromResource);
		if (!resolved) {
			return;
		}

		const uri = vibecoda.Uri.from(resolved.uri);
		switch (resolved.kind) {
			case 'external':
				return vibecoda.commands.executeCommand('vibecoda.open', uri);

			case 'folder':
				return vibecoda.commands.executeCommand('revealInExplorer', uri);

			case 'file': {
				// If no explicit viewColumn is given, check if the editor is already open in a tab
				if (typeof viewColumn === 'undefined') {
					for (const tab of vibecoda.window.tabGroups.all.flatMap(x => x.tabs)) {
						if (tab.input instanceof vibecoda.TabInputText) {
							if (tab.input.uri.fsPath === uri.fsPath) {
								viewColumn = tab.group.viewColumn;
								break;
							}
						}
					}
				}

				return vibecoda.commands.executeCommand('vibecoda.open', uri, {
					selection: resolved.position ? new vibecoda.Range(resolved.position.line, resolved.position.character, resolved.position.line, resolved.position.character) : undefined,
					viewColumn: viewColumn ?? getViewColumn(fromResource),
				} satisfies vibecoda.TextDocumentShowOptions);
			}
		}
	}
}

function getViewColumn(resource: vibecoda.Uri): vibecoda.ViewColumn {
	const config = vibecoda.workspace.getConfiguration('markdown', resource);
	const openLinks = config.get<OpenMarkdownLinks>('links.openLocation', OpenMarkdownLinks.currentGroup);
	switch (openLinks) {
		case OpenMarkdownLinks.beside:
			return vibecoda.ViewColumn.Beside;
		case OpenMarkdownLinks.currentGroup:
		default:
			return vibecoda.ViewColumn.Active;
	}
}

