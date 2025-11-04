/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as vibecoda from 'vibecoda';
import { BaseLanguageClient, LanguageClientOptions, NotebookDocumentSyncRegistrationType, Range, TextEdit } from 'vibecoda-languageclient';
import { IMdParser } from '../markdownEngine';
import { IDisposable } from '../util/dispose';
import { looksLikeMarkdownPath, markdownFileExtensions, markdownLanguageIds } from '../util/file';
import { FileWatcherManager } from './fileWatchingManager';
import { InMemoryDocument } from './inMemoryDocument';
import * as proto from './protocol';
import { VsCodeMdWorkspace } from './workspace';

export type LanguageClientConstructor = (name: string, description: string, clientOptions: LanguageClientOptions) => BaseLanguageClient;

export class MdLanguageClient implements IDisposable {

	constructor(
		private readonly _client: BaseLanguageClient,
		private readonly _workspace: VsCodeMdWorkspace,
	) { }

	dispose(): void {
		this._client.stop();
		this._workspace.dispose();
	}

	resolveLinkTarget(linkText: string, uri: vibecoda.Uri): Promise<proto.ResolvedDocumentLinkTarget> {
		return this._client.sendRequest(proto.resolveLinkTarget, { linkText, uri: uri.toString() });
	}

	getEditForFileRenames(files: ReadonlyArray<{ oldUri: string; newUri: string }>, token: vibecoda.CancellationToken) {
		return this._client.sendRequest(proto.getEditForFileRenames, files, token);
	}

	getReferencesToFileInWorkspace(resource: vibecoda.Uri, token: vibecoda.CancellationToken) {
		return this._client.sendRequest(proto.getReferencesToFileInWorkspace, { uri: resource.toString() }, token);
	}

	prepareUpdatePastedLinks(doc: vibecoda.Uri, ranges: readonly vibecoda.Range[], token: vibecoda.CancellationToken) {
		return this._client.sendRequest(proto.prepareUpdatePastedLinks, {
			uri: doc.toString(),
			ranges: ranges.map(range => Range.create(range.start.line, range.start.character, range.end.line, range.end.character)),
		}, token);
	}

	getUpdatePastedLinksEdit(pastingIntoDoc: vibecoda.Uri, edits: readonly vibecoda.TextEdit[], metadata: string, token: vibecoda.CancellationToken) {
		return this._client.sendRequest(proto.getUpdatePastedLinksEdit, {
			metadata,
			pasteIntoDoc: pastingIntoDoc.toString(),
			edits: edits.map(edit => TextEdit.replace(edit.range, edit.newText)),
		}, token);
	}
}

export async function startClient(factory: LanguageClientConstructor, parser: IMdParser): Promise<MdLanguageClient> {

	const mdFileGlob = `**/*.{${markdownFileExtensions.join(',')}}`;

	const clientOptions: LanguageClientOptions = {
		documentSelector: markdownLanguageIds,
		synchronize: {
			configurationSection: ['markdown'],
			fileEvents: vibecoda.workspace.createFileSystemWatcher(mdFileGlob),
		},
		initializationOptions: {
			markdownFileExtensions,
			i10lLocation: vibecoda.l10n.uri?.toJSON(),
		},
		diagnosticPullOptions: {
			onChange: true,
			onTabs: true,
			match(_documentSelector, resource) {
				return looksLikeMarkdownPath(resource);
			},
		},
		markdown: {
			supportHtml: true,
		}
	};

	const client = factory('markdown', vibecoda.l10n.t("Markdown Language Server"), clientOptions);

	client.registerProposedFeatures();

	const notebookFeature = client.getFeature(NotebookDocumentSyncRegistrationType.method);
	if (notebookFeature !== undefined) {
		notebookFeature.register({
			id: String(Date.now()),
			registerOptions: {
				notebookSelector: [{
					notebook: '*',
					cells: [{ language: 'markdown' }]
				}]
			}
		});
	}

	const workspace = new VsCodeMdWorkspace();

	client.onRequest(proto.parse, async (e) => {
		const uri = vibecoda.Uri.parse(e.uri);
		if (typeof e.text === 'string') {
			return parser.tokenize(new InMemoryDocument(uri, e.text, -1));
		} else {
			const doc = await workspace.getOrLoadMarkdownDocument(uri);
			if (doc) {
				return parser.tokenize(doc);
			} else {
				return [];
			}
		}
	});

	client.onRequest(proto.fs_readFile, async (e): Promise<number[]> => {
		const uri = vibecoda.Uri.parse(e.uri);
		return Array.from(await vibecoda.workspace.fs.readFile(uri));
	});

	client.onRequest(proto.fs_stat, async (e): Promise<{ isDirectory: boolean } | undefined> => {
		const uri = vibecoda.Uri.parse(e.uri);
		try {
			const stat = await vibecoda.workspace.fs.stat(uri);
			return { isDirectory: stat.type === vibecoda.FileType.Directory };
		} catch {
			return undefined;
		}
	});

	client.onRequest(proto.fs_readDirectory, async (e): Promise<[string, { isDirectory: boolean }][]> => {
		const uri = vibecoda.Uri.parse(e.uri);
		const result = await vibecoda.workspace.fs.readDirectory(uri);
		return result.map(([name, type]) => [name, { isDirectory: type === vibecoda.FileType.Directory }]);
	});

	client.onRequest(proto.findMarkdownFilesInWorkspace, async (): Promise<string[]> => {
		return (await vibecoda.workspace.findFiles(mdFileGlob, '**/node_modules/**')).map(x => x.toString());
	});

	const watchers = new FileWatcherManager();

	client.onRequest(proto.fs_watcher_create, async (params): Promise<void> => {
		const id = params.id;
		const uri = vibecoda.Uri.parse(params.uri);

		const sendWatcherChange = (kind: 'create' | 'change' | 'delete') => {
			client.sendRequest(proto.fs_watcher_onChange, { id, uri: params.uri, kind });
		};

		watchers.create(id, uri, params.watchParentDirs, {
			create: params.options.ignoreCreate ? undefined : () => sendWatcherChange('create'),
			change: params.options.ignoreChange ? undefined : () => sendWatcherChange('change'),
			delete: params.options.ignoreDelete ? undefined : () => sendWatcherChange('delete'),
		});
	});

	client.onRequest(proto.fs_watcher_delete, async (params): Promise<void> => {
		watchers.delete(params.id);
	});

	vibecoda.commands.registerCommand('vibecodaMarkdownLanguageservice.open', (uri, args) => {
		return vibecoda.commands.executeCommand('vibecoda.open', uri, args);
	});

	vibecoda.commands.registerCommand('vibecodaMarkdownLanguageservice.rename', (uri, pos) => {
		return vibecoda.commands.executeCommand('editor.action.rename', [vibecoda.Uri.from(uri), new vibecoda.Position(pos.line, pos.character)]);
	});

	await client.start();

	return new MdLanguageClient(client, workspace);
}
