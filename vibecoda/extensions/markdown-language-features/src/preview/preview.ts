/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as vibecoda from 'vibecoda';
import * as uri from 'vibecoda-uri';
import { ILogger } from '../logging';
import { MarkdownContributionProvider } from '../markdownExtensions';
import { Disposable } from '../util/dispose';
import { isMarkdownFile } from '../util/file';
import { MdLinkOpener } from '../util/openDocumentLink';
import { WebviewResourceProvider } from '../util/resources';
import { urlToUri } from '../util/url';
import { ImageInfo, MdDocumentRenderer } from './documentRenderer';
import { MarkdownPreviewConfigurationManager } from './previewConfig';
import { scrollEditorToLine, StartingScrollFragment, StartingScrollLine, StartingScrollLocation } from './scrolling';
import { getVisibleLine, LastScrollLocation, TopmostLineMonitor } from './topmostLineMonitor';
import type { FromWebviewMessage, ToWebviewMessage } from '../../types/previewMessaging';

export class PreviewDocumentVersion {

	public readonly resource: vibecoda.Uri;
	private readonly _version: number;

	public constructor(document: vibecoda.TextDocument) {
		this.resource = document.uri;
		this._version = document.version;
	}

	public equals(other: PreviewDocumentVersion): boolean {
		return this.resource.fsPath === other.resource.fsPath
			&& this._version === other._version;
	}
}

interface MarkdownPreviewDelegate {
	getTitle?(resource: vibecoda.Uri): string;
	getAdditionalState(): {};
	openPreviewLinkToMarkdownFile(markdownLink: vibecoda.Uri, fragment: string | undefined): void;
}

class MarkdownPreview extends Disposable implements WebviewResourceProvider {

	private static readonly _unwatchedImageSchemes = new Set(['https', 'http', 'data']);

	private _disposed: boolean = false;

	private readonly _delay = 300;
	private _throttleTimer: any;

	private readonly _resource: vibecoda.Uri;
	private readonly _webviewPanel: vibecoda.WebviewPanel;

	private _line: number | undefined;
	private readonly _scrollToFragment: string | undefined;
	private _firstUpdate = true;
	private _currentVersion?: PreviewDocumentVersion;
	private _isScrolling = false;

	private _imageInfo: readonly ImageInfo[] = [];
	private readonly _fileWatchersBySrc = new Map</* src: */ string, vibecoda.FileSystemWatcher>();

	private readonly _onScrollEmitter = this._register(new vibecoda.EventEmitter<LastScrollLocation>());
	public readonly onScroll = this._onScrollEmitter.event;

	private readonly _disposeCts = this._register(new vibecoda.CancellationTokenSource());

	constructor(
		webview: vibecoda.WebviewPanel,
		resource: vibecoda.Uri,
		startingScroll: StartingScrollLocation | undefined,
		private readonly _delegate: MarkdownPreviewDelegate,
		private readonly _contentProvider: MdDocumentRenderer,
		private readonly _previewConfigurations: MarkdownPreviewConfigurationManager,
		private readonly _logger: ILogger,
		private readonly _contributionProvider: MarkdownContributionProvider,
		private readonly _opener: MdLinkOpener,
	) {
		super();

		this._webviewPanel = webview;
		this._resource = resource;

		switch (startingScroll?.type) {
			case 'line':
				if (!isNaN(startingScroll.line!)) {
					this._line = startingScroll.line;
				}
				break;

			case 'fragment':
				this._scrollToFragment = startingScroll.fragment;
				break;
		}

		this._register(_contributionProvider.onContributionsChanged(() => {
			setTimeout(() => this.refresh(true), 0);
		}));

		this._register(vibecoda.workspace.onDidChangeTextDocument(event => {
			if (this.isPreviewOf(event.document.uri)) {
				this.refresh();
			}
		}));

		this._register(vibecoda.workspace.onDidOpenTextDocument(document => {
			if (this.isPreviewOf(document.uri)) {
				this.refresh();
			}
		}));

		const watcher = this._register(vibecoda.workspace.createFileSystemWatcher(new vibecoda.RelativePattern(resource, '*')));
		this._register(watcher.onDidChange(uri => {
			if (this.isPreviewOf(uri)) {
				// Only use the file system event when Vibecoda does not already know about the file
				if (!vibecoda.workspace.textDocuments.some(doc => doc.uri.toString() === uri.toString())) {
					this.refresh();
				}
			}
		}));

		this._register(this._webviewPanel.webview.onDidReceiveMessage((e: FromWebviewMessage.Type) => {
			if (e.source !== this._resource.toString()) {
				return;
			}

			switch (e.type) {
				case 'cacheImageSizes':
					this._imageInfo = e.imageData;
					break;

				case 'revealLine':
					this._onDidScrollPreview(e.line);
					break;

				case 'didClick':
					this._onDidClickPreview(e.line);
					break;

				case 'openLink':
					this._onDidClickPreviewLink(e.href);
					break;

				case 'showPreviewSecuritySelector':
					vibecoda.commands.executeCommand('markdown.showPreviewSecuritySelector', e.source);
					break;

				case 'previewStyleLoadError':
					vibecoda.window.showWarningMessage(
						vibecoda.l10n.t("Could not load 'markdown.styles': {0}", e.unloadedStyles.join(', ')));
					break;
			}
		}));

		this.refresh();
	}

