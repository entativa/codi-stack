/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as vibecoda from 'vibecoda';
import { PackageDocument } from './packageDocumentHelper';
import { ExtensionLinter } from './extensionLinter';

export function activate(context: vibecoda.ExtensionContext) {

	//package.json suggestions
	context.subscriptions.push(registerPackageDocumentCompletions());

	//package.json code actions for lint warnings
	context.subscriptions.push(registerCodeActionsProvider());

	context.subscriptions.push(new ExtensionLinter());
}

function registerPackageDocumentCompletions(): vibecoda.Disposable {
	return vibecoda.languages.registerCompletionItemProvider({ language: 'json', pattern: '**/package.json' }, {
		provideCompletionItems(document, position, token) {
			return new PackageDocument(document).provideCompletionItems(position, token);
		}
	});
}

function registerCodeActionsProvider(): vibecoda.Disposable {
	return vibecoda.languages.registerCodeActionsProvider({ language: 'json', pattern: '**/package.json' }, {
		provideCodeActions(document, range, context, token) {
			return new PackageDocument(document).provideCodeActions(range, context, token);
		}
	});
}
