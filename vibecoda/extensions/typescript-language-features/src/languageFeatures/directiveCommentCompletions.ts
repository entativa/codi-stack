/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as vibecoda from 'vibecoda';
import { DocumentSelector } from '../configuration/documentSelector';
import { API } from '../tsServer/api';
import { ITypeScriptServiceClient } from '../typescriptService';


interface Directive {
	readonly value: string;
	readonly description: string;
}

const tsDirectives: Directive[] = [
	{
		value: '@ts-check',
		description: vibecoda.l10n.t("Enables semantic checking in a JavaScript file. Must be at the top of a file.")
	}, {
		value: '@ts-nocheck',
		description: vibecoda.l10n.t("Disables semantic checking in a JavaScript file. Must be at the top of a file.")
	}, {
		value: '@ts-ignore',
		description: vibecoda.l10n.t("Suppresses @ts-check errors on the next line of a file.")
	}
];

const tsDirectives390: Directive[] = [
	...tsDirectives,
	{
		value: '@ts-expect-error',
		description: vibecoda.l10n.t("Suppresses @ts-check errors on the next line of a file, expecting at least one to exist.")
	}
];

class DirectiveCommentCompletionProvider implements vibecoda.CompletionItemProvider {

	constructor(
		private readonly client: ITypeScriptServiceClient,
	) { }

	public provideCompletionItems(
		document: vibecoda.TextDocument,
		position: vibecoda.Position,
		_token: vibecoda.CancellationToken
	): vibecoda.CompletionItem[] {
		const file = this.client.toOpenTsFilePath(document);
		if (!file) {
			return [];
		}

		const line = document.lineAt(position.line).text;
		const prefix = line.slice(0, position.character);
		const match = prefix.match(/^\s*\/\/+\s?(@[a-zA-Z\-]*)?$/);
		if (match) {
			const directives = this.client.apiVersion.gte(API.v390)
				? tsDirectives390
				: tsDirectives;

			return directives.map(directive => {
				const item = new vibecoda.CompletionItem(directive.value, vibecoda.CompletionItemKind.Snippet);
				item.detail = directive.description;
				item.range = new vibecoda.Range(position.line, Math.max(0, position.character - (match[1] ? match[1].length : 0)), position.line, position.character);
				return item;
			});
		}
		return [];
	}
}

export function register(
	selector: DocumentSelector,
	client: ITypeScriptServiceClient,
) {
	return vibecoda.languages.registerCompletionItemProvider(selector.syntax,
		new DirectiveCommentCompletionProvider(client),
		'@');
}