	override dispose() {
		this._disposeCts.cancel();

		super.dispose();

		this._disposed = true;

		clearTimeout(this._throttleTimer);
		for (const entry of this._fileWatchersBySrc.values()) {
			entry.dispose();
		}
		this._fileWatchersBySrc.clear();
	}

	public get resource(): vibecoda.Uri {
		return this._resource;
	}

	public get state() {
		return {
			resource: this._resource.toString(),
			line: this._line,
			fragment: this._scrollToFragment,
			...this._delegate.getAdditionalState(),
		};
	}

	/**
	 * The first call immediately refreshes the preview,
	 * calls happening shortly thereafter are debounced.
	*/
	public refresh(forceUpdate: boolean = false) {
		// Schedule update if none is pending
		if (!this._throttleTimer) {
			if (this._firstUpdate) {
				this._updatePreview(true);
			} else {
				this._throttleTimer = setTimeout(() => this._updatePreview(forceUpdate), this._delay);
			}
		}

		this._firstUpdate = false;
	}


	public isPreviewOf(resource: vibecoda.Uri): boolean {
		return this._resource.fsPath === resource.fsPath;
	}

	public postMessage(msg: ToWebviewMessage.Type) {
		if (!this._disposed) {
			this._webviewPanel.webview.postMessage(msg);
		}
	}

	public scrollTo(topLine: number) {
		if (this._disposed) {
			return;
		}

		if (this._isScrolling) {
			this._isScrolling = false;
			return;
		}

		this._logger.trace('MarkdownPreview', 'updateForView', { markdownFile: this._resource });
		this._line = topLine;
		this.postMessage({
			type: 'updateView',
			line: topLine,
			source: this._resource.toString()
		});
	}

	private async _updatePreview(forceUpdate?: boolean): Promise<void> {
		clearTimeout(this._throttleTimer);
		this._throttleTimer = undefined;

		if (this._disposed) {
			return;
		}

		let document: vibecoda.TextDocument;
		try {
			document = await vibecoda.workspace.openTextDocument(this._resource);
		} catch {
			if (!this._disposed) {
				await this._showFileNotFoundError();
			}
			return;
		}

		if (this._disposed) {
			return;
		}

		const pendingVersion = new PreviewDocumentVersion(document);
		if (!forceUpdate && this._currentVersion?.equals(pendingVersion)) {
			if (this._line) {
				this.scrollTo(this._line);
			}
			return;
		}

		const shouldReloadPage = forceUpdate || !this._currentVersion || this._currentVersion.resource.toString() !== pendingVersion.resource.toString() || !this._webviewPanel.visible;
		this._currentVersion = pendingVersion;

		let selectedLine: number | undefined = undefined;
		for (const editor of vibecoda.window.visibleTextEditors) {
			if (this.isPreviewOf(editor.document.uri)) {
				selectedLine = editor.selection.active.line;
				break;
			}
		}

		const content = await (shouldReloadPage
			? this._contentProvider.renderDocument(document, this, this._previewConfigurations, this._line, selectedLine, this.state, this._imageInfo, this._disposeCts.token)
			: this._contentProvider.renderBody(document, this));

		// Another call to `doUpdate` may have happened.
		// Make sure we are still updating for the correct document
		if (this._currentVersion?.equals(pendingVersion)) {
			this._updateWebviewContent(content.html, shouldReloadPage);
			this._updateImageWatchers(content.containingImages);
		}
	}

