/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as vibecoda from 'vibecoda';
import { Command } from '../commandManager';
import { MarkdownPreviewManager } from '../preview/previewManager';
import { PreviewSecuritySelector } from '../preview/security';
import { isMarkdownFile } from '../util/file';

export class ShowPreviewSecuritySelectorCommand implements Command {
	public readonly id = 'markdown.showPreviewSecuritySelector';

	public constructor(
		private readonly _previewSecuritySelector: PreviewSecuritySelector,
		private readonly _previewManager: MarkdownPreviewManager
	) { }

	public execute(resource: string | undefined) {
		if (this._previewManager.activePreviewResource) {
			this._previewSecuritySelector.showSecuritySelectorForResource(this._previewManager.activePreviewResource);
		} else if (resource) {
			const source = vibecoda.Uri.parse(resource);
			this._previewSecuritySelector.showSecuritySelectorForResource(source.query ? vibecoda.Uri.parse(source.query) : source);
		} else if (vibecoda.window.activeTextEditor && isMarkdownFile(vibecoda.window.activeTextEditor.document)) {
			this._previewSecuritySelector.showSecuritySelectorForResource(vibecoda.window.activeTextEditor.document.uri);
		}
	}
}
