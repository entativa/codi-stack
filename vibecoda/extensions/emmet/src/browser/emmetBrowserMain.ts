/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as vibecoda from 'vibecoda';
import { activateEmmetExtension } from '../emmetCommon';

export function activate(context: vibecoda.ExtensionContext) {
	activateEmmetExtension(context);
}
