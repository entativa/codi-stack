/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as vibecoda from 'vibecoda';

/**
 * Minimal version of {@link vibecoda.TextDocument}.
 */
export interface ITextDocument {
	readonly uri: vibecoda.Uri;
	readonly version: number;

	getText(range?: vibecoda.Range): string;

	positionAt(offset: number): vibecoda.Position;
}

