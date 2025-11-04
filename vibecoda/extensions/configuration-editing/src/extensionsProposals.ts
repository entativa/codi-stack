/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as vibecoda from 'vibecoda';


export async function provideInstalledExtensionProposals(existing: string[], additionalText: string, range: vibecoda.Range, includeBuiltinExtensions: boolean): Promise<vibecoda.CompletionItem[] | vibecoda.CompletionList> {
	if (Array.isArray(existing)) {
		const extensions = includeBuiltinExtensions ? vibecoda.extensions.all : vibecoda.extensions.all.filter(e => !(e.id.startsWith('vibecoda.') || e.id === 'Microsoft.vibecoda-markdown'));
		const knownExtensionProposals = extensions.filter(e => existing.indexOf(e.id) === -1);
		if (knownExtensionProposals.length) {
			return knownExtensionProposals.map(e => {
				const item = new vibecoda.CompletionItem(e.id);
				const insertText = `"${e.id}"${additionalText}`;
				item.kind = vibecoda.CompletionItemKind.Value;
				item.insertText = insertText;
				item.range = range;
				item.filterText = insertText;
				return item;
			});
		} else {
			const example = new vibecoda.CompletionItem(vibecoda.l10n.t("Example"));
			example.insertText = '"vibecoda.csharp"';
			example.kind = vibecoda.CompletionItemKind.Value;
			example.range = range;
			return [example];
		}
	}
	return [];
}
