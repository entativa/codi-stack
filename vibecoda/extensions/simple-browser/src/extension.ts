/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as vibecoda from 'vibecoda';
import { SimpleBrowserManager } from './simpleBrowserManager';
import { SimpleBrowserView } from './simpleBrowserView';

declare class URL {
	constructor(input: string, base?: string | URL);
	hostname: string;
}

const openApiCommand = 'simpleBrowser.api.open';
const showCommand = 'simpleBrowser.show';

const enabledHosts = new Set<string>([
	'localhost',
	// localhost IPv4
	'127.0.0.1',
	// localhost IPv6
	'[0:0:0:0:0:0:0:1]',
	'[::1]',
	// all interfaces IPv4
	'0.0.0.0',
	// all interfaces IPv6
	'[0:0:0:0:0:0:0:0]',
	'[::]'
]);

const openerId = 'simpleBrowser.open';

export function activate(context: vibecoda.ExtensionContext) {

	const manager = new SimpleBrowserManager(context.extensionUri);
	context.subscriptions.push(manager);

	context.subscriptions.push(vibecoda.window.registerWebviewPanelSerializer(SimpleBrowserView.viewType, {
		deserializeWebviewPanel: async (panel, state) => {
			manager.restore(panel, state);
		}
	}));

	context.subscriptions.push(vibecoda.commands.registerCommand(showCommand, async (url?: string) => {
		if (!url) {
			url = await vibecoda.window.showInputBox({
				placeHolder: vibecoda.l10n.t("https://example.com"),
				prompt: vibecoda.l10n.t("Enter url to visit")
			});
		}

		if (url) {
			manager.show(url);
		}
	}));

	context.subscriptions.push(vibecoda.commands.registerCommand(openApiCommand, (url: vibecoda.Uri, showOptions?: {
		preserveFocus?: boolean;
		viewColumn: vibecoda.ViewColumn;
	}) => {
		manager.show(url, showOptions);
	}));

	context.subscriptions.push(vibecoda.window.registerExternalUriOpener(openerId, {
		canOpenExternalUri(uri: vibecoda.Uri) {
			// We have to replace the IPv6 hosts with IPv4 because URL can't handle IPv6.
			const originalUri = new URL(uri.toString(true));
			if (enabledHosts.has(originalUri.hostname)) {
				return isWeb()
					? vibecoda.ExternalUriOpenerPriority.Default
					: vibecoda.ExternalUriOpenerPriority.Option;
			}

			return vibecoda.ExternalUriOpenerPriority.None;
		},
		openExternalUri(resolveUri: vibecoda.Uri) {
			return manager.show(resolveUri, {
				viewColumn: vibecoda.window.activeTextEditor ? vibecoda.ViewColumn.Beside : vibecoda.ViewColumn.Active
			});
		}
	}, {
		schemes: ['http', 'https'],
		label: vibecoda.l10n.t("Open in simple browser"),
	}));
}

function isWeb(): boolean {
	return !(typeof process === 'object' && !!process.versions.node) && vibecoda.env.uiKind === vibecoda.UIKind.Web;
}
