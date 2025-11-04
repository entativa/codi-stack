/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as vibecoda from 'vibecoda';
import { Utils } from 'vibecoda-uri';
import { Command } from '../commandManager';
import { createUriListSnippet, linkEditKind } from '../languageFeatures/copyFiles/shared';
import { mediaFileExtensions } from '../util/mimes';
import { coalesce } from '../util/arrays';
import { getParentDocumentUri } from '../util/document';
import { Schemes } from '../util/schemes';


export class InsertLinkFromWorkspace implements Command {
	public readonly id = 'markdown.editor.insertLinkFromWorkspace';

	public async execute(resources?: vibecoda.Uri[]) {
		const activeEditor = vibecoda.window.activeTextEditor;
		if (!activeEditor) {
			return;
		}

		resources ??= await vibecoda.window.showOpenDialog({
			canSelectFiles: true,
			canSelectFolders: false,
			canSelectMany: true,
			openLabel: vibecoda.l10n.t("Insert link"),
			title: vibecoda.l10n.t("Insert link"),
			defaultUri: getDefaultUri(activeEditor.document),
		});
		if (!resources) {
			return;
		}

		return insertLink(activeEditor, resources, false);
	}
}

export class InsertImageFromWorkspace implements Command {
	public readonly id = 'markdown.editor.insertImageFromWorkspace';

	public async execute(resources?: vibecoda.Uri[]) {
		const activeEditor = vibecoda.window.activeTextEditor;
		if (!activeEditor) {
			return;
		}

		resources ??= await vibecoda.window.showOpenDialog({
			canSelectFiles: true,
			canSelectFolders: false,
			canSelectMany: true,
			filters: {
				[vibecoda.l10n.t("Media")]: Array.from(mediaFileExtensions.keys())
			},
			openLabel: vibecoda.l10n.t("Insert image"),
			title: vibecoda.l10n.t("Insert image"),
			defaultUri: getDefaultUri(activeEditor.document),
		});
		if (!resources) {
			return;
		}

		return insertLink(activeEditor, resources, true);
	}
}

function getDefaultUri(document: vibecoda.TextDocument) {
	const docUri = getParentDocumentUri(document.uri);
	if (docUri.scheme === Schemes.untitled) {
		return vibecoda.workspace.workspaceFolders?.[0]?.uri;
	}
	return Utils.dirname(docUri);
}

async function insertLink(activeEditor: vibecoda.TextEditor, selectedFiles: readonly vibecoda.Uri[], insertAsMedia: boolean): Promise<void> {
	const edit = createInsertLinkEdit(activeEditor, selectedFiles, insertAsMedia);
	if (edit) {
		await vibecoda.workspace.applyEdit(edit);
	}
}

function createInsertLinkEdit(activeEditor: vibecoda.TextEditor, selectedFiles: readonly vibecoda.Uri[], insertAsMedia: boolean) {
	const snippetEdits = coalesce(activeEditor.selections.map((selection, i): vibecoda.SnippetTextEdit | undefined => {
		const selectionText = activeEditor.document.getText(selection);
		const snippet = createUriListSnippet(activeEditor.document.uri, selectedFiles.map(uri => ({ uri })), {
			linkKindHint: insertAsMedia ? 'media' : linkEditKind,
			placeholderText: selectionText,
			placeholderStartIndex: (i + 1) * selectedFiles.length,
			separator: insertAsMedia ? '\n' : ' ',
		});

		return snippet ? new vibecoda.SnippetTextEdit(selection, snippet.snippet) : undefined;
	}));
	if (!snippetEdits.length) {
		return;
	}

	const edit = new vibecoda.WorkspaceEdit();
	edit.set(activeEditor.document.uri, snippetEdits);
	return edit;
}
