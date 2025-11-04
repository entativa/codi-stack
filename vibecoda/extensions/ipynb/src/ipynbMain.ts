/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as vibecoda from 'vibecoda';
import { activate as keepNotebookModelStoreInSync } from './notebookModelStoreSync';
import { notebookImagePasteSetup } from './notebookImagePaste';
import { AttachmentCleaner } from './notebookAttachmentCleaner';
import { serializeNotebookToString } from './serializers';
import { defaultNotebookFormat } from './constants';

// From {nbformat.INotebookMetadata} in @jupyterlab/coreutils
type NotebookMetadata = {
	kernelspec?: {
		name: string;
		display_name: string;
		[propName: string]: unknown;
	};
	language_info?: {
		name: string;
		codemirror_mode?: string | {};
		file_extension?: string;
		mimetype?: string;
		pygments_lexer?: string;
		[propName: string]: unknown;
	};
	orig_nbformat?: number;
	[propName: string]: unknown;
};

type OptionsWithCellContentMetadata = vibecoda.NotebookDocumentContentOptions & { cellContentMetadata: { attachments: boolean } };


export function activate(context: vibecoda.ExtensionContext, serializer: vibecoda.NotebookSerializer) {
	keepNotebookModelStoreInSync(context);
	const notebookSerializerOptions: OptionsWithCellContentMetadata = {
		transientOutputs: false,
		transientDocumentMetadata: {
			cells: true,
			indentAmount: true
		},
		transientCellMetadata: {
			breakpointMargin: true,
			id: false,
			metadata: false,
			attachments: false
		},
		cellContentMetadata: {
			attachments: true
		}
	};
	context.subscriptions.push(vibecoda.workspace.registerNotebookSerializer('jupyter-notebook', serializer, notebookSerializerOptions));

	const interactiveSerializeOptions: OptionsWithCellContentMetadata = {
		transientOutputs: false,
		transientCellMetadata: {
			breakpointMargin: true,
			id: false,
			metadata: false,
			attachments: false
		},
		cellContentMetadata: {
			attachments: true
		}
	};
	context.subscriptions.push(vibecoda.workspace.registerNotebookSerializer('interactive', serializer, interactiveSerializeOptions));

	vibecoda.languages.registerCodeLensProvider({ pattern: '**/*.ipynb' }, {
		provideCodeLenses: (document) => {
			if (
				document.uri.scheme === 'vibecoda-notebook-cell' ||
				document.uri.scheme === 'vibecoda-notebook-cell-metadata' ||
				document.uri.scheme === 'vibecoda-notebook-cell-output'
			) {
				return [];
			}
			const codelens = new vibecoda.CodeLens(new vibecoda.Range(0, 0, 0, 0), { title: 'Open in Notebook Editor', command: 'ipynb.openIpynbInNotebookEditor', arguments: [document.uri] });
			return [codelens];
		}
	});

	context.subscriptions.push(vibecoda.commands.registerCommand('ipynb.newUntitledIpynb', async () => {
		const language = 'python';
		const cell = new vibecoda.NotebookCellData(vibecoda.NotebookCellKind.Code, '', language);
		const data = new vibecoda.NotebookData([cell]);
		data.metadata = {
			cells: [],
			metadata: {},
			nbformat: defaultNotebookFormat.major,
			nbformat_minor: defaultNotebookFormat.minor,
		};
		const doc = await vibecoda.workspace.openNotebookDocument('jupyter-notebook', data);
		await vibecoda.window.showNotebookDocument(doc);
	}));

	context.subscriptions.push(vibecoda.commands.registerCommand('ipynb.openIpynbInNotebookEditor', async (uri: vibecoda.Uri) => {
		if (vibecoda.window.activeTextEditor?.document.uri.toString() === uri.toString()) {
			await vibecoda.commands.executeCommand('workbench.action.closeActiveEditor');
		}
		const document = await vibecoda.workspace.openNotebookDocument(uri);
		await vibecoda.window.showNotebookDocument(document);
	}));

	context.subscriptions.push(notebookImagePasteSetup());

	const enabled = vibecoda.workspace.getConfiguration('ipynb').get('pasteImagesAsAttachments.enabled', false);
	if (enabled) {
		const cleaner = new AttachmentCleaner();
		context.subscriptions.push(cleaner);
	}

	return {
		get dropCustomMetadata() {
			return true;
		},
		exportNotebook: (notebook: vibecoda.NotebookData): Promise<string> => {
			return Promise.resolve(serializeNotebookToString(notebook));
		},
		setNotebookMetadata: async (resource: vibecoda.Uri, metadata: Partial<NotebookMetadata>): Promise<boolean> => {
			const document = vibecoda.workspace.notebookDocuments.find(doc => doc.uri.toString() === resource.toString());
			if (!document) {
				return false;
			}

			const edit = new vibecoda.WorkspaceEdit();
			edit.set(resource, [vibecoda.NotebookEdit.updateNotebookMetadata({
				...document.metadata,
				metadata: {
					...(document.metadata.metadata ?? {}),
					...metadata
				} satisfies NotebookMetadata,
			})]);
			return vibecoda.workspace.applyEdit(edit);
		},
	};
}

export function deactivate() { }
