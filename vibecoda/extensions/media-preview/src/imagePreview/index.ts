/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as vibecoda from 'vibecoda';
import { BinarySizeStatusBarEntry } from '../binarySizeStatusBarEntry';
import { MediaPreview, PreviewState, reopenAsText } from '../mediaPreview';
import { escapeAttribute } from '../util/dom';
import { generateUuid } from '../util/uuid';
import { SizeStatusBarEntry } from './sizeStatusBarEntry';
import { Scale, ZoomStatusBarEntry } from './zoomStatusBarEntry';


export class ImagePreviewManager implements vibecoda.CustomReadonlyEditorProvider {

	public static readonly viewType = 'imagePreview.previewEditor';

	private readonly _previews = new Set<ImagePreview>();
	private _activePreview: ImagePreview | undefined;

	constructor(
		private readonly extensionRoot: vibecoda.Uri,
		private readonly sizeStatusBarEntry: SizeStatusBarEntry,
		private readonly binarySizeStatusBarEntry: BinarySizeStatusBarEntry,
		private readonly zoomStatusBarEntry: ZoomStatusBarEntry,
	) { }

	public async openCustomDocument(uri: vibecoda.Uri) {
		return { uri, dispose: () => { } };
	}

	public async resolveCustomEditor(
		document: vibecoda.CustomDocument,
		webviewEditor: vibecoda.WebviewPanel,
	): Promise<void> {
		const preview = new ImagePreview(this.extensionRoot, document.uri, webviewEditor, this.sizeStatusBarEntry, this.binarySizeStatusBarEntry, this.zoomStatusBarEntry);
		this._previews.add(preview);
		this.setActivePreview(preview);

		webviewEditor.onDidDispose(() => { this._previews.delete(preview); });

		webviewEditor.onDidChangeViewState(() => {
			if (webviewEditor.active) {
				this.setActivePreview(preview);
			} else if (this._activePreview === preview && !webviewEditor.active) {
				this.setActivePreview(undefined);
			}
		});
	}

	public get activePreview() {
		return this._activePreview;
	}

	public getPreviewFor(resource: vibecoda.Uri, viewColumn?: vibecoda.ViewColumn): ImagePreview | undefined {
		for (const preview of this._previews) {
			if (preview.resource.toString() === resource.toString()) {
				if (!viewColumn || preview.viewColumn === viewColumn) {
					return preview;
				}
			}
		}
		return undefined;
	}

	private setActivePreview(value: ImagePreview | undefined): void {
		this._activePreview = value;
	}
}


class ImagePreview extends MediaPreview {

	private _imageSize: string | undefined;
	private _imageZoom: Scale | undefined;

	private readonly emptyPngDataUri = 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAAEElEQVR42gEFAPr/AP///wAI/AL+Sr4t6gAAAABJRU5ErkJggg==';

	constructor(
		private readonly extensionRoot: vibecoda.Uri,
		resource: vibecoda.Uri,
		webviewEditor: vibecoda.WebviewPanel,
		private readonly sizeStatusBarEntry: SizeStatusBarEntry,
		binarySizeStatusBarEntry: BinarySizeStatusBarEntry,
		private readonly zoomStatusBarEntry: ZoomStatusBarEntry,
	) {
		super(extensionRoot, resource, webviewEditor, binarySizeStatusBarEntry);

		this._register(webviewEditor.webview.onDidReceiveMessage(message => {
			switch (message.type) {
				case 'size': {
					this._imageSize = message.value;
					this.updateState();
					break;
				}
				case 'zoom': {
					this._imageZoom = message.value;
					this.updateState();
					break;
				}
				case 'reopen-as-text': {
					reopenAsText(resource, webviewEditor.viewColumn);
					break;
				}
			}
		}));

		this._register(zoomStatusBarEntry.onDidChangeScale(e => {
			if (this.previewState === PreviewState.Active) {
				this._webviewEditor.webview.postMessage({ type: 'setScale', scale: e.scale });
			}
		}));

		this._register(webviewEditor.onDidChangeViewState(() => {
			this._webviewEditor.webview.postMessage({ type: 'setActive', value: this._webviewEditor.active });
		}));

		this._register(webviewEditor.onDidDispose(() => {
			if (this.previewState === PreviewState.Active) {
				this.sizeStatusBarEntry.hide(this);
				this.zoomStatusBarEntry.hide(this);
			}
			this.previewState = PreviewState.Disposed;
		}));

		this.updateBinarySize();
		this.render();
		this.updateState();
	}

	public override dispose(): void {
		super.dispose();
		this.sizeStatusBarEntry.hide(this);
		this.zoomStatusBarEntry.hide(this);
	}

	public get viewColumn() {
		return this._webviewEditor.viewColumn;
	}

