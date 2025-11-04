/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as vibecoda from 'vibecoda';
import type { ICompletionResource } from '../types';

export function createCompletionItem(cursorPosition: number, currentCommandString: string, commandResource: ICompletionResource, detail?: string, documentation?: string | vibecoda.MarkdownString, kind?: vibecoda.TerminalCompletionItemKind): vibecoda.TerminalCompletionItem {
	const endsWithSpace = currentCommandString.endsWith(' ');
	const lastWord = endsWithSpace ? '' : currentCommandString.split(' ').at(-1) ?? '';
	return {
		label: commandResource.label,
		detail: detail ?? commandResource.detail ?? '',
		documentation,
		replacementRange: [cursorPosition - lastWord.length, cursorPosition],
		kind: kind ?? commandResource.kind ?? vibecoda.TerminalCompletionItemKind.Method
	};
}
