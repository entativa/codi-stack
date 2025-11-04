/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as vibecoda from 'vibecoda';
import { GitHubAuthenticationProvider, UriEventHandler } from './github';

const settingNotSent = '"github-enterprise.uri" not set';
const settingInvalid = '"github-enterprise.uri" invalid';

class NullAuthProvider implements vibecoda.AuthenticationProvider {
	private _onDidChangeSessions = new vibecoda.EventEmitter<vibecoda.AuthenticationProviderAuthenticationSessionsChangeEvent>();
	onDidChangeSessions = this._onDidChangeSessions.event;

	private readonly _disposable: vibecoda.Disposable;

	constructor(private readonly _errorMessage: string) {
		this._disposable = vibecoda.authentication.registerAuthenticationProvider('github-enterprise', 'GitHub Enterprise', this);
	}

	createSession(): Thenable<vibecoda.AuthenticationSession> {
		throw new Error(this._errorMessage);
	}

	getSessions(): Thenable<vibecoda.AuthenticationSession[]> {
		return Promise.resolve([]);
	}
	removeSession(): Thenable<void> {
		throw new Error(this._errorMessage);
	}

	dispose() {
		this._onDidChangeSessions.dispose();
		this._disposable.dispose();
	}
}

function initGHES(context: vibecoda.ExtensionContext, uriHandler: UriEventHandler): vibecoda.Disposable {
	const settingValue = vibecoda.workspace.getConfiguration().get<string>('github-enterprise.uri');
	if (!settingValue) {
		const provider = new NullAuthProvider(settingNotSent);
		context.subscriptions.push(provider);
		return provider;
	}

	// validate user value
	let uri: vibecoda.Uri;
	try {
		uri = vibecoda.Uri.parse(settingValue, true);
	} catch (e) {
		vibecoda.window.showErrorMessage(vibecoda.l10n.t('GitHub Enterprise Server URI is not a valid URI: {0}', e.message ?? e));
		const provider = new NullAuthProvider(settingInvalid);
		context.subscriptions.push(provider);
		return provider;
	}

	const githubEnterpriseAuthProvider = new GitHubAuthenticationProvider(context, uriHandler, uri);
	context.subscriptions.push(githubEnterpriseAuthProvider);
	return githubEnterpriseAuthProvider;
}

export function activate(context: vibecoda.ExtensionContext) {
	const uriHandler = new UriEventHandler();
	context.subscriptions.push(uriHandler);
	context.subscriptions.push(vibecoda.window.registerUriHandler(uriHandler));

	context.subscriptions.push(new GitHubAuthenticationProvider(context, uriHandler));

	let before = vibecoda.workspace.getConfiguration().get<string>('github-enterprise.uri');
	let githubEnterpriseAuthProvider = initGHES(context, uriHandler);
	context.subscriptions.push(vibecoda.workspace.onDidChangeConfiguration(e => {
		if (e.affectsConfiguration('github-enterprise.uri')) {
			const after = vibecoda.workspace.getConfiguration().get<string>('github-enterprise.uri');
			if (before !== after) {
				githubEnterpriseAuthProvider?.dispose();
				before = after;
				githubEnterpriseAuthProvider = initGHES(context, uriHandler);
			}
		}
	}));

	// Listener to prompt for reload when the fetch implementation setting changes
	const beforeFetchSetting = vibecoda.workspace.getConfiguration().get<boolean>('github-authentication.useElectronFetch', true);
	context.subscriptions.push(vibecoda.workspace.onDidChangeConfiguration(async e => {
		if (e.affectsConfiguration('github-authentication.useElectronFetch')) {
			const afterFetchSetting = vibecoda.workspace.getConfiguration().get<boolean>('github-authentication.useElectronFetch', true);
			if (beforeFetchSetting !== afterFetchSetting) {
				const selection = await vibecoda.window.showInformationMessage(
					vibecoda.l10n.t('GitHub Authentication - Reload required'),
					{
						modal: true,
						detail: vibecoda.l10n.t('A reload is required for the fetch setting change to take effect.')
					},
					vibecoda.l10n.t('Reload Window')
				);
				if (selection) {
					await vibecoda.commands.executeCommand('workbench.action.reloadWindow');
				}
			}
		}
	}));
}
