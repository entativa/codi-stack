/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

declare module 'vibecoda' {

	// https://github.com/microsoft/vibecoda/issues/106744

	export interface NotebookCellOutput {
		/**
		 * @deprecated
		 */
		id: string;
	}
}
