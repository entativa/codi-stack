/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as vibecoda from 'vibecoda';
import * as interfaces from './interfaces';

export default class MergeConflictCodeLensProvider implements vibecoda.CodeLensProvider, vibecoda.Disposable {
	private codeLensRegistrationHandle?: vibecoda.Disposable | null;
	private config?: interfaces.IExtensionConfiguration;
	private tracker: interfaces.IDocumentMergeConflictTracker;

	constructor(trackerService: interfaces.IDocumentMergeConflictTrackerService) {
		this.tracker = trackerService.createTracker('codelens');
	}

	begin(config: interfaces.IExtensionConfiguration) {
		this.config = config;

		if (this.config.enableCodeLens) {
			this.registerCodeLensProvider();
		}
	}

	configurationUpdated(updatedConfig: interfaces.IExtensionConfiguration) {

		if (updatedConfig.enableCodeLens === false && this.codeLensRegistrationHandle) {
			this.codeLensRegistrationHandle.dispose();
			this.codeLensRegistrationHandle = null;
		}
		else if (updatedConfig.enableCodeLens === true && !this.codeLensRegistrationHandle) {
			this.registerCodeLensProvider();
		}

		this.config = updatedConfig;
	}


	dispose() {
		if (this.codeLensRegistrationHandle) {
			this.codeLensRegistrationHandle.dispose();
			this.codeLensRegistrationHandle = null;
		}
	}

	async provideCodeLenses(document: vibecoda.TextDocument, _token: vibecoda.CancellationToken): Promise<vibecoda.CodeLens[] | null> {

		if (!this.config || !this.config.enableCodeLens) {
			return null;
		}

		const conflicts = await this.tracker.getConflicts(document);
		const conflictsCount = conflicts?.length ?? 0;
		vibecoda.commands.executeCommand('setContext', 'mergeConflictsCount', conflictsCount);

		if (!conflictsCount) {
			return null;
		}

		const items: vibecoda.CodeLens[] = [];

		conflicts.forEach(conflict => {
			const acceptCurrentCommand: vibecoda.Command = {
				command: 'merge-conflict.accept.current',
				title: vibecoda.l10n.t("Accept Current Change"),
				arguments: ['known-conflict', conflict]
			};

			const acceptIncomingCommand: vibecoda.Command = {
				command: 'merge-conflict.accept.incoming',
				title: vibecoda.l10n.t("Accept Incoming Change"),
				arguments: ['known-conflict', conflict]
			};

			const acceptBothCommand: vibecoda.Command = {
				command: 'merge-conflict.accept.both',
				title: vibecoda.l10n.t("Accept Both Changes"),
				arguments: ['known-conflict', conflict]
			};

			const diffCommand: vibecoda.Command = {
				command: 'merge-conflict.compare',
				title: vibecoda.l10n.t("Compare Changes"),
				arguments: [conflict]
			};

			const range = document.lineAt(conflict.range.start.line).range;
			items.push(
				new vibecoda.CodeLens(range, acceptCurrentCommand),
				new vibecoda.CodeLens(range, acceptIncomingCommand),
				new vibecoda.CodeLens(range, acceptBothCommand),
				new vibecoda.CodeLens(range, diffCommand)
			);
		});

		return items;
	}

	private registerCodeLensProvider() {
		this.codeLensRegistrationHandle = vibecoda.languages.registerCodeLensProvider([
			{ scheme: 'file' },
			{ scheme: 'vibecoda-vfs' },
			{ scheme: 'untitled' },
			{ scheme: 'vibecoda-userdata' },
		], this);
	}
}
