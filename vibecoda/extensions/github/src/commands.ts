/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as vibecoda from 'vibecoda';
import { API as GitAPI, RefType, Repository } from './typings/git.js';
import { publishRepository } from './publish.js';
import { DisposableStore, getRepositoryFromUrl } from './util.js';
import { LinkContext, getCommitLink, getLink, getVscodeDevHost } from './links.js';

async function copyVscodeDevLink(gitAPI: GitAPI, useSelection: boolean, context: LinkContext, includeRange = true) {
	try {
		const permalink = await getLink(gitAPI, useSelection, true, getVscodeDevHost(), 'headlink', context, includeRange);
		if (permalink) {
			return vibecoda.env.clipboard.writeText(permalink);
		}
	} catch (err) {
		if (!(err instanceof vibecoda.CancellationError)) {
			vibecoda.window.showErrorMessage(err.message);
		}
	}
}

async function openVscodeDevLink(gitAPI: GitAPI): Promise<vibecoda.Uri | undefined> {
	try {
		const headlink = await getLink(gitAPI, true, false, getVscodeDevHost(), 'headlink');
		return headlink ? vibecoda.Uri.parse(headlink) : undefined;
	} catch (err) {
		if (!(err instanceof vibecoda.CancellationError)) {
			vibecoda.window.showErrorMessage(err.message);
		}
		return undefined;
	}
}

async function openOnGitHub(repository: Repository, commit: string): Promise<void> {
	// Get the unique remotes that contain the commit
	const branches = await repository.getBranches({ contains: commit, remote: true });
	const remoteNames = new Set(branches.filter(b => b.type === RefType.RemoteHead && b.remote).map(b => b.remote!));

	// GitHub remotes that contain the commit
	const remotes = repository.state.remotes
		.filter(r => remoteNames.has(r.name) && r.fetchUrl && getRepositoryFromUrl(r.fetchUrl));

	if (remotes.length === 0) {
		vibecoda.window.showInformationMessage(vibecoda.l10n.t('No GitHub remotes found that contain this commit.'));
		return;
	}

	// upstream -> origin -> first
	const remote = remotes.find(r => r.name === 'upstream')
		?? remotes.find(r => r.name === 'origin')
		?? remotes[0];

	const link = getCommitLink(remote.fetchUrl!, commit);
	vibecoda.env.openExternal(vibecoda.Uri.parse(link));
}

export function registerCommands(gitAPI: GitAPI): vibecoda.Disposable {
	const disposables = new DisposableStore();

	disposables.add(vibecoda.commands.registerCommand('github.publish', async () => {
		try {
			publishRepository(gitAPI);
		} catch (err) {
			vibecoda.window.showErrorMessage(err.message);
		}
	}));

	disposables.add(vibecoda.commands.registerCommand('github.copyVscodeDevLink', async (context: LinkContext) => {
		return copyVscodeDevLink(gitAPI, true, context);
	}));

	disposables.add(vibecoda.commands.registerCommand('github.copyVscodeDevLinkFile', async (context: LinkContext) => {
		return copyVscodeDevLink(gitAPI, false, context);
	}));

	disposables.add(vibecoda.commands.registerCommand('github.copyVscodeDevLinkWithoutRange', async (context: LinkContext) => {
		return copyVscodeDevLink(gitAPI, true, context, false);
	}));

	disposables.add(vibecoda.commands.registerCommand('github.openOnGitHub', async (url: string, historyItemId: string) => {
		const link = getCommitLink(url, historyItemId);
		vibecoda.env.openExternal(vibecoda.Uri.parse(link));
	}));

	disposables.add(vibecoda.commands.registerCommand('github.graph.openOnGitHub', async (repository: vibecoda.SourceControl, historyItem: vibecoda.SourceControlHistoryItem) => {
		if (!repository || !historyItem) {
			return;
		}

		const apiRepository = gitAPI.repositories.find(r => r.rootUri.fsPath === repository.rootUri?.fsPath);
		if (!apiRepository) {
			return;
		}

		await openOnGitHub(apiRepository, historyItem.id);
	}));

	disposables.add(vibecoda.commands.registerCommand('github.timeline.openOnGitHub', async (item: vibecoda.TimelineItem, uri: vibecoda.Uri) => {
		if (!item.id || !uri) {
			return;
		}

		const apiRepository = gitAPI.getRepository(uri);
		if (!apiRepository) {
			return;
		}

		await openOnGitHub(apiRepository, item.id);
	}));

	disposables.add(vibecoda.commands.registerCommand('github.openOnVscodeDev', async () => {
		return openVscodeDevLink(gitAPI);
	}));

	return disposables;
}
