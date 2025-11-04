/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as vibecoda from 'vibecoda';

export interface WebviewResourceProvider {
	asWebviewUri(resource: vibecoda.Uri): vibecoda.Uri;

	readonly cspSource: string;
}

