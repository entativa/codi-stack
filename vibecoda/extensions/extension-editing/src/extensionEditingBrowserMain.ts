/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as vibecoda from 'vibecoda';
import { PackageDocument } from './packageDocumentHelper';

export function activate(context: vibecoda.ExtensionContext) {
	//package.json suggestions
	context.subscriptions.push(registerPackageDocumentCompletions());

}

function registerPackageDocumentCompletions(): vibecoda.Disposable {
	return vibecoda.languages.registerCompletionItemProvider({ language: 'json', pattern: '**/package.json' }, {
		provideCompletionItems(document, position, token) {
			return new PackageDocument(document).provideCompletionItems(position, token);
		}
	});

}
