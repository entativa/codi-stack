/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as vibecoda from 'vibecoda';

export interface ICompletionResource {
	label: string | vibecoda.CompletionItemLabel;
	/**
	 * The definition command of the completion, this will be the resolved value of an alias
	 * completion.
	 */
	definitionCommand?: string;
	documentation?: string | vibecoda.MarkdownString;
	detail?: string;
	kind?: vibecoda.TerminalCompletionItemKind;
}
