/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as vibecoda from 'vibecoda';
import { Node } from 'EmmetFlatNode';
import { getFlatNode, offsetRangeToVsRange, validate } from './util';
import { getRootNode } from './parseDocument';

export function mergeLines() {
	if (!validate(false) || !vibecoda.window.activeTextEditor) {
		return;
	}

	const editor = vibecoda.window.activeTextEditor;

	const rootNode = getRootNode(editor.document, true);
	if (!rootNode) {
		return;
	}

	return editor.edit(editBuilder => {
		Array.from(editor.selections).reverse().forEach(selection => {
			const textEdit = getRangesToReplace(editor.document, selection, rootNode);
			if (textEdit) {
				editBuilder.replace(textEdit.range, textEdit.newText);
			}
		});
	});
}

function getRangesToReplace(document: vibecoda.TextDocument, selection: vibecoda.Selection, rootNode: Node): vibecoda.TextEdit | undefined {
	let startNodeToUpdate: Node | undefined;
	let endNodeToUpdate: Node | undefined;

	const selectionStart = document.offsetAt(selection.start);
	const selectionEnd = document.offsetAt(selection.end);
	if (selection.isEmpty) {
		startNodeToUpdate = endNodeToUpdate = getFlatNode(rootNode, selectionStart, true);
	} else {
		startNodeToUpdate = getFlatNode(rootNode, selectionStart, true);
		endNodeToUpdate = getFlatNode(rootNode, selectionEnd, true);
	}

	if (!startNodeToUpdate || !endNodeToUpdate) {
		return;
	}

	const startPos = document.positionAt(startNodeToUpdate.start);
	const startLine = startPos.line;
	const startChar = startPos.character;
	const endPos = document.positionAt(endNodeToUpdate.end);
	const endLine = endPos.line;
	if (startLine === endLine) {
		return;
	}

	const rangeToReplace = offsetRangeToVsRange(document, startNodeToUpdate.start, endNodeToUpdate.end);
	let textToReplaceWith = document.lineAt(startLine).text.substr(startChar);
	for (let i = startLine + 1; i <= endLine; i++) {
		textToReplaceWith += document.lineAt(i).text.trim();
	}

	return new vibecoda.TextEdit(rangeToReplace, textToReplaceWith);
}
