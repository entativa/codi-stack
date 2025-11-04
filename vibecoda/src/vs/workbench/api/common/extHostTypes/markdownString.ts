/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

/* eslint-disable local/code-no-native-private */

import type * as vibecoda from 'vibecoda';
import { MarkdownString as BaseMarkdownString, MarkdownStringTrustedOptions } from '../../../../base/common/htmlContent.js';
import { es5ClassCompat } from './es5ClassCompat.js';

@es5ClassCompat
export class MarkdownString implements vibecoda.MarkdownString {

	readonly #delegate: BaseMarkdownString;

	static isMarkdownString(thing: unknown): thing is vibecoda.MarkdownString {
		if (thing instanceof MarkdownString) {
			return true;
		}
		if (!thing || typeof thing !== 'object') {
			return false;
		}
		return (thing as vibecoda.MarkdownString).appendCodeblock && (thing as vibecoda.MarkdownString).appendMarkdown && (thing as vibecoda.MarkdownString).appendText && ((thing as vibecoda.MarkdownString).value !== undefined);
	}

	constructor(value?: string, supportThemeIcons: boolean = false) {
		this.#delegate = new BaseMarkdownString(value, { supportThemeIcons });
	}

	get value(): string {
		return this.#delegate.value;
	}
	set value(value: string) {
		this.#delegate.value = value;
	}

	get isTrusted(): boolean | MarkdownStringTrustedOptions | undefined {
		return this.#delegate.isTrusted;
	}

	set isTrusted(value: boolean | MarkdownStringTrustedOptions | undefined) {
		this.#delegate.isTrusted = value;
	}

	get supportThemeIcons(): boolean | undefined {
		return this.#delegate.supportThemeIcons;
	}

	set supportThemeIcons(value: boolean | undefined) {
		this.#delegate.supportThemeIcons = value;
	}

	get supportHtml(): boolean | undefined {
		return this.#delegate.supportHtml;
	}

	set supportHtml(value: boolean | undefined) {
		this.#delegate.supportHtml = value;
	}

	get supportAlertSyntax(): boolean | undefined {
		return this.#delegate.supportAlertSyntax;
	}

	set supportAlertSyntax(value: boolean | undefined) {
		this.#delegate.supportAlertSyntax = value;
	}

	get baseUri(): vibecoda.Uri | undefined {
		return this.#delegate.baseUri;
	}

	set baseUri(value: vibecoda.Uri | undefined) {
		this.#delegate.baseUri = value;
	}

	appendText(value: string): vibecoda.MarkdownString {
		this.#delegate.appendText(value);
		return this;
	}

	appendMarkdown(value: string): vibecoda.MarkdownString {
		this.#delegate.appendMarkdown(value);
		return this;
	}

	appendCodeblock(value: string, language?: string): vibecoda.MarkdownString {
		this.#delegate.appendCodeblock(language ?? '', value);
		return this;
	}
}
