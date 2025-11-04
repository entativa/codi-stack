/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as assert from 'assert';
import 'mocha';
import { TextDecoder, TextEncoder } from 'util';
import * as vibecoda from 'vibecoda';
import { asPromise, assertNoRpc, closeAllEditors, createRandomFile, disposeAll, revertAllDirty, saveAllEditors } from '../utils';

async function createRandomNotebookFile() {
	return createRandomFile('', undefined, '.vsctestnb');
}

async function openRandomNotebookDocument() {
	const uri = await createRandomNotebookFile();
	return vibecoda.workspace.openNotebookDocument(uri);
}

async function openUntitledNotebookDocument(data?: vibecoda.NotebookData) {
	return vibecoda.workspace.openNotebookDocument('notebookCoreTest', data);
}

export async function saveAllFilesAndCloseAll() {
	await saveAllEditors();
	await closeAllEditors();
}


function sleep(ms: number): Promise<void> {
	return new Promise(resolve => {
		setTimeout(resolve, ms);
	});
}

const notebookType = 'notebookCoreTest';

export class Kernel {

	readonly controller: vibecoda.NotebookController;

	readonly associatedNotebooks = new Set<string>();

	constructor(id: string, label: string, viewType: string = notebookType) {
		this.controller = vibecoda.notebooks.createNotebookController(id, viewType, label);
		this.controller.executeHandler = this._execute.bind(this);
		this.controller.supportsExecutionOrder = true;
		this.controller.supportedLanguages = ['typescript', 'javascript'];
		this.controller.onDidChangeSelectedNotebooks(e => {
			if (e.selected) {
				this.associatedNotebooks.add(e.notebook.uri.toString());
			} else {
				this.associatedNotebooks.delete(e.notebook.uri.toString());
			}
		});
	}

	protected async _execute(cells: vibecoda.NotebookCell[]): Promise<void> {
		for (const cell of cells) {
			await this._runCell(cell);
		}
	}

	protected async _runCell(cell: vibecoda.NotebookCell) {
		// create a single output with exec order 1 and output is plain/text
		// of either the cell itself or (iff empty) the cell's document's uri
		const task = this.controller.createNotebookCellExecution(cell);
		task.start(Date.now());
		task.executionOrder = 1;
		await sleep(10); // Force to be take some time
		await task.replaceOutput([new vibecoda.NotebookCellOutput([
			vibecoda.NotebookCellOutputItem.text(cell.document.getText() || cell.document.uri.toString(), 'text/plain')
		])]);
		task.end(true);
	}
}


function getFocusedCell(editor?: vibecoda.NotebookEditor) {
	return editor ? editor.notebook.cellAt(editor.selections[0].start) : undefined;
}

const apiTestSerializer: vibecoda.NotebookSerializer = {
	serializeNotebook(_data, _token) {
		return new Uint8Array();
	},
	deserializeNotebook(_content, _token) {
		const dto: vibecoda.NotebookData = {
			metadata: { testMetadata: false },
			cells: [
				{
					value: 'test',
					languageId: 'typescript',
					kind: vibecoda.NotebookCellKind.Code,
					outputs: [],
					metadata: { testCellMetadata: 123 },
					executionSummary: { timing: { startTime: 10, endTime: 20 } }
				},
				{
					value: 'test2',
					languageId: 'typescript',
					kind: vibecoda.NotebookCellKind.Code,
					outputs: [
						new vibecoda.NotebookCellOutput([
							vibecoda.NotebookCellOutputItem.text('Hello World', 'text/plain')
						],
							{
								testOutputMetadata: true,
								['text/plain']: { testOutputItemMetadata: true }
							})
					],
					executionSummary: { executionOrder: 5, success: true },
					metadata: { testCellMetadata: 456 }
				}
			]
		};
		return dto;
	},
};