	private _onDidScrollPreview(line: number) {
		this._line = line;
		this._onScrollEmitter.fire({ line: this._line, uri: this._resource });
		const config = this._previewConfigurations.loadAndCacheConfiguration(this._resource);
		if (!config.scrollEditorWithPreview) {
			return;
		}

		for (const editor of vibecoda.window.visibleTextEditors) {
			if (!this.isPreviewOf(editor.document.uri)) {
				continue;
			}

			this._isScrolling = true;
			scrollEditorToLine(line, editor);
		}
	}

	private async _onDidClickPreview(line: number): Promise<void> {
		// fix #82457, find currently opened but unfocused source tab
		await vibecoda.commands.executeCommand('markdown.showSource');

		const revealLineInEditor = (editor: vibecoda.TextEditor) => {
			const position = new vibecoda.Position(line, 0);
			const newSelection = new vibecoda.Selection(position, position);
			editor.selection = newSelection;
			editor.revealRange(newSelection, vibecoda.TextEditorRevealType.InCenterIfOutsideViewport);
		};

		for (const visibleEditor of vibecoda.window.visibleTextEditors) {
			if (this.isPreviewOf(visibleEditor.document.uri)) {
				const editor = await vibecoda.window.showTextDocument(visibleEditor.document, visibleEditor.viewColumn);
				revealLineInEditor(editor);
				return;
			}
		}

		await vibecoda.workspace.openTextDocument(this._resource)
			.then(vibecoda.window.showTextDocument)
			.then((editor) => {
				revealLineInEditor(editor);
			}, () => {
				vibecoda.window.showErrorMessage(vibecoda.l10n.t('Could not open {0}', this._resource.toString()));
			});
	}

	private async _showFileNotFoundError() {
		this._webviewPanel.webview.html = this._contentProvider.renderFileNotFoundDocument(this._resource);
	}

	private _updateWebviewContent(html: string, reloadPage: boolean): void {
		if (this._disposed) {
			return;
		}

		if (this._delegate.getTitle) {
			this._webviewPanel.title = this._delegate.getTitle(this._resource);
		}
		this._webviewPanel.webview.options = this._getWebviewOptions();

		if (reloadPage) {
			this._webviewPanel.webview.html = html;
		} else {
			this.postMessage({
				type: 'updateContent',
				content: html,
				source: this._resource.toString(),
			});
		}
	}

	private _updateImageWatchers(srcs: Set<string>) {
		// Delete stale file watchers.
		for (const [src, watcher] of this._fileWatchersBySrc) {
			if (!srcs.has(src)) {
				watcher.dispose();
				this._fileWatchersBySrc.delete(src);
			}
		}

		// Create new file watchers.
		const root = vibecoda.Uri.joinPath(this._resource, '../');
		for (const src of srcs) {
			const uri = urlToUri(src, root);
			if (uri && !MarkdownPreview._unwatchedImageSchemes.has(uri.scheme) && !this._fileWatchersBySrc.has(src)) {
				const watcher = vibecoda.workspace.createFileSystemWatcher(new vibecoda.RelativePattern(uri, '*'));
				watcher.onDidChange(() => {
					this.refresh(true);
				});
				this._fileWatchersBySrc.set(src, watcher);
			}
		}
	}

	private _getWebviewOptions(): vibecoda.WebviewOptions {
		return {
			enableScripts: true,
			enableForms: false,
			localResourceRoots: this._getLocalResourceRoots()
		};
	}

	private _getLocalResourceRoots(): ReadonlyArray<vibecoda.Uri> {
		const baseRoots = Array.from(this._contributionProvider.contributions.previewResourceRoots);

		const folder = vibecoda.workspace.getWorkspaceFolder(this._resource);
		if (folder) {
			const workspaceRoots = vibecoda.workspace.workspaceFolders?.map(folder => folder.uri);
			if (workspaceRoots) {
				baseRoots.push(...workspaceRoots);
			}
		} else {
			baseRoots.push(uri.Utils.dirname(this._resource));
		}

		return baseRoots;
	}

