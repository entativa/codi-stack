/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as vibecoda from 'vibecoda';
import { MdLanguageClient } from '../client/client';
import { Mime } from '../util/mimes';

class UpdatePastedLinksEditProvider implements vibecoda.DocumentPasteEditProvider {

	public static readonly kind = vibecoda.DocumentDropOrPasteEditKind.Text.append('updateLinks', 'markdown');

	public static readonly metadataMime = 'application/vnd.vibecoda.markdown.updatelinks.metadata';

	constructor(
		private readonly _client: MdLanguageClient,
	) { }

	async prepareDocumentPaste(document: vibecoda.TextDocument, ranges: readonly vibecoda.Range[], dataTransfer: vibecoda.DataTransfer, token: vibecoda.CancellationToken): Promise<void> {
		if (!this._isEnabled(document)) {
			return;
		}

		const metadata = await this._client.prepareUpdatePastedLinks(document.uri, ranges, token);
		if (token.isCancellationRequested) {
			return;
		}

		dataTransfer.set(UpdatePastedLinksEditProvider.metadataMime, new vibecoda.DataTransferItem(metadata));
	}

	async provideDocumentPasteEdits(
		document: vibecoda.TextDocument,
		ranges: readonly vibecoda.Range[],
		dataTransfer: vibecoda.DataTransfer,
		context: vibecoda.DocumentPasteEditContext,
		token: vibecoda.CancellationToken,
	): Promise<vibecoda.DocumentPasteEdit[] | undefined> {
		if (!this._isEnabled(document)) {
			return;
		}

		const metadata = dataTransfer.get(UpdatePastedLinksEditProvider.metadataMime)?.value;
		if (!metadata) {
			return;
		}

		const textItem = dataTransfer.get(Mime.textPlain);
		const text = await textItem?.asString();
		if (!text || token.isCancellationRequested) {
			return;
		}

		// TODO: Handle cases such as:
		// - copy empty line
		// - Copy with multiple cursors and paste into multiple locations
		// - ...
		const edits = await this._client.getUpdatePastedLinksEdit(document.uri, ranges.map(x => new vibecoda.TextEdit(x, text)), metadata, token);
		if (!edits?.length || token.isCancellationRequested) {
			return;
		}

		const pasteEdit = new vibecoda.DocumentPasteEdit('', vibecoda.l10n.t("Paste and update pasted links"), UpdatePastedLinksEditProvider.kind);
		const workspaceEdit = new vibecoda.WorkspaceEdit();
		workspaceEdit.set(document.uri, edits.map(x => new vibecoda.TextEdit(new vibecoda.Range(x.range.start.line, x.range.start.character, x.range.end.line, x.range.end.character,), x.newText)));
		pasteEdit.additionalEdit = workspaceEdit;

		if (!context.only || !UpdatePastedLinksEditProvider.kind.contains(context.only)) {
			pasteEdit.yieldTo = [vibecoda.DocumentDropOrPasteEditKind.Text];
		}

		return [pasteEdit];
	}

	private _isEnabled(document: vibecoda.TextDocument): boolean {
		return vibecoda.workspace.getConfiguration('markdown', document.uri).get<boolean>('editor.updateLinksOnPaste.enabled', true);
	}
}

export function registerUpdatePastedLinks(selector: vibecoda.DocumentSelector, client: MdLanguageClient) {
	return vibecoda.languages.registerDocumentPasteEditProvider(selector, new UpdatePastedLinksEditProvider(client), {
		copyMimeTypes: [UpdatePastedLinksEditProvider.metadataMime],
		providedPasteEditKinds: [UpdatePastedLinksEditProvider.kind],
		pasteMimeTypes: [UpdatePastedLinksEditProvider.metadataMime],
	});
}
