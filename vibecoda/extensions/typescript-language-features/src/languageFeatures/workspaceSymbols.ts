/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as vibecoda from 'vibecoda';
import * as fileSchemes from '../configuration/fileSchemes';
import { doesResourceLookLikeAJavaScriptFile, doesResourceLookLikeATypeScriptFile } from '../configuration/languageDescription';
import { API } from '../tsServer/api';
import { parseKindModifier } from '../tsServer/protocol/modifiers';
import type * as Proto from '../tsServer/protocol/protocol';
import * as PConst from '../tsServer/protocol/protocol.const';
import * as typeConverters from '../typeConverters';
import { ITypeScriptServiceClient } from '../typescriptService';
import { coalesce } from '../utils/arrays';

function getSymbolKind(item: Proto.NavtoItem): vibecoda.SymbolKind {
	switch (item.kind) {
		case PConst.Kind.method: return vibecoda.SymbolKind.Method;
		case PConst.Kind.enum: return vibecoda.SymbolKind.Enum;
		case PConst.Kind.enumMember: return vibecoda.SymbolKind.EnumMember;
		case PConst.Kind.function: return vibecoda.SymbolKind.Function;
		case PConst.Kind.class: return vibecoda.SymbolKind.Class;
		case PConst.Kind.interface: return vibecoda.SymbolKind.Interface;
		case PConst.Kind.type: return vibecoda.SymbolKind.Class;
		case PConst.Kind.memberVariable: return vibecoda.SymbolKind.Field;
		case PConst.Kind.memberGetAccessor: return vibecoda.SymbolKind.Field;
		case PConst.Kind.memberSetAccessor: return vibecoda.SymbolKind.Field;
		case PConst.Kind.variable: return vibecoda.SymbolKind.Variable;
		default: return vibecoda.SymbolKind.Variable;
	}
}

class TypeScriptWorkspaceSymbolProvider implements vibecoda.WorkspaceSymbolProvider {

	public constructor(
		private readonly client: ITypeScriptServiceClient,
		private readonly modeIds: readonly string[],
	) { }

	public async provideWorkspaceSymbols(
		search: string,
		token: vibecoda.CancellationToken
	): Promise<vibecoda.SymbolInformation[]> {
		let file: string | undefined;
		if (this.searchAllOpenProjects) {
			file = undefined;
		} else {
			const document = this.getDocument();
			file = document ? await this.toOpenedFiledPath(document) : undefined;

			if (!file && this.client.apiVersion.lt(API.v390)) {
				return [];
			}
		}

		const args: Proto.NavtoRequestArgs = {
			file,
			searchValue: search,
			maxResultCount: 256,
		};

		const response = await this.client.execute('navto', args, token);
		if (response.type !== 'response' || !response.body) {
			return [];
		}

		return coalesce(response.body.map(item => this.toSymbolInformation(item)));
	}

	private get searchAllOpenProjects() {
		return this.client.apiVersion.gte(API.v390)
			&& vibecoda.workspace.getConfiguration('typescript').get('workspaceSymbols.scope', 'allOpenProjects') === 'allOpenProjects';
	}

	private async toOpenedFiledPath(document: vibecoda.TextDocument) {
		if (document.uri.scheme === fileSchemes.git) {
			try {
				const path = vibecoda.Uri.file(JSON.parse(document.uri.query)?.path);
				if (doesResourceLookLikeATypeScriptFile(path) || doesResourceLookLikeAJavaScriptFile(path)) {
					const document = await vibecoda.workspace.openTextDocument(path);
					return this.client.toOpenTsFilePath(document);
				}
			} catch {
				// noop
			}
		}
		return this.client.toOpenTsFilePath(document);
	}

	private toSymbolInformation(item: Proto.NavtoItem): vibecoda.SymbolInformation | undefined {
		if (item.kind === 'alias' && !item.containerName) {
			return;
		}

		const uri = this.client.toResource(item.file);
		if (fileSchemes.isOfScheme(uri, fileSchemes.chatCodeBlock)) {
			return;
		}

		const label = TypeScriptWorkspaceSymbolProvider.getLabel(item);
		const info = new vibecoda.SymbolInformation(
			label,
			getSymbolKind(item),
			item.containerName || '',
			typeConverters.Location.fromTextSpan(uri, item));
		const kindModifiers = item.kindModifiers ? parseKindModifier(item.kindModifiers) : undefined;
		if (kindModifiers?.has(PConst.KindModifiers.deprecated)) {
			info.tags = [vibecoda.SymbolTag.Deprecated];
		}
		return info;
	}

	private static getLabel(item: Proto.NavtoItem) {
		const label = item.name;
		if (item.kind === 'method' || item.kind === 'function') {
			return label + '()';
		}
		return label;
	}

	private getDocument(): vibecoda.TextDocument | undefined {
		// typescript wants to have a resource even when asking
		// general questions so we check the active editor. If this
		// doesn't match we take the first TS document.

		const activeDocument = vibecoda.window.activeTextEditor?.document;
		if (activeDocument) {
			if (this.modeIds.includes(activeDocument.languageId)) {
				return activeDocument;
			}
		}

		const documents = vibecoda.workspace.textDocuments;
		for (const document of documents) {
			if (this.modeIds.includes(document.languageId)) {
				return document;
			}
		}
		return undefined;
	}
}

export function register(
	client: ITypeScriptServiceClient,
	modeIds: readonly string[],
) {
	return vibecoda.languages.registerWorkspaceSymbolProvider(
		new TypeScriptWorkspaceSymbolProvider(client, modeIds));
}