	private async _onDidClickPreviewLink(href: string) {
		const config = vibecoda.workspace.getConfiguration('markdown', this.resource);
		const openLinks = config.get<string>('preview.openMarkdownLinks', 'inPreview');
		if (openLinks === 'inPreview') {
			const resolved = await this._opener.resolveDocumentLink(href, this.resource);
			if (resolved.kind === 'file') {
				try {
					const doc = await vibecoda.workspace.openTextDocument(vibecoda.Uri.from(resolved.uri));
					if (isMarkdownFile(doc)) {
						return this._delegate.openPreviewLinkToMarkdownFile(doc.uri, resolved.fragment ? decodeURIComponent(resolved.fragment) : undefined);
					}
				} catch {
					// Noop
				}
			}
		}

		return this._opener.openDocumentLink(href, this.resource);
	}

	//#region WebviewResourceProvider

	asWebviewUri(resource: vibecoda.Uri) {
		return this._webviewPanel.webview.asWebviewUri(resource);
	}

	get cspSource() {
		return [
			this._webviewPanel.webview.cspSource,

			// On web, we also need to allow loading of resources from contributed extensions
			...this._contributionProvider.contributions.previewResourceRoots
				.filter(root => root.scheme === 'http' || root.scheme === 'https')
				.map(root => {
					const dirRoot = root.path.endsWith('/') ? root : root.with({ path: root.path + '/' });
					return dirRoot.toString();
				}),
		].join(' ');
	}

	//#endregion
}

export interface IManagedMarkdownPreview {

	readonly resource: vibecoda.Uri;
	readonly resourceColumn: vibecoda.ViewColumn;

	readonly onDispose: vibecoda.Event<void>;
	readonly onDidChangeViewState: vibecoda.Event<vibecoda.WebviewPanelOnDidChangeViewStateEvent>;

	copyImage(id: string): void;
	dispose(): void;
	refresh(): void;
	updateConfiguration(): void;

	matchesResource(
		otherResource: vibecoda.Uri,
		otherPosition: vibecoda.ViewColumn | undefined,
		otherLocked: boolean
	): boolean;
}

export class StaticMarkdownPreview extends Disposable implements IManagedMarkdownPreview {

	public static readonly customEditorViewType = 'vibecoda.markdown.preview.editor';

	public static revive(
		resource: vibecoda.Uri,
		webview: vibecoda.WebviewPanel,
		contentProvider: MdDocumentRenderer,
		previewConfigurations: MarkdownPreviewConfigurationManager,
		topmostLineMonitor: TopmostLineMonitor,
		logger: ILogger,
		contributionProvider: MarkdownContributionProvider,
		opener: MdLinkOpener,
		scrollLine?: number,
	): StaticMarkdownPreview {
		return new StaticMarkdownPreview(webview, resource, contentProvider, previewConfigurations, topmostLineMonitor, logger, contributionProvider, opener, scrollLine);
	}

	private readonly _preview: MarkdownPreview;

	private constructor(
		private readonly _webviewPanel: vibecoda.WebviewPanel,
		resource: vibecoda.Uri,
		contentProvider: MdDocumentRenderer,
		private readonly _previewConfigurations: MarkdownPreviewConfigurationManager,
		topmostLineMonitor: TopmostLineMonitor,
		logger: ILogger,
		contributionProvider: MarkdownContributionProvider,
		opener: MdLinkOpener,
		scrollLine?: number,
	) {
		super();
		const topScrollLocation = scrollLine ? new StartingScrollLine(scrollLine) : undefined;
		this._preview = this._register(new MarkdownPreview(this._webviewPanel, resource, topScrollLocation, {
			getAdditionalState: () => { return {}; },
			openPreviewLinkToMarkdownFile: (markdownLink, fragment) => {
				return vibecoda.commands.executeCommand('vibecoda.openWith', markdownLink.with({
					fragment
				}), StaticMarkdownPreview.customEditorViewType, this._webviewPanel.viewColumn);
			}
		}, contentProvider, _previewConfigurations, logger, contributionProvider, opener));

		this._register(this._webviewPanel.onDidDispose(() => {
			this.dispose();
		}));

		this._register(this._webviewPanel.onDidChangeViewState(e => {
			this._onDidChangeViewState.fire(e);
		}));

		this._register(this._preview.onScroll((scrollInfo) => {
			topmostLineMonitor.setPreviousStaticEditorLine(scrollInfo);
		}));

		this._register(topmostLineMonitor.onDidChanged(event => {
			if (this._preview.isPreviewOf(event.resource)) {
				this._preview.scrollTo(event.line);
			}
		}));
	}

	copyImage(id: string) {
		this._webviewPanel.reveal();
		this._preview.postMessage({
			type: 'copyImage',
			source: this.resource.toString(),
			id: id
		});
	}

