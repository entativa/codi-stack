/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as vibecoda from 'vibecoda';

declare global {
	var testExtensionContext: vibecoda.ExtensionContext;
}

export function activate(_context: vibecoda.ExtensionContext) {
	// Set context as a global as some tests depend on it
	global.testExtensionContext = _context;
}
