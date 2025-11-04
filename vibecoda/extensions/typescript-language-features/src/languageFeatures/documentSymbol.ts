/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as vibecoda from 'vibecoda';
import { DocumentSelector } from '../configuration/documentSelector';
import { CachedResponse } from '../tsServer/cachedResponse';
import { parseKindModifier } from '../tsServer/protocol/modifiers';
import type * as Proto from '../tsServer/protocol/protocol';
import * as PConst from '../tsServer/protocol/protocol.const';
import * as typeConverters from '../typeConverters';
import { ITypeScriptServiceClient } from '../typescriptService';

const getSymbolKind = (kind: string): vibecoda.SymbolKind => {
	switch (kind) {
		case PConst.Kind.module: return vibecoda.SymbolKind.Module;
		case PConst.Kind.class: return vibecoda.SymbolKind.Class;
		case PConst.Kind.enum: return vibecoda.SymbolKind.Enum;
		case PConst.Kind.interface: return vibecoda.SymbolKind.Interface;
		case PConst.Kind.method: return vibecoda.SymbolKind.Method;
		case PConst.Kind.memberVariable: return vibecoda.SymbolKind.Property;
		case PConst.Kind.memberGetAccessor: return vibecoda.SymbolKind.Property;
		case PConst.Kind.memberSetAccessor: return vibecoda.SymbolKind.Property;
		case PConst.Kind.variable: return vibecoda.SymbolKind.Variable;
		case PConst.Kind.const: return vibecoda.SymbolKind.Variable;
		case PConst.Kind.localVariable: return vibecoda.SymbolKind.Variable;
		case PConst.Kind.function: return vibecoda.SymbolKind.Function;
		case PConst.Kind.localFunction: return vibecoda.SymbolKind.Function;
		case PConst.Kind.constructSignature: return vibecoda.SymbolKind.Constructor;
		case PConst.Kind.constructorImplementation: return vibecoda.SymbolKind.Constructor;
	}
	return vibecoda.SymbolKind.Variable;
};

class TypeScriptDocumentSymbolProvider implements vibecoda.DocumentSymbolProvider {

	public constructor(
		private readonly client: ITypeScriptServiceClient,
		private readonly cachedResponse: CachedResponse<Proto.NavTreeResponse>,
	) { }

	public async provideDocumentSymbols(document: vibecoda.TextDocument, token: vibecoda.CancellationToken): Promise<vibecoda.DocumentSymbol[] | undefined> {
		const file = this.client.toOpenTsFilePath(document);
		if (!file) {
			return undefined;
		}

		const args: Proto.FileRequestArgs = { file };
		const response = await this.cachedResponse.execute(document, () => this.client.execute('navtree', args, token));
		if (response.type !== 'response' || !response.body?.childItems) {
			return undefined;
		}

		// The root represents the file. Ignore this when showing in the UI
		const result: vibecoda.DocumentSymbol[] = [];
		for (const item of response.body.childItems) {
			TypeScriptDocumentSymbolProvider.convertNavTree(document.uri, result, item);
		}
		return result;
	}

	private static convertNavTree(
		resource: vibecoda.Uri,
		output: vibecoda.DocumentSymbol[],
		item: Proto.NavigationTree,
	): boolean {
		let shouldInclude = TypeScriptDocumentSymbolProvider.shouldInclueEntry(item);
		if (!shouldInclude && !item.childItems?.length) {
			return false;
		}

		const children = new Set(item.childItems || []);
		for (const span of item.spans) {
			const range = typeConverters.Range.fromTextSpan(span);
			const symbolInfo = TypeScriptDocumentSymbolProvider.convertSymbol(item, range);

			for (const child of children) {
				if (child.spans.some(span => !!range.intersection(typeConverters.Range.fromTextSpan(span)))) {
					const includedChild = TypeScriptDocumentSymbolProvider.convertNavTree(resource, symbolInfo.children, child);
					shouldInclude = shouldInclude || includedChild;
					children.delete(child);
				}
			}

			if (shouldInclude) {
				output.push(symbolInfo);
			}
		}

		return shouldInclude;
	}

	private static convertSymbol(item: Proto.NavigationTree, range: vibecoda.Range): vibecoda.DocumentSymbol {
		const selectionRange = item.nameSpan ? typeConverters.Range.fromTextSpan(item.nameSpan) : range;
		let label = item.text;

		switch (item.kind) {
			case PConst.Kind.memberGetAccessor: label = `(get) ${label}`; break;
			case PConst.Kind.memberSetAccessor: label = `(set) ${label}`; break;
		}

		const symbolInfo = new vibecoda.DocumentSymbol(
			label,
			'',
			getSymbolKind(item.kind),
			range,
			range.contains(selectionRange) ? selectionRange : range);


		const kindModifiers = parseKindModifier(item.kindModifiers);
		if (kindModifiers.has(PConst.KindModifiers.deprecated)) {
			symbolInfo.tags = [vibecoda.SymbolTag.Deprecated];
		}

		return symbolInfo;
	}

	private static shouldInclueEntry(item: Proto.NavigationTree | Proto.NavigationBarItem): boolean {
		if (item.kind === PConst.Kind.alias) {
			return false;
		}
		return !!(item.text && item.text !== '<function>' && item.text !== '<class>');
	}
}

export function register(
	selector: DocumentSelector,
	client: ITypeScriptServiceClient,
	cachedResponse: CachedResponse<Proto.NavTreeResponse>,
) {
	return vibecoda.languages.registerDocumentSymbolProvider(selector.syntax,
		new TypeScriptDocumentSymbolProvider(client, cachedResponse), { label: 'TypeScript' });
}