	private readonly _onDispose = this._register(new vibecoda.EventEmitter<void>());
	public readonly onDispose = this._onDispose.event;

	private readonly _onDidChangeViewState = this._register(new vibecoda.EventEmitter<vibecoda.WebviewPanelOnDidChangeViewStateEvent>());
	public readonly onDidChangeViewState = this._onDidChangeViewState.event;

	override dispose() {
		this._onDispose.fire();
		super.dispose();
	}

	public matchesResource(
		_otherResource: vibecoda.Uri,
		_otherPosition: vibecoda.ViewColumn | undefined,
		_otherLocked: boolean
	): boolean {
		return false;
	}

	public refresh() {
		this._preview.refresh(true);
	}

	public updateConfiguration() {
		if (this._previewConfigurations.hasConfigurationChanged(this._preview.resource)) {
			this.refresh();
		}
	}

	public get resource() {
		return this._preview.resource;
	}

	public get resourceColumn() {
		return this._webviewPanel.viewColumn || vibecoda.ViewColumn.One;
	}
}

interface DynamicPreviewInput {
	readonly resource: vibecoda.Uri;
	readonly resourceColumn: vibecoda.ViewColumn;
	readonly locked: boolean;
	readonly line?: number;
}

export class DynamicMarkdownPreview extends Disposable implements IManagedMarkdownPreview {

	public static readonly viewType = 'markdown.preview';

	private readonly _resourceColumn: vibecoda.ViewColumn;
	private _locked: boolean;

	private readonly _webviewPanel: vibecoda.WebviewPanel;
	private _preview: MarkdownPreview;

	public static revive(
		input: DynamicPreviewInput,
		webview: vibecoda.WebviewPanel,
		contentProvider: MdDocumentRenderer,
		previewConfigurations: MarkdownPreviewConfigurationManager,
		logger: ILogger,
		topmostLineMonitor: TopmostLineMonitor,
		contributionProvider: MarkdownContributionProvider,
		opener: MdLinkOpener,
	): DynamicMarkdownPreview {
		webview.iconPath = contentProvider.iconPath;

		return new DynamicMarkdownPreview(webview, input,
			contentProvider, previewConfigurations, logger, topmostLineMonitor, contributionProvider, opener);
	}

	public static create(
		input: DynamicPreviewInput,
		previewColumn: vibecoda.ViewColumn,
		contentProvider: MdDocumentRenderer,
		previewConfigurations: MarkdownPreviewConfigurationManager,
		logger: ILogger,
		topmostLineMonitor: TopmostLineMonitor,
		contributionProvider: MarkdownContributionProvider,
		opener: MdLinkOpener,
	): DynamicMarkdownPreview {
		const webview = vibecoda.window.createWebviewPanel(
			DynamicMarkdownPreview.viewType,
			DynamicMarkdownPreview._getPreviewTitle(input.resource, input.locked),
			previewColumn, { enableFindWidget: true, });

		webview.iconPath = contentProvider.iconPath;

		return new DynamicMarkdownPreview(webview, input,
			contentProvider, previewConfigurations, logger, topmostLineMonitor, contributionProvider, opener);
	}

	private constructor(
		webview: vibecoda.WebviewPanel,
		input: DynamicPreviewInput,
		private readonly _contentProvider: MdDocumentRenderer,
		private readonly _previewConfigurations: MarkdownPreviewConfigurationManager,
		private readonly _logger: ILogger,
		private readonly _topmostLineMonitor: TopmostLineMonitor,
		private readonly _contributionProvider: MarkdownContributionProvider,
		private readonly _opener: MdLinkOpener,
	) {
		super();

		this._webviewPanel = webview;

		this._resourceColumn = input.resourceColumn;
		this._locked = input.locked;

		this._preview = this._createPreview(input.resource, typeof input.line === 'number' ? new StartingScrollLine(input.line) : undefined);

		this._register(webview.onDidDispose(() => { this.dispose(); }));

		this._register(this._webviewPanel.onDidChangeViewState(e => {
			this._onDidChangeViewStateEmitter.fire(e);
		}));

		this._register(this._topmostLineMonitor.onDidChanged(event => {
			if (this._preview.isPreviewOf(event.resource)) {
				this._preview.scrollTo(event.line);
			}
		}));

		this._register(vibecoda.window.onDidChangeTextEditorSelection(event => {
			if (this._preview.isPreviewOf(event.textEditor.document.uri)) {
				this._preview.postMessage({
					type: 'onDidChangeTextEditorSelection',
					line: event.selections[0].active.line,
					source: this._preview.resource.toString()
				});
			}
		}));

		this._register(vibecoda.window.onDidChangeActiveTextEditor(editor => {
			// Only allow previewing normal text editors which have a viewColumn: See #101514
			if (typeof editor?.viewColumn === 'undefined') {
				return;
			}

			if (isMarkdownFile(editor.document) && !this._locked && !this._preview.isPreviewOf(editor.document.uri)) {
				const line = getVisibleLine(editor);
				this.update(editor.document.uri, line ? new StartingScrollLine(line) : undefined);
			}
		}));
	}

