/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as vibecoda from 'vibecoda';
import { SelectTypeScriptVersionCommand } from '../commands/selectTypeScriptVersion';
import { jsTsLanguageModes } from '../configuration/languageIds';
import { TypeScriptVersion } from '../tsServer/versionProvider';
import { ITypeScriptServiceClient } from '../typescriptService';
import { Disposable } from '../utils/dispose';


export class VersionStatus extends Disposable {

	private readonly _statusItem: vibecoda.LanguageStatusItem;

	constructor(
		private readonly _client: ITypeScriptServiceClient,
	) {
		super();

		this._statusItem = this._register(vibecoda.languages.createLanguageStatusItem('typescript.version', jsTsLanguageModes));

		this._statusItem.name = vibecoda.l10n.t("TypeScript Version");
		this._statusItem.detail = vibecoda.l10n.t("TypeScript version");

		this._register(this._client.onTsServerStarted(({ version }) => this.onDidChangeTypeScriptVersion(version)));
	}

	private onDidChangeTypeScriptVersion(version: TypeScriptVersion) {
		this._statusItem.text = version.displayName;
		this._statusItem.command = {
			command: SelectTypeScriptVersionCommand.id,
			title: vibecoda.l10n.t("Select Version"),
			tooltip: version.path
		};
	}
}
