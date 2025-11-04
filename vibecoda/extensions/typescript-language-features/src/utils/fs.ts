/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as vibecoda from 'vibecoda';

export async function exists(resource: vibecoda.Uri): Promise<boolean> {
	try {
		const stat = await vibecoda.workspace.fs.stat(resource);
		// stat.type is an enum flag
		return !!(stat.type & vibecoda.FileType.File);
	} catch {
		return false;
	}
}

export function looksLikeAbsoluteWindowsPath(path: string): boolean {
	return /^[a-zA-Z]:[\/\\]/.test(path);
}
