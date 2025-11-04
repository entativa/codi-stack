/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as path from 'path';
import * as vibecoda from 'vibecoda';
import { RefType } from './api/git';
import { Model } from './model';

export class GitEditSessionIdentityProvider implements vibecoda.EditSessionIdentityProvider, vibecoda.Disposable {

	private providerRegistration: vibecoda.Disposable;

	constructor(private model: Model) {
		this.providerRegistration = vibecoda.Disposable.from(
			vibecoda.workspace.registerEditSessionIdentityProvider('file', this),
			vibecoda.workspace.onWillCreateEditSessionIdentity((e) => {
				e.waitUntil(
					this._onWillCreateEditSessionIdentity(e.workspaceFolder).catch(err => {
						if (err instanceof vibecoda.CancellationError) {
							throw err;
						}
					})
				);
			})
		);
	}

	dispose() {
		this.providerRegistration.dispose();
	}

	async provideEditSessionIdentity(workspaceFolder: vibecoda.WorkspaceFolder, token: vibecoda.CancellationToken): Promise<string | undefined> {
		await this.model.openRepository(path.dirname(workspaceFolder.uri.fsPath));

		const repository = this.model.getRepository(workspaceFolder.uri);
		await repository?.status();

		if (!repository || !repository?.HEAD?.upstream) {
			return undefined;
		}

		const remoteUrl = repository.remotes.find((remote) => remote.name === repository.HEAD?.upstream?.remote)?.pushUrl?.replace(/^(git@[^\/:]+)(:)/i, 'ssh://$1/');
		const remote = remoteUrl ? await vibecoda.workspace.getCanonicalUri(vibecoda.Uri.parse(remoteUrl), { targetScheme: 'https' }, token) : null;

		return JSON.stringify({
			remote: remote?.toString() ?? remoteUrl,
			ref: repository.HEAD?.upstream?.name ?? null,
			sha: repository.HEAD?.commit ?? null,
		});
	}

	provideEditSessionIdentityMatch(identity1: string, identity2: string): vibecoda.EditSessionIdentityMatch {
		try {
			const normalizedIdentity1 = normalizeEditSessionIdentity(identity1);
			const normalizedIdentity2 = normalizeEditSessionIdentity(identity2);

			if (normalizedIdentity1.remote === normalizedIdentity2.remote &&
				normalizedIdentity1.ref === normalizedIdentity2.ref &&
				normalizedIdentity1.sha === normalizedIdentity2.sha) {
				// This is a perfect match
				return vibecoda.EditSessionIdentityMatch.Complete;
			} else if (normalizedIdentity1.remote === normalizedIdentity2.remote &&
				normalizedIdentity1.ref === normalizedIdentity2.ref &&
				normalizedIdentity1.sha !== normalizedIdentity2.sha) {
				// Same branch and remote but different SHA
				return vibecoda.EditSessionIdentityMatch.Partial;
			} else {
				return vibecoda.EditSessionIdentityMatch.None;
			}
		} catch (ex) {
			return vibecoda.EditSessionIdentityMatch.Partial;
		}
	}

	private async _onWillCreateEditSessionIdentity(workspaceFolder: vibecoda.WorkspaceFolder): Promise<void> {
		await this._doPublish(workspaceFolder);
	}

	private async _doPublish(workspaceFolder: vibecoda.WorkspaceFolder) {
		await this.model.openRepository(path.dirname(workspaceFolder.uri.fsPath));

		const repository = this.model.getRepository(workspaceFolder.uri);
		if (!repository) {
			return;
		}

		await repository.status();

		if (!repository.HEAD?.commit) {
			// Handle publishing empty repository with no commits

			const yes = vibecoda.l10n.t('Yes');
			const selection = await vibecoda.window.showInformationMessage(
				vibecoda.l10n.t('Would you like to publish this repository to continue working on it elsewhere?'),
				{ modal: true },
				yes
			);
			if (selection !== yes) {
				throw new vibecoda.CancellationError();
			}
			await repository.commit('Initial commit', { all: true });
			await vibecoda.commands.executeCommand('git.publish');
		} else if (!repository.HEAD?.upstream && repository.HEAD?.type === RefType.Head) {
			// If this branch hasn't been published to the remote yet,
			// ensure that it is published before Continue On is invoked

			const publishBranch = vibecoda.l10n.t('Publish Branch');
			const selection = await vibecoda.window.showInformationMessage(
				vibecoda.l10n.t('The current branch is not published to the remote. Would you like to publish it to access your changes elsewhere?'),
				{ modal: true },
				publishBranch
			);
			if (selection !== publishBranch) {
				throw new vibecoda.CancellationError();
			}

			await vibecoda.commands.executeCommand('git.publish');
		}
	}
}

function normalizeEditSessionIdentity(identity: string) {
	let { remote, ref, sha } = JSON.parse(identity);

	if (typeof remote === 'string' && remote.endsWith('.git')) {
		remote = remote.slice(0, remote.length - 4);
	}

	return {
		remote,
		ref,
		sha
	};
}
