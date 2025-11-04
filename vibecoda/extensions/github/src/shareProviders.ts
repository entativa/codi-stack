/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as vibecoda from 'vibecoda';
import { API } from './typings/git.js';
import { getRepositoryFromUrl, repositoryHasGitHubRemote } from './util.js';
import { encodeURIComponentExceptSlashes, ensurePublished, getRepositoryForFile, notebookCellRangeString, rangeString } from './links.js';

export class VscodeDevShareProvider implements vibecoda.ShareProvider, vibecoda.Disposable {
	readonly id: string = 'copyVscodeDevLink';
	readonly label: string = vibecoda.l10n.t('Copy vibecoda.dev Link');
	readonly priority: number = 10;


	private _hasGitHubRepositories: boolean = false;
	private set hasGitHubRepositories(value: boolean) {
		vibecoda.commands.executeCommand('setContext', 'github.hasGitHubRepo', value);
		this._hasGitHubRepositories = value;
		this.ensureShareProviderRegistration();
	}

	private shareProviderRegistration: vibecoda.Disposable | undefined;
	private disposables: vibecoda.Disposable[] = [];

	constructor(private readonly gitAPI: API) {
		this.initializeGitHubRepoContext();
	}

	dispose() {
		this.disposables.forEach(d => d.dispose());
	}

	private initializeGitHubRepoContext() {
		if (this.gitAPI.repositories.find(repo => repositoryHasGitHubRemote(repo))) {
			this.hasGitHubRepositories = true;
			vibecoda.commands.executeCommand('setContext', 'github.hasGitHubRepo', true);
		} else {
			this.disposables.push(this.gitAPI.onDidOpenRepository(async e => {
				await e.status();
				if (repositoryHasGitHubRemote(e)) {
					vibecoda.commands.executeCommand('setContext', 'github.hasGitHubRepo', true);
					this.hasGitHubRepositories = true;
				}
			}));
		}
		this.disposables.push(this.gitAPI.onDidCloseRepository(() => {
			if (!this.gitAPI.repositories.find(repo => repositoryHasGitHubRemote(repo))) {
				this.hasGitHubRepositories = false;
			}
		}));
	}

	private ensureShareProviderRegistration() {
		if (vibecoda.env.appHost !== 'codespaces' && !this.shareProviderRegistration && this._hasGitHubRepositories) {
			const shareProviderRegistration = vibecoda.window.registerShareProvider({ scheme: 'file' }, this);
			this.shareProviderRegistration = shareProviderRegistration;
			this.disposables.push(shareProviderRegistration);
		} else if (this.shareProviderRegistration && !this._hasGitHubRepositories) {
			this.shareProviderRegistration.dispose();
			this.shareProviderRegistration = undefined;
		}
	}

	async provideShare(item: vibecoda.ShareableItem, _token: vibecoda.CancellationToken): Promise<vibecoda.Uri | undefined> {
		const repository = getRepositoryForFile(this.gitAPI, item.resourceUri);
		if (!repository) {
			return;
		}

		await ensurePublished(repository, item.resourceUri);

		let repo: { owner: string; repo: string } | undefined;
		repository.state.remotes.find(remote => {
			if (remote.fetchUrl) {
				const foundRepo = getRepositoryFromUrl(remote.fetchUrl);
				if (foundRepo && (remote.name === repository.state.HEAD?.upstream?.remote)) {
					repo = foundRepo;
					return;
				} else if (foundRepo && !repo) {
					repo = foundRepo;
				}
			}
			return;
		});

		if (!repo) {
			return;
		}

		const blobSegment = repository?.state.HEAD?.name ? encodeURIComponentExceptSlashes(repository.state.HEAD?.name) : repository?.state.HEAD?.commit;
		const filepathSegment = encodeURIComponentExceptSlashes(item.resourceUri.path.substring(repository?.rootUri.path.length));
		const rangeSegment = getRangeSegment(item);
		return vibecoda.Uri.parse(`${this.getVscodeDevHost()}/${repo.owner}/${repo.repo}/blob/${blobSegment}${filepathSegment}${rangeSegment}`);

	}

	private getVscodeDevHost(): string {
		return `https://${vibecoda.env.appName.toLowerCase().includes('insiders') ? 'insiders.' : ''}vibecoda.dev/github`;
	}
}

function getRangeSegment(item: vibecoda.ShareableItem) {
	if (item.resourceUri.scheme === 'vibecoda-notebook-cell') {
		const notebookEditor = vibecoda.window.visibleNotebookEditors.find(editor => editor.notebook.uri.fsPath === item.resourceUri.fsPath);
		const cell = notebookEditor?.notebook.getCells().find(cell => cell.document.uri.fragment === item.resourceUri?.fragment);
		const cellIndex = cell?.index ?? notebookEditor?.selection.start;
		return notebookCellRangeString(cellIndex, item.selection);
	}

	return rangeString(item.selection);
}
