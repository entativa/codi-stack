/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as vibecoda from 'vibecoda';
import { LanguageClient, ServerOptions, TransportKind } from 'vibecoda-languageclient/node';
import { MdLanguageClient, startClient } from './client/client';
import { activateShared } from './extension.shared';
import { VsCodeOutputLogger } from './logging';
import { IMdParser, MarkdownItEngine } from './markdownEngine';
import { getMarkdownExtensionContributions } from './markdownExtensions';
import { githubSlugifier } from './slugify';

export async function activate(context: vibecoda.ExtensionContext) {
	const contributions = getMarkdownExtensionContributions(context);
	context.subscriptions.push(contributions);

	const logger = new VsCodeOutputLogger();
	context.subscriptions.push(logger);

	const engine = new MarkdownItEngine(contributions, githubSlugifier, logger);

	const client = await startServer(context, engine);
	context.subscriptions.push(client);
	activateShared(context, client, engine, logger, contributions);
}

function startServer(context: vibecoda.ExtensionContext, parser: IMdParser): Promise<MdLanguageClient> {
	const isDebugBuild = context.extension.packageJSON.main.includes('/out/');

	const serverModule = context.asAbsolutePath(
		isDebugBuild
			// For local non bundled version of vibecoda-markdown-languageserver
			// ? './node_modules/vibecoda-markdown-languageserver/out/node/workerMain'
			? './node_modules/vibecoda-markdown-languageserver/dist/node/workerMain'
			: './dist/serverWorkerMain'
	);

	// The debug options for the server
	const debugOptions = { execArgv: ['--nolazy', '--inspect=' + (7000 + Math.round(Math.random() * 999))] };

	// If the extension is launch in debug mode the debug server options are use
	// Otherwise the run options are used
	const serverOptions: ServerOptions = {
		run: { module: serverModule, transport: TransportKind.ipc },
		debug: { module: serverModule, transport: TransportKind.ipc, options: debugOptions }
	};

	// pass the location of the localization bundle to the server
	process.env['VSCODE_L10N_BUNDLE_LOCATION'] = vibecoda.l10n.uri?.toString() ?? '';

	return startClient((id, name, clientOptions) => {
		return new LanguageClient(id, name, serverOptions, clientOptions);
	}, parser);
}
