/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as vibecoda from 'vibecoda';
import { MarkdownItEngine } from '../markdownEngine';
import { MarkdownContributionProvider, MarkdownContributions } from '../markdownExtensions';
import { githubSlugifier } from '../slugify';
import { nulLogger } from './nulLogging';

const emptyContributions = new class implements MarkdownContributionProvider {
	readonly extensionUri = vibecoda.Uri.file('/');
	readonly contributions = MarkdownContributions.Empty;

	private readonly _onContributionsChanged = new vibecoda.EventEmitter<this>();
	readonly onContributionsChanged = this._onContributionsChanged.event;

	dispose() {
		this._onContributionsChanged.dispose();
	}
};

export function createNewMarkdownEngine(): MarkdownItEngine {
	return new MarkdownItEngine(emptyContributions, githubSlugifier, nulLogger);
}