(vibecoda.env.uiKind === vibecoda.UIKind.Web ? suite.skip : suite)('Notebook API tests', function () {

	const testDisposables: vibecoda.Disposable[] = [];
	const suiteDisposables: vibecoda.Disposable[] = [];

	suiteTeardown(async function () {

		assertNoRpc();

		await revertAllDirty();
		await closeAllEditors();

		disposeAll(suiteDisposables);
		suiteDisposables.length = 0;
	});

	suiteSetup(function () {
		suiteDisposables.push(vibecoda.workspace.registerNotebookSerializer(notebookType, apiTestSerializer));
	});

	let defaultKernel: Kernel;

	setup(async function () {
		// there should be ONE default kernel in this suite
		defaultKernel = new Kernel('mainKernel', 'Notebook Default Kernel');
		testDisposables.push(defaultKernel.controller);
		await saveAllFilesAndCloseAll();
	});

	teardown(async function () {
		disposeAll(testDisposables);
		testDisposables.length = 0;
		await revertAllDirty();
		await saveAllFilesAndCloseAll();
	});

	test('notebook open', async function () {
		const notebook = await openRandomNotebookDocument();
		const editor = await vibecoda.window.showNotebookDocument(notebook);
		assert.strictEqual(getFocusedCell(editor)?.document.getText(), 'test');
		assert.strictEqual(getFocusedCell(editor)?.document.languageId, 'typescript');

		const secondCell = editor.notebook.cellAt(1);
		assert.strictEqual(secondCell.outputs.length, 1);
		assert.deepStrictEqual(secondCell.outputs[0].metadata, { testOutputMetadata: true, ['text/plain']: { testOutputItemMetadata: true } });
		assert.strictEqual(secondCell.outputs[0].items.length, 1);
		assert.strictEqual(secondCell.outputs[0].items[0].mime, 'text/plain');
		assert.strictEqual(new TextDecoder().decode(secondCell.outputs[0].items[0].data), 'Hello World');
		assert.strictEqual(secondCell.executionSummary?.executionOrder, 5);
		assert.strictEqual(secondCell.executionSummary?.success, true);
	});

	test('multiple tabs: different editors with same document', async function () {
		const notebook = await openRandomNotebookDocument();
		const firstNotebookEditor = await vibecoda.window.showNotebookDocument(notebook, { viewColumn: vibecoda.ViewColumn.One });
		const secondNotebookEditor = await vibecoda.window.showNotebookDocument(notebook, { viewColumn: vibecoda.ViewColumn.Beside });
		assert.notStrictEqual(firstNotebookEditor, secondNotebookEditor);
		assert.strictEqual(firstNotebookEditor?.notebook, secondNotebookEditor?.notebook, 'split notebook editors share the same document');
	});

	test('#106657. Opening a notebook from markers view is broken ', async function () {

		const document = await openRandomNotebookDocument();
		const [cell] = document.getCells();

		assert.strictEqual(vibecoda.window.activeNotebookEditor, undefined);

		// opening a cell-uri opens a notebook editor
		await vibecoda.window.showTextDocument(cell.document, { viewColumn: vibecoda.ViewColumn.Active });

		assert.strictEqual(!!vibecoda.window.activeNotebookEditor, true);
		assert.strictEqual(vibecoda.window.activeNotebookEditor!.notebook.uri.toString(), document.uri.toString());
	});

	test('Opening an utitled notebook without content will only open the editor when shown.', async function () {
		const document = await openUntitledNotebookDocument();

		assert.strictEqual(vibecoda.window.activeNotebookEditor, undefined);

		// opening a cell-uri opens a notebook editor
		await vibecoda.window.showNotebookDocument(document);

		assert.strictEqual(!!vibecoda.window.activeNotebookEditor, true);
		assert.strictEqual(vibecoda.window.activeNotebookEditor!.notebook.uri.toString(), document.uri.toString());
	});

	test('Opening an untitled notebook with content will open a dirty document.', async function () {
		const language = 'python';
		const cell = new vibecoda.NotebookCellData(vibecoda.NotebookCellKind.Code, '', language);
		const data = new vibecoda.NotebookData([cell]);
		const doc = await vibecoda.workspace.openNotebookDocument('jupyter-notebook', data);

		assert.strictEqual(doc.isDirty, true);
	});

	test('Cannot open notebook from cell-uri with vibecoda.open-command', async function () {

		const document = await openRandomNotebookDocument();
		const [cell] = document.getCells();

		await saveAllFilesAndCloseAll();
		assert.strictEqual(vibecoda.window.activeNotebookEditor, undefined);

		// BUG is that the editor opener (https://github.com/microsoft/vibecoda/blob/8e7877bdc442f1e83a7fec51920d82b696139129/src/vs/editor/browser/services/openerService.ts#L69)
		// removes the fragment if it matches something numeric. For notebooks that's not wanted...
		// opening a cell-uri opens a notebook editor
		await vibecoda.commands.executeCommand('vibecoda.open', cell.document.uri);

		assert.strictEqual(vibecoda.window.activeNotebookEditor!.notebook.uri.toString(), document.uri.toString());
	});

	test('#97830, #97764. Support switch to other editor types', async function () {
		const notebook = await openRandomNotebookDocument();
		const editor = await vibecoda.window.showNotebookDocument(notebook);
		const edit = new vibecoda.WorkspaceEdit();
		const focusedCell = getFocusedCell(editor);
		assert.ok(focusedCell);
		edit.replace(focusedCell.document.uri, focusedCell.document.lineAt(0).range, 'var abc = 0;');
		await vibecoda.workspace.applyEdit(edit);

		assert.strictEqual(getFocusedCell(editor)?.document.getText(), 'var abc = 0;');

		// no kernel -> no default language
		assert.strictEqual(getFocusedCell(editor)?.document.languageId, 'typescript');

		await vibecoda.window.showNotebookDocument(await vibecoda.workspace.openNotebookDocument(notebook.uri));
		assert.strictEqual(vibecoda.window.activeTextEditor?.document.uri.path, notebook.uri.path);
	});

	test('#102411 - untitled notebook creation failed', async function () {
		const document = await vibecoda.workspace.openNotebookDocument(notebookType, undefined);
		await vibecoda.window.showNotebookDocument(document);
		assert.notStrictEqual(vibecoda.window.activeNotebookEditor, undefined, 'untitled notebook editor is not undefined');

		await closeAllEditors();
	});

	test('#207742 - New Untitled notebook failed if previous untilted notebook is modified', async function () {
		await vibecoda.commands.executeCommand('ipynb.newUntitledIpynb');
		assert.notStrictEqual(vibecoda.window.activeNotebookEditor, undefined, 'untitled notebook editor is not undefined');
		const document = vibecoda.window.activeNotebookEditor!.notebook;

		// open another text editor
		const textDocument = await vibecoda.workspace.openTextDocument({ language: 'javascript', content: 'let abc = 0;' });
		await vibecoda.window.showTextDocument(textDocument);

		// insert a new cell to notebook document
		const edit = new vibecoda.WorkspaceEdit();
		const notebookEdit = new vibecoda.NotebookEdit(new vibecoda.NotebookRange(1, 1), [new vibecoda.NotebookCellData(vibecoda.NotebookCellKind.Code, 'print(1)', 'python')]);
		edit.set(document.uri, [notebookEdit]);
		await vibecoda.workspace.applyEdit(edit);

		// switch to the notebook editor
		await vibecoda.window.showNotebookDocument(document);
		await closeAllEditors();
		await vibecoda.commands.executeCommand('ipynb.newUntitledIpynb');
		assert.notStrictEqual(vibecoda.window.activeNotebookEditor, undefined, 'untitled notebook editor is not undefined');

		await closeAllEditors();
	});

	// TODO: Skipped due to notebook content provider removal
	test.skip('#115855 onDidSaveNotebookDocument', async function () {
		const resource = await createRandomNotebookFile();
		const notebook = await vibecoda.workspace.openNotebookDocument(resource);

		const notebookEdit = new vibecoda.NotebookEdit(new vibecoda.NotebookRange(1, 1), [new vibecoda.NotebookCellData(vibecoda.NotebookCellKind.Code, 'test 2', 'javascript')]);
		const edit = new vibecoda.WorkspaceEdit();
		edit.set(notebook.uri, [notebookEdit]);
		await vibecoda.workspace.applyEdit(edit);
		assert.strictEqual(notebook.isDirty, true);

		const saveEvent = asPromise(vibecoda.workspace.onDidSaveNotebookDocument);
		await notebook.save();
		await saveEvent;

		assert.strictEqual(notebook.isDirty, false);
	});
});

