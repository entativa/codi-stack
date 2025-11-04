/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as assert from 'assert';
import 'mocha';
import * as vibecoda from 'vibecoda';
import { asPromise, disposeAll, poll } from '../utils';
import { Kernel, saveAllFilesAndCloseAll } from './notebook.api.test';

export type INativeInteractiveWindow = { notebookUri: vibecoda.Uri; inputUri: vibecoda.Uri; notebookEditor: vibecoda.NotebookEditor };

async function createInteractiveWindow(kernel: Kernel) {
	const { notebookEditor, inputUri } = (await vibecoda.commands.executeCommand(
		'interactive.open',
		// Keep focus on the owning file if there is one
		{ viewColumn: vibecoda.ViewColumn.Beside, preserveFocus: false },
		undefined,
		`vibecoda.vibecoda-api-tests/${kernel.controller.id}`,
		undefined
	)) as unknown as INativeInteractiveWindow;
	assert.ok(notebookEditor, 'Interactive Window was not created successfully');

	return { notebookEditor, inputUri };
}

async function addCell(code: string, notebook: vibecoda.NotebookDocument) {
	const cell = new vibecoda.NotebookCellData(vibecoda.NotebookCellKind.Code, code, 'typescript');
	const edit = vibecoda.NotebookEdit.insertCells(notebook.cellCount, [cell]);
	const workspaceEdit = new vibecoda.WorkspaceEdit();
	workspaceEdit.set(notebook.uri, [edit]);
	const event = asPromise(vibecoda.workspace.onDidChangeNotebookDocument);
	await vibecoda.workspace.applyEdit(workspaceEdit);
	await event;
	return notebook.cellAt(notebook.cellCount - 1);
}

async function addCellAndRun(code: string, notebook: vibecoda.NotebookDocument) {
	const initialCellCount = notebook.cellCount;
	const cell = await addCell(code, notebook);

	const event = asPromise(vibecoda.workspace.onDidChangeNotebookDocument);
	await vibecoda.commands.executeCommand('notebook.cell.execute', { start: initialCellCount, end: initialCellCount + 1 }, notebook.uri);
	try {
		await event;
	} catch (e) {
		const result = notebook.cellAt(notebook.cellCount - 1);
		assert.fail(`Notebook change event was not triggered after executing newly added cell. Initial Cell count: ${initialCellCount}. Current cell count: ${notebook.cellCount}. execution summary: ${JSON.stringify(result.executionSummary)}`);
	}
	assert.strictEqual(cell.outputs.length, 1, `Executed cell has no output. Initial Cell count: ${initialCellCount}. Current cell count: ${notebook.cellCount}. execution summary: ${JSON.stringify(cell.executionSummary)}`);
	return cell;
}


(vibecoda.env.uiKind === vibecoda.UIKind.Web ? suite.skip : suite)('Interactive Window', function () {

	const testDisposables: vibecoda.Disposable[] = [];
	let defaultKernel: Kernel;
	let secondKernel: Kernel;

	setup(async function () {
		defaultKernel = new Kernel('mainKernel', 'Notebook Default Kernel', 'interactive');
		secondKernel = new Kernel('secondKernel', 'Notebook Secondary Kernel', 'interactive');
		testDisposables.push(defaultKernel.controller);
		testDisposables.push(secondKernel.controller);
		await saveAllFilesAndCloseAll();
	});

	teardown(async function () {
		disposeAll(testDisposables);
		testDisposables.length = 0;
		await saveAllFilesAndCloseAll();
	});

	test.skip('Can open an interactive window and execute from input box', async () => {
		assert.ok(vibecoda.workspace.workspaceFolders);
		const { notebookEditor, inputUri } = await createInteractiveWindow(defaultKernel);

		const inputBox = vibecoda.window.visibleTextEditors.find(
			(e) => e.document.uri.path === inputUri.path
		);
		await inputBox!.edit((editBuilder) => {
			editBuilder.insert(new vibecoda.Position(0, 0), 'print foo');
		});
		await vibecoda.commands.executeCommand('interactive.execute', notebookEditor.notebook.uri);

		assert.strictEqual(notebookEditor.notebook.cellCount, 1);
		assert.strictEqual(notebookEditor.notebook.cellAt(0).kind, vibecoda.NotebookCellKind.Code);
	});

	test('Interactive window scrolls after execute', async () => {
		assert.ok(vibecoda.workspace.workspaceFolders);
		const { notebookEditor } = await createInteractiveWindow(defaultKernel);

		// Run and add a bunch of cells
		for (let i = 0; i < 10; i++) {
			await addCellAndRun(`print ${i}`, notebookEditor.notebook);
		}

		// Verify visible range has the last cell
		if (!lastCellIsVisible(notebookEditor)) {
			// scroll happens async, so give it some time to scroll
			await new Promise<void>((resolve) => setTimeout(() => {
				assert.ok(lastCellIsVisible(notebookEditor), `Last cell is not visible`);
				resolve();
			}, 1000));
		}
	});

	// https://github.com/microsoft/vibecoda/issues/266229
	test.skip('Interactive window has the correct kernel', async function () {
		// Extend timeout a bit as kernel association can be async & occasionally slow on CI
		this.timeout(20000);
		assert.ok(vibecoda.workspace.workspaceFolders);
		await createInteractiveWindow(defaultKernel);

		await vibecoda.commands.executeCommand('workbench.action.closeActiveEditor');

		// Create a new interactive window with a different kernel
		const { notebookEditor } = await createInteractiveWindow(secondKernel);
		assert.ok(notebookEditor);

		// Run a cell to ensure the kernel is actually exercised
		await addCellAndRun(`print`, notebookEditor.notebook);

		await poll(
			() => Promise.resolve(secondKernel.associatedNotebooks.has(notebookEditor.notebook.uri.toString())),
			v => v,
			'Secondary kernel was not set as the kernel for the interactive window'
		);
		assert.strictEqual(secondKernel.associatedNotebooks.has(notebookEditor.notebook.uri.toString()), true, `Secondary kernel was not set as the kernel for the interactive window`);
	});
});

function lastCellIsVisible(notebookEditor: vibecoda.NotebookEditor) {
	if (!notebookEditor.visibleRanges.length) {
		return false;
	}
	const lastVisibleCell = notebookEditor.visibleRanges[notebookEditor.visibleRanges.length - 1].end;
	return notebookEditor.notebook.cellCount === lastVisibleCell;
}
