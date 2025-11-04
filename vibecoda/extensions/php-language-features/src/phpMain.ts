/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as vibecoda from 'vibecoda';

import PHPCompletionItemProvider from './features/completionItemProvider';
import PHPHoverProvider from './features/hoverProvider';
import PHPSignatureHelpProvider from './features/signatureHelpProvider';
import PHPValidationProvider from './features/validationProvider';

export function activate(context: vibecoda.ExtensionContext): any {

	const validator = new PHPValidationProvider();
	validator.activate(context.subscriptions);

	// add providers
	context.subscriptions.push(vibecoda.languages.registerCompletionItemProvider('php', new PHPCompletionItemProvider(), '>', '$'));
	context.subscriptions.push(vibecoda.languages.registerHoverProvider('php', new PHPHoverProvider()));
	context.subscriptions.push(vibecoda.languages.registerSignatureHelpProvider('php', new PHPSignatureHelpProvider(), '(', ','));
}
