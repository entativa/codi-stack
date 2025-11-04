/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as vibecoda from 'vibecoda';

export interface TSConfig {
	readonly uri: vibecoda.Uri;
	readonly fsPath: string;
	readonly posixPath: string;
	readonly workspaceFolder?: vibecoda.WorkspaceFolder;
}

export class TsConfigProvider {
	public async getConfigsForWorkspace(token: vibecoda.CancellationToken): Promise<Iterable<TSConfig>> {
		if (!vibecoda.workspace.workspaceFolders) {
			return [];
		}

		const configs = new Map<string, TSConfig>();
		for (const config of await this.findConfigFiles(token)) {
			const root = vibecoda.workspace.getWorkspaceFolder(config);
			if (root) {
				configs.set(config.fsPath, {
					uri: config,
					fsPath: config.fsPath,
					posixPath: config.path,
					workspaceFolder: root
				});
			}
		}
		return configs.values();
	}

	private async findConfigFiles(token: vibecoda.CancellationToken): Promise<vibecoda.Uri[]> {
		return await vibecoda.workspace.findFiles('**/tsconfig*.json', '**/{node_modules,.*}/**', undefined, token);
	}
}
