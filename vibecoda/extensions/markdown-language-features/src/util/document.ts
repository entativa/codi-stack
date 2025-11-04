/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as vibecoda from 'vibecoda';
import { Schemes } from './schemes';
import { Utils } from 'vibecoda-uri';

export function getDocumentDir(uri: vibecoda.Uri): vibecoda.Uri | undefined {
	const docUri = getParentDocumentUri(uri);
	if (docUri.scheme === Schemes.untitled) {
		return vibecoda.workspace.workspaceFolders?.[0]?.uri;
	}
	return Utils.dirname(docUri);
}

export function getParentDocumentUri(uri: vibecoda.Uri): vibecoda.Uri {
	if (uri.scheme === Schemes.notebookCell) {
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
