/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
import * as vibecoda from 'vibecoda';

export function escapeAttribute(value: string | vibecoda.Uri): string {
	return value.toString().replace(/"/g, '&quot;');
}
