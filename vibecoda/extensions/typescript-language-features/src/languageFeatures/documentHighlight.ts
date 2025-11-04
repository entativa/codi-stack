/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as vibecoda from 'vibecoda';
import { DocumentSelector } from '../configuration/documentSelector';
import type * as Proto from '../tsServer/protocol/protocol';
import * as typeConverters from '../typeConverters';
import { ITypeScriptServiceClient } from '../typescriptService';

class TypeScriptDocumentHighlightProvider implements vibecoda.DocumentHighlightProvider, vibecoda.MultiDocumentHighlightProvider {
	public constructor(
		private readonly client: ITypeScriptServiceClient
	) { }

	public async provideMultiDocumentHighlights(
		document: vibecoda.TextDocument,
		position: vibecoda.Position,
		otherDocuments: vibecoda.TextDocument[],
		token: vibecoda.CancellationToken
	): Promise<vibecoda.MultiDocumentHighlight[]> {
		const allFiles = [document, ...otherDocuments].map(doc => this.client.toOpenTsFilePath(doc)).filter(file => !!file) as string[];
		const file = this.client.toOpenTsFilePath(document);

		if (!file || allFiles.length === 0) {
			return [];
		}

		const args = {
			...typeConverters.Position.toFileLocationRequestArgs(file, position),
			filesToSearch: allFiles
		};
		const response = await this.client.execute('documentHighlights', args, token);
		if (response.type !== 'response' || !response.body) {
			return [];
		}

		const result = response.body.map(highlightItem =>
			new vibecoda.MultiDocumentHighlight(
				vibecoda.Uri.file(highlightItem.file),
				[...convertDocumentHighlight(highlightItem)]
			)
		);

		return result;
	}

	public async provideDocumentHighlights(
		document: vibecoda.TextDocument,
		position: vibecoda.Position,
		token: vibecoda.CancellationToken
	): Promise<vibecoda.DocumentHighlight[]> {
		const file = this.client.toOpenTsFilePath(document);
		if (!file) {
			return [];
		}

		const args = {
			...typeConverters.Position.toFileLocationRequestArgs(file, position),
			filesToSearch: [file]
		};
		const response = await this.client.execute('documentHighlights', args, token);
		if (response.type !== 'response' || !response.body) {
			return [];
		}

		return response.body.flatMap(convertDocumentHighlight);
	}
}

function convertDocumentHighlight(highlight: Proto.DocumentHighlightsItem): ReadonlyArray<vibecoda.DocumentHighlight> {
	return highlight.highlightSpans.map(span =>
		new vibecoda.DocumentHighlight(
			typeConverters.Range.fromTextSpan(span),
			span.kind === 'writtenReference' ? vibecoda.DocumentHighlightKind.Write : vibecoda.DocumentHighlightKind.Read));
}

export function register(
	selector: DocumentSelector,
	client: ITypeScriptServiceClient,
) {
	const provider = new TypeScriptDocumentHighlightProvider(client);

	return vibecoda.Disposable.from(
		vibecoda.languages.registerDocumentHighlightProvider(selector.syntax, provider),
		vibecoda.languages.registerMultiDocumentHighlightProvider(selector.syntax, provider)
	);
}
