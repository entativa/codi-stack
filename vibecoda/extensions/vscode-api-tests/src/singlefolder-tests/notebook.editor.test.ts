/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as assert from 'assert';
import * as vibecoda from 'vibecoda';
import * as utils from '../utils';

(vibecoda.env.uiKind === vibecoda.UIKind.Web ? suite.skip : suite.skip)('Notebook Editor', function () {

	const contentSerializer = new class implements vibecoda.NotebookSerializer {
		deserializeNotebook() {
			return new vibecoda.NotebookData(
				[new vibecoda.NotebookCellData(vibecoda.NotebookCellKind.Code, '// code cell', 'javascript')],
			);
		}
		serializeNotebook() {
			return new Uint8Array();
		}
	};

	const onDidOpenNotebookEditor = (timeout = vibecoda.env.uiKind === vibecoda.UIKind.Desktop ? 5000 : 15000) => {
		return new Promise<boolean>((resolve, reject) => {

			const handle = setTimeout(() => {
				sub.dispose();
				reject(new Error('onDidOpenNotebookEditor TIMEOUT reached'));
			}, timeout);

			const sub = vibecoda.window.onDidChangeActiveNotebookEditor(() => {
				if (vibecoda.window.activeNotebookEditor === undefined) {
					// skip if there is no active notebook editor (e.g. when opening a new notebook)
					return;
				}

				clearTimeout(handle);
				sub.dispose();
				resolve(true);
			});
		});
	};

	const disposables: vibecoda.Disposable[] = [];
	const testDisposables: vibecoda.Disposable[] = [];

	suiteTeardown(async function () {
		utils.assertNoRpc();
		await utils.revertAllDirty();
		await utils.closeAllEditors();
		utils.disposeAll(disposables);
		disposables.length = 0;

		for (const doc of vibecoda.workspace.notebookDocuments) {
			assert.strictEqual(doc.isDirty, false, doc.uri.toString());
		}
	});

	suiteSetup(function () {
		disposables.push(vibecoda.workspace.registerNotebookSerializer('notebook.nbdtest', contentSerializer));
	});

	teardown(async function () {
		utils.disposeAll(testDisposables);
		testDisposables.length = 0;
	});

	// #138683
	// TODO@rebornix https://github.com/microsoft/vibecoda/issues/170072
	test.skip('Opening a notebook should fire activeNotebook event changed only once', utils.withVerboseLogs(async function () {
		const openedEditor = onDidOpenNotebookEditor();
		const resource = await utils.createRandomFile(undefined, undefined, '.nbdtest');
		const document = await vibecoda.workspace.openNotebookDocument(resource);
		const editor = await vibecoda.window.showNotebookDocument(document);
		assert.ok(await openedEditor);
		assert.strictEqual(editor.notebook.uri.toString(), resource.toString());
	}));

	// TODO@rebornix https://github.com/microsoft/vibecoda/issues/173125
	test.skip('Active/Visible Editor', async function () {
		const firstEditorOpen = onDidOpenNotebookEditor();
		const resource = await utils.createRandomFile(undefined, undefined, '.nbdtest');
		const document = await vibecoda.workspace.openNotebookDocument(resource);

		const firstEditor = await vibecoda.window.showNotebookDocument(document);
		await firstEditorOpen;
		assert.strictEqual(vibecoda.window.activeNotebookEditor, firstEditor);
		assert.strictEqual(vibecoda.window.visibleNotebookEditors.includes(firstEditor), true);

		const secondEditor = await vibecoda.window.showNotebookDocument(document, { viewColumn: vibecoda.ViewColumn.Beside });
		// There is no guarantee that when `showNotebookDocument` resolves, the active notebook editor is already updated correctly.
		// assert.strictEqual(secondEditor === vibecoda.window.activeNotebookEditor, true);
		assert.notStrictEqual(firstEditor, secondEditor);
		assert.strictEqual(vibecoda.window.visibleNotebookEditors.includes(secondEditor), true);
		assert.strictEqual(vibecoda.window.visibleNotebookEditors.includes(firstEditor), true);
		assert.strictEqual(vibecoda.window.visibleNotebookEditors.length, 2);
		await utils.closeAllEditors();
	});

	test('Notebook Editor Event - onDidChangeVisibleNotebookEditors on open/close', async function () {
		const openedEditor = utils.asPromise(vibecoda.window.onDidChangeVisibleNotebookEditors);
		const resource = await utils.createRandomFile(undefined, undefined, '.nbdtest');
		const document = await vibecoda.workspace.openNotebookDocument(resource);
		await vibecoda.window.showNotebookDocument(document);
		assert.ok(await openedEditor);

		const firstEditorClose = utils.asPromise(vibecoda.window.onDidChangeVisibleNotebookEditors);
		await utils.closeAllEditors();
		await firstEditorClose;
	});

	test('Notebook Editor Event - onDidChangeVisibleNotebookEditors on two editor groups', async function () {
		const resource = await utils.createRandomFile(undefined, undefined, '.nbdtest');
		const document = await vibecoda.workspace.openNotebookDocument(resource);

		let count = 0;
		testDisposables.push(vibecoda.window.onDidChangeVisibleNotebookEditors(() => {
			count = vibecoda.window.visibleNotebookEditors.length;
		}));

		await vibecoda.window.showNotebookDocument(document, { viewColumn: vibecoda.ViewColumn.Active });
		assert.strictEqual(count, 1);

		await vibecoda.window.showNotebookDocument(document, { viewColumn: vibecoda.ViewColumn.Beside });
		assert.strictEqual(count, 2);

		await utils.closeAllEditors();
		assert.strictEqual(count, 0);
	});
});
