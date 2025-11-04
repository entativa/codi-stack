/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as vibecoda from 'vibecoda';
import { BinarySizeStatusBarEntry } from './binarySizeStatusBarEntry';
import { MediaPreview, reopenAsText } from './mediaPreview';
import { escapeAttribute } from './util/dom';
import { generateUuid } from './util/uuid';


class VideoPreviewProvider implements vibecoda.CustomReadonlyEditorProvider {

	public static readonly viewType = 'vibecoda.videoPreview';

	constructor(
		private readonly extensionRoot: vibecoda.Uri,
		private readonly binarySizeStatusBarEntry: BinarySizeStatusBarEntry,
	) { }

	public async openCustomDocument(uri: vibecoda.Uri) {
		return { uri, dispose: () => { } };
	}

	public async resolveCustomEditor(document: vibecoda.CustomDocument, webviewEditor: vibecoda.WebviewPanel): Promise<void> {
		new VideoPreview(this.extensionRoot, document.uri, webviewEditor, this.binarySizeStatusBarEntry);
	}
}


class VideoPreview extends MediaPreview {

	constructor(
		private readonly extensionRoot: vibecoda.Uri,
		resource: vibecoda.Uri,
		webviewEditor: vibecoda.WebviewPanel,
		binarySizeStatusBarEntry: BinarySizeStatusBarEntry,
	) {
		super(extensionRoot, resource, webviewEditor, binarySizeStatusBarEntry);

		this._register(webviewEditor.webview.onDidReceiveMessage(message => {
			switch (message.type) {
				case 'reopen-as-text': {
					reopenAsText(resource, webviewEditor.viewColumn);
					break;
				}
			}
		}));

		this.updateBinarySize();
		this.render();
		this.updateState();
	}

	protected async getWebviewContents(): Promise<string> {
		const version = Date.now().toString();
		const configurations = vibecoda.workspace.getConfiguration('mediaPreview.video');
		const settings = {
			src: await this.getResourcePath(this._webviewEditor, this._resource, version),
			autoplay: configurations.get('autoPlay'),
			loop: configurations.get('loop'),
		};

		const nonce = generateUuid();

		const cspSource = this._webviewEditor.webview.cspSource;
		return /* html */`<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="UTF-8">

	<!-- Disable pinch zooming -->
	<meta name="viewport"
		content="width=device-width, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0, user-scalable=no">

	<title>Video Preview</title>

	<link rel="stylesheet" href="${escapeAttribute(this.extensionResource('media', 'videoPreview.css'))}" type="text/css" media="screen" nonce="${nonce}">

	<meta http-equiv="Content-Security-Policy" content="default-src 'none'; img-src data: ${cspSource}; media-src ${cspSource}; script-src 'nonce-${nonce}'; style-src ${cspSource} 'nonce-${nonce}';">
	<meta id="settings" data-settings="${escapeAttribute(JSON.stringify(settings))}">
</head>
<body class="loading" data-vibecoda-context='{ "preventDefaultContextMenuItems": true }'>
	<div class="loading-indicator"></div>
	<div class="loading-error">
		<p>${vibecoda.l10n.t("An error occurred while loading the video file.")}</p>
		<a href="#" class="open-file-link">${vibecoda.l10n.t("Open file using Vibecoda's standard text/binary editor?")}</a>
	</div>
	<script src="${escapeAttribute(this.extensionResource('media', 'videoPreview.js'))}" nonce="${nonce}"></script>
</body>
</html>`;
	}

	private async getResourcePath(webviewEditor: vibecoda.WebviewPanel, resource: vibecoda.Uri, version: string): Promise<string | null> {
		if (resource.scheme === 'git') {
			const stat = await vibecoda.workspace.fs.stat(resource);
			if (stat.size === 0) {
				// The file is stored on git lfs
				return null;
			}
		}

		// Avoid adding cache busting if there is already a query string
		if (resource.query) {
			return webviewEditor.webview.asWebviewUri(resource).toString();
		}
		return webviewEditor.webview.asWebviewUri(resource).with({ query: `version=${version}` }).toString();
	}

	private extensionResource(...parts: string[]) {
		return this._webviewEditor.webview.asWebviewUri(vibecoda.Uri.joinPath(this.extensionRoot, ...parts));
	}
}

export function registerVideoPreviewSupport(context: vibecoda.ExtensionContext, binarySizeStatusBarEntry: BinarySizeStatusBarEntry): vibecoda.Disposable {
	const provider = new VideoPreviewProvider(context.extensionUri, binarySizeStatusBarEntry);
	return vibecoda.window.registerCustomEditorProvider(VideoPreviewProvider.viewType, provider, {
		supportsMultipleEditorsPerDocument: true,
		webviewOptions: {
			retainContextWhenHidden: true,
		}
	});
}
