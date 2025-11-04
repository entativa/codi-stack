/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import { TextDocument } from 'vibecoda-languageserver-textdocument';
import * as vibecoda from 'vibecoda';
import { ITextDocument } from '../types/textDocument';

export class InMemoryDocument implements ITextDocument {

	private readonly _doc: TextDocument;

	public readonly uri: vibecoda.Uri;
	public readonly version: number;

	constructor(
		uri: vibecoda.Uri,
		contents: string,
		version: number = 0,
	) {
		this.uri = uri;
		this.version = version;
		this._doc = TextDocument.create(this.uri.toString(), 'markdown', 0, contents);
	}

	getText(range?: vibecoda.Range): string {
		return this._doc.getText(range);
	}

	positionAt(offset: number): vibecoda.Position {
		const pos = this._doc.positionAt(offset);
		return new vibecoda.Position(pos.line, pos.character);
	}
}
