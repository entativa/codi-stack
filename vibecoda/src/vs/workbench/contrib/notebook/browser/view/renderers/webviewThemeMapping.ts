/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import { WebviewStyles } from '../../../../webview/browser/webview.js';

const mapping: ReadonlyMap<string, string> = new Map([
	['theme-font-family', 'vibecoda-font-family'],
	['theme-font-weight', 'vibecoda-font-weight'],
	['theme-font-size', 'vibecoda-font-size'],
	['theme-code-font-family', 'vibecoda-editor-font-family'],
	['theme-code-font-weight', 'vibecoda-editor-font-weight'],
	['theme-code-font-size', 'vibecoda-editor-font-size'],
	['theme-scrollbar-background', 'vibecoda-scrollbarSlider-background'],
	['theme-scrollbar-hover-background', 'vibecoda-scrollbarSlider-hoverBackground'],
	['theme-scrollbar-active-background', 'vibecoda-scrollbarSlider-activeBackground'],
	['theme-quote-background', 'vibecoda-textBlockQuote-background'],
	['theme-quote-border', 'vibecoda-textBlockQuote-border'],
	['theme-code-foreground', 'vibecoda-textPreformat-foreground'],
	['theme-code-background', 'vibecoda-textPreformat-background'],
	// Editor
	['theme-background', 'vibecoda-editor-background'],
	['theme-foreground', 'vibecoda-editor-foreground'],
	['theme-ui-foreground', 'vibecoda-foreground'],
	['theme-link', 'vibecoda-textLink-foreground'],
	['theme-link-active', 'vibecoda-textLink-activeForeground'],
	// Buttons
	['theme-button-background', 'vibecoda-button-background'],
	['theme-button-hover-background', 'vibecoda-button-hoverBackground'],
	['theme-button-foreground', 'vibecoda-button-foreground'],
	['theme-button-secondary-background', 'vibecoda-button-secondaryBackground'],
	['theme-button-secondary-hover-background', 'vibecoda-button-secondaryHoverBackground'],
	['theme-button-secondary-foreground', 'vibecoda-button-secondaryForeground'],
	['theme-button-hover-foreground', 'vibecoda-button-foreground'],
	['theme-button-focus-foreground', 'vibecoda-button-foreground'],
	['theme-button-secondary-hover-foreground', 'vibecoda-button-secondaryForeground'],
	['theme-button-secondary-focus-foreground', 'vibecoda-button-secondaryForeground'],
	// Inputs
	['theme-input-background', 'vibecoda-input-background'],
	['theme-input-foreground', 'vibecoda-input-foreground'],
	['theme-input-placeholder-foreground', 'vibecoda-input-placeholderForeground'],
	['theme-input-focus-border-color', 'vibecoda-focusBorder'],
	// Menus
	['theme-menu-background', 'vibecoda-menu-background'],
	['theme-menu-foreground', 'vibecoda-menu-foreground'],
	['theme-menu-hover-background', 'vibecoda-menu-selectionBackground'],
	['theme-menu-focus-background', 'vibecoda-menu-selectionBackground'],
	['theme-menu-hover-foreground', 'vibecoda-menu-selectionForeground'],
	['theme-menu-focus-foreground', 'vibecoda-menu-selectionForeground'],
	// Errors
	['theme-error-background', 'vibecoda-inputValidation-errorBackground'],
	['theme-error-foreground', 'vibecoda-foreground'],
	['theme-warning-background', 'vibecoda-inputValidation-warningBackground'],
	['theme-warning-foreground', 'vibecoda-foreground'],
	['theme-info-background', 'vibecoda-inputValidation-infoBackground'],
	['theme-info-foreground', 'vibecoda-foreground'],
	// Notebook:
	['theme-notebook-output-background', 'vibecoda-notebook-outputContainerBackgroundColor'],
	['theme-notebook-output-border', 'vibecoda-notebook-outputContainerBorderColor'],
	['theme-notebook-cell-selected-background', 'vibecoda-notebook-selectedCellBackground'],
	['theme-notebook-symbol-highlight-background', 'vibecoda-notebook-symbolHighlightBackground'],
	['theme-notebook-diff-removed-background', 'vibecoda-diffEditor-removedTextBackground'],
	['theme-notebook-diff-inserted-background', 'vibecoda-diffEditor-insertedTextBackground'],
]);

const constants: Readonly<WebviewStyles> = {
	'theme-input-border-width': '1px',
	'theme-button-primary-hover-shadow': 'none',
	'theme-button-secondary-hover-shadow': 'none',
	'theme-input-border-color': 'transparent',
};

/**
 * Transforms base vibecoda theme variables into generic variables for notebook
 * renderers.
 * @see https://github.com/microsoft/vibecoda/issues/107985 for context
 * @deprecated
 */
export const transformWebviewThemeVars = (s: Readonly<WebviewStyles>): WebviewStyles => {
	const result = { ...s, ...constants };
	for (const [target, src] of mapping) {
		result[target] = s[src];
	}

	return result;
};
