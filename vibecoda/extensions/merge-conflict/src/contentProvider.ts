/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as vibecoda from 'vibecoda';

export default class MergeConflictContentProvider implements vibecoda.TextDocumentContentProvider, vibecoda.Disposable {

	static scheme = 'merge-conflict.conflict-diff';

	constructor(private context: vibecoda.ExtensionContext) {
	}

	begin() {
		this.context.subscriptions.push(
			vibecoda.workspace.registerTextDocumentContentProvider(MergeConflictContentProvider.scheme, this)
		);
	}

	dispose() {
	}

	async provideTextDocumentContent(uri: vibecoda.Uri): Promise<string | null> {
		try {
			const { scheme, ranges } = JSON.parse(uri.query) as { scheme: string; ranges: [{ line: number; character: number }[], { line: number; character: number }[]][] };

			// complete diff
			const document = await vibecoda.workspace.openTextDocument(uri.with({ scheme, query: '' }));

			let text = '';
			let lastPosition = new vibecoda.Position(0, 0);

			ranges.forEach(rangeObj => {
				const [conflictRange, fullRange] = rangeObj;
				const [start, end] = conflictRange;
				const [fullStart, fullEnd] = fullRange;

				text += document.getText(new vibecoda.Range(lastPosition.line, lastPosition.character, fullStart.line, fullStart.character));
				text += document.getText(new vibecoda.Range(start.line, start.character, end.line, end.character));
				lastPosition = new vibecoda.Position(fullEnd.line, fullEnd.character);
			});

			const documentEnd = document.lineAt(document.lineCount - 1).range.end;
			text += document.getText(new vibecoda.Range(lastPosition.line, lastPosition.character, documentEnd.line, documentEnd.character));

			return text;
		}
		catch (ex) {
			await vibecoda.window.showErrorMessage('Unable to show comparison');
			return null;
		}
	}
}