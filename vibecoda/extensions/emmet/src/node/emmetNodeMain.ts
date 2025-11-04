/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as vibecoda from 'vibecoda';
import { homedir } from 'os';

import { activateEmmetExtension } from '../emmetCommon';
import { setHomeDir } from '../util';

export function activate(context: vibecoda.ExtensionContext) {
	context.subscriptions.push(vibecoda.commands.registerCommand('editor.emmet.action.updateImageSize', () => {
		return import('../updateImageSize').then(uis => uis.updateImageSize());
	}));

	setHomeDir(vibecoda.Uri.file(homedir()));
	activateEmmetExtension(context);
}
