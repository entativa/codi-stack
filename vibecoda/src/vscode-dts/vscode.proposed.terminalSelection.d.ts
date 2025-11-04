/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

declare module 'vibecoda' {

	// https://github.com/microsoft/vibecoda/issues/188173

	export interface Terminal {
		/**
		 * The selected text of the terminal or undefined if there is no selection.
		 */
		readonly selection: string | undefined;
	}
}