	copyImage(id: string) {
		this._webviewPanel.reveal();
		this._preview.postMessage({
			type: 'copyImage',
			source: this.resource.toString(),
			id: id
		});
	}

	private readonly _onDisposeEmitter = this._register(new vibecoda.EventEmitter<void>());
	public readonly onDispose = this._onDisposeEmitter.event;

	private readonly _onDidChangeViewStateEmitter = this._register(new vibecoda.EventEmitter<vibecoda.WebviewPanelOnDidChangeViewStateEvent>());
	public readonly onDidChangeViewState = this._onDidChangeViewStateEmitter.event;

	override dispose() {
		this._preview.dispose();
		this._webviewPanel.dispose();

		this._onDisposeEmitter.fire();
		this._onDisposeEmitter.dispose();
		super.dispose();
	}

	public get resource() {
		return this._preview.resource;
	}

	public get resourceColumn() {
		return this._resourceColumn;
	}

	public reveal(viewColumn: vibecoda.ViewColumn) {
		this._webviewPanel.reveal(viewColumn);
	}

	public refresh() {
		this._preview.refresh(true);
	}

	public updateConfiguration() {
		if (this._previewConfigurations.hasConfigurationChanged(this._preview.resource)) {
			this.refresh();
		}
	}

	public update(newResource: vibecoda.Uri, scrollLocation?: StartingScrollLocation) {
		if (this._preview.isPreviewOf(newResource)) {
			switch (scrollLocation?.type) {
				case 'line':
					this._preview.scrollTo(scrollLocation.line);
					return;

				case 'fragment':
					// Workaround. For fragments, just reload the entire preview
					break;

				default:
					return;
			}
		}

		this._preview.dispose();
		this._preview = this._createPreview(newResource, scrollLocation);
	}

	public toggleLock() {
		this._locked = !this._locked;
		this._webviewPanel.title = DynamicMarkdownPreview._getPreviewTitle(this._preview.resource, this._locked);
	}

	private static _getPreviewTitle(resource: vibecoda.Uri, locked: boolean): string {
		const resourceLabel = uri.Utils.basename(resource);
		return locked
			? vibecoda.l10n.t('[Preview] {0}', resourceLabel)
			: vibecoda.l10n.t('Preview {0}', resourceLabel);
	}

	public get position(): vibecoda.ViewColumn | undefined {
		return this._webviewPanel.viewColumn;
	}

	public matchesResource(
		otherResource: vibecoda.Uri,
		otherPosition: vibecoda.ViewColumn | undefined,
		otherLocked: boolean
	): boolean {
		if (this.position !== otherPosition) {
			return false;
		}

		if (this._locked) {
			return otherLocked && this._preview.isPreviewOf(otherResource);
		} else {
			return !otherLocked;
		}
	}

	public matches(otherPreview: DynamicMarkdownPreview): boolean {
		return this.matchesResource(otherPreview._preview.resource, otherPreview.position, otherPreview._locked);
	}

	private _createPreview(resource: vibecoda.Uri, startingScroll?: StartingScrollLocation): MarkdownPreview {
		return new MarkdownPreview(this._webviewPanel, resource, startingScroll, {
			getTitle: (resource) => DynamicMarkdownPreview._getPreviewTitle(resource, this._locked),
			getAdditionalState: () => {
				return {
					resourceColumn: this.resourceColumn,
					locked: this._locked,
				};
			},
			openPreviewLinkToMarkdownFile: (link: vibecoda.Uri, fragment?: string) => {
				this.update(link, fragment ? new StartingScrollFragment(fragment) : undefined);
			}
		},
			this._contentProvider,
			this._previewConfigurations,
			this._logger,
			this._contributionProvider,
			this._opener);
	}
}
