/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as vibecoda from 'vibecoda';

export class GitCommitFoldingProvider implements vibecoda.FoldingRangeProvider {

	provideFoldingRanges(
		document: vibecoda.TextDocument,
		_context: vibecoda.FoldingContext,
		_token: vibecoda.CancellationToken
	): vibecoda.ProviderResult<vibecoda.FoldingRange[]> {
		const ranges: vibecoda.FoldingRange[] = [];

		let commentBlockStart: number | undefined;
		let currentDiffStart: number | undefined;

		for (let i = 0; i < document.lineCount; i++) {
			const line = document.lineAt(i);
			const lineText = line.text;

			// Check for comment lines (lines starting with #)
			if (lineText.startsWith('#')) {
				// Close any active diff block when we encounter a comment
				if (currentDiffStart !== undefined) {
					// Only create fold if there are at least 2 lines
					if (i - currentDiffStart > 1) {
						ranges.push(new vibecoda.FoldingRange(currentDiffStart, i - 1));
					}
					currentDiffStart = undefined;
				}

				if (commentBlockStart === undefined) {
					commentBlockStart = i;
				}
			} else {
				// End of comment block
				if (commentBlockStart !== undefined) {
					// Only create fold if there are at least 2 lines
					if (i - commentBlockStart > 1) {
						ranges.push(new vibecoda.FoldingRange(
							commentBlockStart,
							i - 1,
							vibecoda.FoldingRangeKind.Comment
						));
					}
					commentBlockStart = undefined;
				}
			}

			// Check for diff sections (lines starting with "diff --git")
			if (lineText.startsWith('diff --git ')) {
				// If there's a previous diff block, close it
				if (currentDiffStart !== undefined) {
					// Only create fold if there are at least 2 lines
					if (i - currentDiffStart > 1) {
						ranges.push(new vibecoda.FoldingRange(currentDiffStart, i - 1));
					}
				}
				// Start new diff block
				currentDiffStart = i;
			}
		}

		// Handle end-of-document cases

		// If comment block extends to end of document
		if (commentBlockStart !== undefined) {
			if (document.lineCount - commentBlockStart > 1) {
				ranges.push(new vibecoda.FoldingRange(
					commentBlockStart,
					document.lineCount - 1,
					vibecoda.FoldingRangeKind.Comment
				));
			}
		}

		// If diff block extends to end of document
		if (currentDiffStart !== undefined) {
			if (document.lineCount - currentDiffStart > 1) {
				ranges.push(new vibecoda.FoldingRange(
					currentDiffStart,
					document.lineCount - 1
				));
			}
		}

		return ranges;
	}
}
