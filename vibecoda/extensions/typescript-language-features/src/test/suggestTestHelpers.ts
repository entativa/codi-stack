/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import 'mocha';
import * as vibecoda from 'vibecoda';
import { onChangedDocument, retryUntilDocumentChanges, wait } from './testUtils';

export async function acceptFirstSuggestion(uri: vibecoda.Uri, _disposables: vibecoda.Disposable[]) {
	return retryUntilDocumentChanges(uri, { retries: 10, timeout: 0 }, _disposables, async () => {
		await vibecoda.commands.executeCommand('editor.action.triggerSuggest');
		await wait(1000);
		await vibecoda.commands.executeCommand('acceptSelectedSuggestion');
	});
}

export async function typeCommitCharacter(uri: vibecoda.Uri, character: string, _disposables: vibecoda.Disposable[]) {
	const didChangeDocument = onChangedDocument(uri, _disposables);
	await vibecoda.commands.executeCommand('editor.action.triggerSuggest');
	await wait(3000); // Give time for suggestions to show
	await vibecoda.commands.executeCommand('type', { text: character });
	return await didChangeDocument;
}
