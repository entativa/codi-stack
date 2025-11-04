/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

declare module 'vibecoda' {

	// @alexr00 https://github.com/microsoft/vibecoda/issues/201131

	export interface CommentReaction {
		readonly reactors?: readonly CommentAuthorInformation[];
	}
}
