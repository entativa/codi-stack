/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as vibecoda from 'vibecoda';

export const typescript = 'typescript';
export const typescriptreact = 'typescriptreact';
export const javascript = 'javascript';
export const javascriptreact = 'javascriptreact';
export const jsxTags = 'jsx-tags';

export const jsTsLanguageModes = [
	javascript,
	javascriptreact,
	typescript,
	typescriptreact,
];

export function isSupportedLanguageMode(doc: vibecoda.TextDocument) {
	return vibecoda.languages.match([typescript, typescriptreact, javascript, javascriptreact], doc) > 0;
}

export function isTypeScriptDocument(doc: vibecoda.TextDocument) {
	return vibecoda.languages.match([typescript, typescriptreact], doc) > 0;
}
