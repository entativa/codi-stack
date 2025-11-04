/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as vibecoda from 'vibecoda';
import { Utils } from 'vibecoda-uri';

export const Schemes = Object.freeze({
	file: 'file',
	notebookCell: 'vibecoda-notebook-cell',
	untitled: 'untitled',
});

export const Mimes = Object.freeze({
	plain: 'text/plain',
	uriList: 'text/uri-list',
});


export function getDocumentDir(uri: vibecoda.Uri): vibecoda.Uri | undefined {
	const docUri = getParentDocumentUri(uri);
	if (docUri.scheme === Schemes.untitled) {
		return vibecoda.workspace.workspaceFolders?.[0]?.uri;
	}
	return Utils.dirname(docUri);
}

function getParentDocumentUri(uri: vibecoda.Uri): vibecoda.Uri {
	if (uri.scheme === Schemes.notebookCell) {
		// is notebook documents necessary?
		for (const notebook of vibecoda.workspace.notebookDocuments) {
			for (const cell of notebook.getCells()) {
				if (cell.document.uri.toString() === uri.toString()) {
					return notebook.uri;
				}
			}
		}
	}

	return uri;
}
