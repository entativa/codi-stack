/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as vibecoda from 'vibecoda';
import { CommandManager } from '../commandManager';
import { isMarkdownFile } from '../util/file';


// Copied from markdown language service
export enum DiagnosticCode {
	link_noSuchReferences = 'link.no-such-reference',
	link_noSuchHeaderInOwnFile = 'link.no-such-header-in-own-file',
	link_noSuchFile = 'link.no-such-file',
	link_noSuchHeaderInFile = 'link.no-such-header-in-file',
}


class AddToIgnoreLinksQuickFixProvider implements vibecoda.CodeActionProvider {

	private static readonly _addToIgnoreLinksCommandId = '_markdown.addToIgnoreLinks';

	private static readonly _metadata: vibecoda.CodeActionProviderMetadata = {
		providedCodeActionKinds: [
			vibecoda.CodeActionKind.QuickFix
		],
	};

	public static register(selector: vibecoda.DocumentSelector, commandManager: CommandManager): vibecoda.Disposable {
		const reg = vibecoda.languages.registerCodeActionsProvider(selector, new AddToIgnoreLinksQuickFixProvider(), AddToIgnoreLinksQuickFixProvider._metadata);
		const commandReg = commandManager.register({
			id: AddToIgnoreLinksQuickFixProvider._addToIgnoreLinksCommandId,
			execute(resource: vibecoda.Uri, path: string) {
				const settingId = 'validate.ignoredLinks';
				const config = vibecoda.workspace.getConfiguration('markdown', resource);
				const paths = new Set(config.get<string[]>(settingId, []));
				paths.add(path);
				config.update(settingId, [...paths], vibecoda.ConfigurationTarget.WorkspaceFolder);
			}
		});
		return vibecoda.Disposable.from(reg, commandReg);
	}

	provideCodeActions(document: vibecoda.TextDocument, _range: vibecoda.Range | vibecoda.Selection, context: vibecoda.CodeActionContext, _token: vibecoda.CancellationToken): vibecoda.ProviderResult<(vibecoda.CodeAction | vibecoda.Command)[]> {
		const fixes: vibecoda.CodeAction[] = [];

		for (const diagnostic of context.diagnostics) {
			switch (diagnostic.code) {
				case DiagnosticCode.link_noSuchReferences:
				case DiagnosticCode.link_noSuchHeaderInOwnFile:
				case DiagnosticCode.link_noSuchFile:
				case DiagnosticCode.link_noSuchHeaderInFile: {
					// eslint-disable-next-line local/code-no-any-casts
					const hrefText = (diagnostic as any).data?.hrefText;
					if (hrefText) {
						const fix = new vibecoda.CodeAction(
							vibecoda.l10n.t("Exclude '{0}' from link validation.", hrefText),
							vibecoda.CodeActionKind.QuickFix);

						fix.command = {
							command: AddToIgnoreLinksQuickFixProvider._addToIgnoreLinksCommandId,
							title: '',
							arguments: [document.uri, hrefText],
						};
						fixes.push(fix);
					}
					break;
				}
			}
		}

		return fixes;
	}
}

function registerMarkdownStatusItem(selector: vibecoda.DocumentSelector, commandManager: CommandManager): vibecoda.Disposable {
	const statusItem = vibecoda.languages.createLanguageStatusItem('markdownStatus', selector);

	const enabledSettingId = 'validate.enabled';
	const commandId = '_markdown.toggleValidation';

	const commandSub = commandManager.register({
		id: commandId,
		execute: (enabled: boolean) => {
			vibecoda.workspace.getConfiguration('markdown').update(enabledSettingId, enabled);
		}
	});

	const update = () => {
		const activeDoc = vibecoda.window.activeTextEditor?.document;
		const markdownDoc = activeDoc && isMarkdownFile(activeDoc) ? activeDoc : undefined;

		const enabled = vibecoda.workspace.getConfiguration('markdown', markdownDoc).get(enabledSettingId);
		if (enabled) {
			statusItem.text = vibecoda.l10n.t('Markdown link validation enabled');
			statusItem.command = {
				command: commandId,
				arguments: [false],
				title: vibecoda.l10n.t('Disable'),
				tooltip: vibecoda.l10n.t('Disable validation of Markdown links'),
			};
		} else {
			statusItem.text = vibecoda.l10n.t('Markdown link validation disabled');
			statusItem.command = {
				command: commandId,
				arguments: [true],
				title: vibecoda.l10n.t('Enable'),
				tooltip: vibecoda.l10n.t('Enable validation of Markdown links'),
			};
		}
	};
	update();

	return vibecoda.Disposable.from(
		statusItem,
		commandSub,
		vibecoda.workspace.onDidChangeConfiguration(e => {
			if (e.affectsConfiguration('markdown.' + enabledSettingId)) {
				update();
			}
		}),
	);
}

export function registerDiagnosticSupport(
	selector: vibecoda.DocumentSelector,
	commandManager: CommandManager,
): vibecoda.Disposable {
	return vibecoda.Disposable.from(
		AddToIgnoreLinksQuickFixProvider.register(selector, commandManager),
		registerMarkdownStatusItem(selector, commandManager),
	);
}
