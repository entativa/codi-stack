/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as picomatch from 'picomatch';
import * as vibecoda from 'vibecoda';
import { TextDocumentEdit } from 'vibecoda-languageclient';
import { Utils } from 'vibecoda-uri';
import { MdLanguageClient } from '../client/client';
import { Delayer } from '../util/async';
import { noopToken } from '../util/cancellation';
import { Disposable } from '../util/dispose';
import { convertRange } from './fileReferences';


const settingNames = Object.freeze({
	enabled: 'updateLinksOnFileMove.enabled',
	include: 'updateLinksOnFileMove.include',
	enableForDirectories: 'updateLinksOnFileMove.enableForDirectories',
});

const enum UpdateLinksOnFileMoveSetting {
	Prompt = 'prompt',
	Always = 'always',
	Never = 'never',
}

interface RenameAction {
	readonly oldUri: vibecoda.Uri;
	readonly newUri: vibecoda.Uri;
}

class UpdateLinksOnFileRenameHandler extends Disposable {

	private readonly _delayer = new Delayer(50);
	private readonly _pendingRenames = new Set<RenameAction>();

	public constructor(
		private readonly _client: MdLanguageClient,
	) {
		super();

		this._register(vibecoda.workspace.onDidRenameFiles(async (e) => {
			await Promise.all(e.files.map(async (rename) => {
				if (await this._shouldParticipateInLinkUpdate(rename.newUri)) {
					this._pendingRenames.add(rename);
				}
			}));

			if (this._pendingRenames.size) {
				this._delayer.trigger(() => {
					vibecoda.window.withProgress({
						location: vibecoda.ProgressLocation.Window,
						title: vibecoda.l10n.t("Checking for Markdown links to update")
					}, () => this._flushRenames());
				});
			}
		}));
	}

	private async _flushRenames(): Promise<void> {
		const renames = Array.from(this._pendingRenames);
		this._pendingRenames.clear();

		const result = await this._getEditsForFileRename(renames, noopToken);

		if (result?.edit.size) {
			if (await this._confirmActionWithUser(result.resourcesBeingRenamed)) {
				await vibecoda.workspace.applyEdit(result.edit);
			}
		}
	}

	private async _confirmActionWithUser(newResources: readonly vibecoda.Uri[]): Promise<boolean> {
		if (!newResources.length) {
			return false;
		}

		const config = vibecoda.workspace.getConfiguration('markdown', newResources[0]);
		const setting = config.get<UpdateLinksOnFileMoveSetting>(settingNames.enabled);
		switch (setting) {
			case UpdateLinksOnFileMoveSetting.Prompt:
				return this._promptUser(newResources);
			case UpdateLinksOnFileMoveSetting.Always:
				return true;
			case UpdateLinksOnFileMoveSetting.Never:
			default:
				return false;
		}
	}
	private async _shouldParticipateInLinkUpdate(newUri: vibecoda.Uri): Promise<boolean> {
		const config = vibecoda.workspace.getConfiguration('markdown', newUri);
		const setting = config.get<UpdateLinksOnFileMoveSetting>(settingNames.enabled);
		if (setting === UpdateLinksOnFileMoveSetting.Never) {
			return false;
		}

		const externalGlob = config.get<string[]>(settingNames.include);
		if (externalGlob) {
			for (const glob of externalGlob) {
				if (picomatch.isMatch(newUri.fsPath, glob)) {
					return true;
				}
			}
		}

		const stat = await vibecoda.workspace.fs.stat(newUri);
		if (stat.type === vibecoda.FileType.Directory) {
			return config.get<boolean>(settingNames.enableForDirectories, true);
		}

		return false;
	}