suite('Notebook & LiveShare', function () {

	const suiteDisposables: vibecoda.Disposable[] = [];
	const notebookType = 'vsls-testing';

	suiteTeardown(() => {
		vibecoda.Disposable.from(...suiteDisposables).dispose();
	});

	suiteSetup(function () {

		suiteDisposables.push(vibecoda.workspace.registerNotebookSerializer(notebookType, new class implements vibecoda.NotebookSerializer {
			deserializeNotebook(content: Uint8Array, _token: vibecoda.CancellationToken): vibecoda.NotebookData | Thenable<vibecoda.NotebookData> {
				const value = new TextDecoder().decode(content);
				const cell1 = new vibecoda.NotebookCellData(vibecoda.NotebookCellKind.Code, value, 'fooLang');
				cell1.outputs = [new vibecoda.NotebookCellOutput([vibecoda.NotebookCellOutputItem.stderr(value)])];
				return new vibecoda.NotebookData([cell1]);
			}
			serializeNotebook(data: vibecoda.NotebookData, _token: vibecoda.CancellationToken): Uint8Array | Thenable<Uint8Array> {
				return new TextEncoder().encode(data.cells[0].value);
			}
		}, {}, {
			displayName: 'LS',
			filenamePattern: ['*'],
		}));
	});

	test('command: vibecoda.resolveNotebookContentProviders', async function () {

		type Info = { viewType: string; displayName: string; filenamePattern: string[] };

		const info = await vibecoda.commands.executeCommand<Info[]>('vibecoda.resolveNotebookContentProviders');
		assert.strictEqual(Array.isArray(info), true);

		const item = info.find(item => item.viewType === notebookType);
		assert.ok(item);
		assert.strictEqual(item?.viewType, notebookType);
	});

	test('command: vibecoda.executeDataToNotebook', async function () {
		const value = 'dataToNotebook';
		const data = await vibecoda.commands.executeCommand<vibecoda.NotebookData>('vibecoda.executeDataToNotebook', notebookType, new TextEncoder().encode(value));
		assert.ok(data instanceof vibecoda.NotebookData);
		assert.strictEqual(data.cells.length, 1);
		assert.strictEqual(data.cells[0].value, value);
		assert.strictEqual(new TextDecoder().decode(data.cells[0].outputs![0].items[0].data), value);
	});

	test('command: vibecoda.executeNotebookToData', async function () {
		const value = 'notebookToData';
		const notebook = new vibecoda.NotebookData([new vibecoda.NotebookCellData(vibecoda.NotebookCellKind.Code, value, 'fooLang')]);
		const data = await vibecoda.commands.executeCommand<Uint8Array>('vibecoda.executeNotebookToData', notebookType, notebook);
		assert.ok(data instanceof Uint8Array);
		assert.deepStrictEqual(new TextDecoder().decode(data), value);
	});
});
