/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as vibecoda from 'vibecoda';
import { Command } from './commandManager';

export const tsNativeExtensionId = 'typescriptteam.native-preview';

export class EnableTsgoCommand implements Command {
	public readonly id = 'typescript.experimental.enableTsgo';

	public async execute(): Promise<void> {
		await updateTsgoSetting(true);
	}
}

export class DisableTsgoCommand implements Command {
	public readonly id = 'typescript.experimental.disableTsgo';

	public async execute(): Promise<void> {
		await updateTsgoSetting(false);
	}
}

/**
 * Updates the TypeScript Go setting and reloads extension host.
 * @param enable Whether to enable or disable TypeScript Go
 */
async function updateTsgoSetting(enable: boolean): Promise<void> {
	const tsgoExtension = vibecoda.extensions.getExtension(tsNativeExtensionId);
	// Error if the TypeScript Go extension is not installed with a button to open the GitHub repo
	if (!tsgoExtension) {
		const selection = await vibecoda.window.showErrorMessage(
			vibecoda.l10n.t('The TypeScript Go extension is not installed.'),
			{
				title: vibecoda.l10n.t('Open on GitHub'),
				isCloseAffordance: true,
			}
		);

		if (selection) {
			await vibecoda.env.openExternal(vibecoda.Uri.parse('https://github.com/microsoft/typescript-go'));
		}
	}

	const tsConfig = vibecoda.workspace.getConfiguration('typescript');
	const currentValue = tsConfig.get<boolean>('experimental.useTsgo', false);
	if (currentValue === enable) {
		return;
	}

	// Determine the target scope for the configuration update
	let target = vibecoda.ConfigurationTarget.Global;
	const inspect = tsConfig.inspect<boolean>('experimental.useTsgo');
	if (inspect?.workspaceValue !== undefined) {
		target = vibecoda.ConfigurationTarget.Workspace;
	} else if (inspect?.workspaceFolderValue !== undefined) {
		target = vibecoda.ConfigurationTarget.WorkspaceFolder;
	} else {
		// If setting is not defined yet, use the same scope as typescript-go.executablePath
		const tsgoConfig = vibecoda.workspace.getConfiguration('typescript-go');
		const tsgoInspect = tsgoConfig.inspect<string>('executablePath');

		if (tsgoInspect?.workspaceValue !== undefined) {
			target = vibecoda.ConfigurationTarget.Workspace;
		} else if (tsgoInspect?.workspaceFolderValue !== undefined) {
			target = vibecoda.ConfigurationTarget.WorkspaceFolder;
		}
	}

	await tsConfig.update('experimental.useTsgo', enable, target);
}