	private async _promptUser(newResources: readonly vibecoda.Uri[]): Promise<boolean> {
		if (!newResources.length) {
			return false;
		}

		const rejectItem: vibecoda.MessageItem = {
			title: vibecoda.l10n.t("No"),
			isCloseAffordance: true,
		};

		const acceptItem: vibecoda.MessageItem = {
			title: vibecoda.l10n.t("Yes"),
		};

		const alwaysItem: vibecoda.MessageItem = {
			title: vibecoda.l10n.t("Always"),
		};

		const neverItem: vibecoda.MessageItem = {
			title: vibecoda.l10n.t("Never"),
		};

		const choice = await vibecoda.window.showInformationMessage(
			newResources.length === 1
				? vibecoda.l10n.t("Update Markdown links for '{0}'?", Utils.basename(newResources[0]))
				: this._getConfirmMessage(vibecoda.l10n.t("Update Markdown links for the following {0} files?", newResources.length), newResources), {
			modal: true,
		}, rejectItem, acceptItem, alwaysItem, neverItem);

		switch (choice) {
			case acceptItem: {
				return true;
			}
			case rejectItem: {
				return false;
			}
			case alwaysItem: {
				const config = vibecoda.workspace.getConfiguration('markdown', newResources[0]);
				config.update(
					settingNames.enabled,
					UpdateLinksOnFileMoveSetting.Always,
					this._getConfigTargetScope(config, settingNames.enabled));
				return true;
			}
			case neverItem: {
				const config = vibecoda.workspace.getConfiguration('markdown', newResources[0]);
				config.update(
					settingNames.enabled,
					UpdateLinksOnFileMoveSetting.Never,
					this._getConfigTargetScope(config, settingNames.enabled));
				return false;
			}
			default: {
				return false;
			}
		}
	}

	private async _getEditsForFileRename(renames: readonly RenameAction[], token: vibecoda.CancellationToken): Promise<{ edit: vibecoda.WorkspaceEdit; resourcesBeingRenamed: vibecoda.Uri[] } | undefined> {
		const result = await this._client.getEditForFileRenames(renames.map(rename => ({ oldUri: rename.oldUri.toString(), newUri: rename.newUri.toString() })), token);
		if (!result?.edit.documentChanges?.length) {
			return undefined;
		}

		const workspaceEdit = new vibecoda.WorkspaceEdit();

		for (const change of result.edit.documentChanges as TextDocumentEdit[]) {
			const uri = vibecoda.Uri.parse(change.textDocument.uri);
			for (const edit of change.edits) {
				workspaceEdit.replace(uri, convertRange(edit.range), edit.newText);
			}
		}

		return {
			edit: workspaceEdit,
			resourcesBeingRenamed: result.participatingRenames.map(x => vibecoda.Uri.parse(x.newUri)),
		};
	}

	private _getConfirmMessage(start: string, resourcesToConfirm: readonly vibecoda.Uri[]): string {
		const MAX_CONFIRM_FILES = 10;

		const paths = [start];
		paths.push('');
		paths.push(...resourcesToConfirm.slice(0, MAX_CONFIRM_FILES).map(r => Utils.basename(r)));

		if (resourcesToConfirm.length > MAX_CONFIRM_FILES) {
			if (resourcesToConfirm.length - MAX_CONFIRM_FILES === 1) {
				paths.push(vibecoda.l10n.t("...1 additional file not shown"));
			} else {
				paths.push(vibecoda.l10n.t("...{0} additional files not shown", resourcesToConfirm.length - MAX_CONFIRM_FILES));
			}
		}

		paths.push('');
		return paths.join('\n');
	}

	private _getConfigTargetScope(config: vibecoda.WorkspaceConfiguration, settingsName: string): vibecoda.ConfigurationTarget {
		const inspected = config.inspect(settingsName);
		if (inspected?.workspaceFolderValue) {
			return vibecoda.ConfigurationTarget.WorkspaceFolder;
		}

		if (inspected?.workspaceValue) {
			return vibecoda.ConfigurationTarget.Workspace;
		}

		return vibecoda.ConfigurationTarget.Global;
	}
}

export function registerUpdateLinksOnRename(client: MdLanguageClient): vibecoda.Disposable {
	return new UpdateLinksOnFileRenameHandler(client);
}