	public zoomIn() {
		if (this.previewState === PreviewState.Active) {
			this._webviewEditor.webview.postMessage({ type: 'zoomIn' });
		}
	}

	public zoomOut() {
		if (this.previewState === PreviewState.Active) {
			this._webviewEditor.webview.postMessage({ type: 'zoomOut' });
		}
	}

	public copyImage() {
		if (this.previewState === PreviewState.Active) {
			this._webviewEditor.reveal();
			this._webviewEditor.webview.postMessage({ type: 'copyImage' });
		}
	}

	protected override updateState() {
		super.updateState();

		if (this.previewState === PreviewState.Disposed) {
			return;
		}

		if (this._webviewEditor.active) {
			this.sizeStatusBarEntry.show(this, this._imageSize || '');
			this.zoomStatusBarEntry.show(this, this._imageZoom || 'fit');
		} else {
			this.sizeStatusBarEntry.hide(this);
			this.zoomStatusBarEntry.hide(this);
		}
	}

	protected override async render(): Promise<void> {
		await super.render();
		this._webviewEditor.webview.postMessage({ type: 'setActive', value: this._webviewEditor.active });
	}

	protected override async getWebviewContents(): Promise<string> {
		const version = Date.now().toString();
		const settings = {
			src: await this.getResourcePath(this._webviewEditor, this._resource, version),
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

	<title>Image Preview</title>

	<link rel="stylesheet" href="${escapeAttribute(this.extensionResource('media', 'imagePreview.css'))}" type="text/css" media="screen" nonce="${nonce}">

	<meta http-equiv="Content-Security-Policy" content="default-src 'none'; img-src data: ${cspSource}; connect-src ${cspSource}; script-src 'nonce-${nonce}'; style-src ${cspSource} 'nonce-${nonce}';">
	<meta id="image-preview-settings" data-settings="${escapeAttribute(JSON.stringify(settings))}">
</head>
<body class="container image scale-to-fit loading" data-vibecoda-context='{ "preventDefaultContextMenuItems": true }'>
	<div class="loading-indicator"></div>
	<div class="image-load-error">
		<p>${vibecoda.l10n.t("An error occurred while loading the image.")}</p>
		<a href="#" class="open-file-link">${vibecoda.l10n.t("Open file using Vibecoda's standard text/binary editor?")}</a>
	</div>
	<script src="${escapeAttribute(this.extensionResource('media', 'imagePreview.js'))}" nonce="${nonce}"></script>
</body>
</html>`;
	}

	private async getResourcePath(webviewEditor: vibecoda.WebviewPanel, resource: vibecoda.Uri, version: string): Promise<string> {
		if (resource.scheme === 'git') {
			const stat = await vibecoda.workspace.fs.stat(resource);
			if (stat.size === 0) {
				return this.emptyPngDataUri;
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

	public async reopenAsText() {
		await vibecoda.commands.executeCommand('reopenActiveEditorWith', 'default');
		this._webviewEditor.dispose();
	}
}


export function registerImagePreviewSupport(context: vibecoda.ExtensionContext, binarySizeStatusBarEntry: BinarySizeStatusBarEntry): vibecoda.Disposable {
	const disposables: vibecoda.Disposable[] = [];

	const sizeStatusBarEntry = new SizeStatusBarEntry();
	disposables.push(sizeStatusBarEntry);

	const zoomStatusBarEntry = new ZoomStatusBarEntry();
	disposables.push(zoomStatusBarEntry);

	const previewManager = new ImagePreviewManager(context.extensionUri, sizeStatusBarEntry, binarySizeStatusBarEntry, zoomStatusBarEntry);

	disposables.push(vibecoda.window.registerCustomEditorProvider(ImagePreviewManager.viewType, previewManager, {
		supportsMultipleEditorsPerDocument: true,
	}));

	disposables.push(vibecoda.commands.registerCommand('imagePreview.zoomIn', () => {
		previewManager.activePreview?.zoomIn();
	}));

	disposables.push(vibecoda.commands.registerCommand('imagePreview.zoomOut', () => {
		previewManager.activePreview?.zoomOut();
	}));

	disposables.push(vibecoda.commands.registerCommand('imagePreview.copyImage', () => {
		previewManager.activePreview?.copyImage();
	}));

	disposables.push(vibecoda.commands.registerCommand('imagePreview.reopenAsText', async () => {
		return previewManager.activePreview?.reopenAsText();
	}));

	disposables.push(vibecoda.commands.registerCommand('imagePreview.reopenAsPreview', async () => {

		await vibecoda.commands.executeCommand('reopenActiveEditorWith', ImagePreviewManager.viewType);
	}));

	return vibecoda.Disposable.from(...disposables);
}
