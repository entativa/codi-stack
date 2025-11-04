/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as assert from 'assert';
import 'mocha';
import * as vibecoda from 'vibecoda';
import { assertNoRpc, closeAllEditors, createRandomFile } from '../utils';

const ipynbContent = JSON.stringify({
	'cells': [
		{
			'cell_type': 'markdown',
			'source': ['## Header'],
			'metadata': {}
		},
		{
			'cell_type': 'code',
			'execution_count': 2,
			'source': [`print('hello 1')\n`, `print('hello 2')`],
			'outputs': [
				{
					'output_type': 'stream',
					'name': 'stdout',
					'text': ['hello 1\n', 'hello 2\n']
				}
			],
			'metadata': {}
		}
	]
});

suite('ipynb NotebookSerializer', function () {
	teardown(async function () {
		assertNoRpc();
		await closeAllEditors();
	});

	test.skip('Can open an ipynb notebook', async () => {
		const file = await createRandomFile(ipynbContent, undefined, '.ipynb');
		const notebook = await vibecoda.workspace.openNotebookDocument(file);
		await vibecoda.window.showNotebookDocument(notebook);

		const notebookEditor = vibecoda.window.activeNotebookEditor;
		assert.ok(notebookEditor);

		assert.strictEqual(notebookEditor.notebook.cellCount, 2);
		assert.strictEqual(notebookEditor.notebook.cellAt(0).kind, vibecoda.NotebookCellKind.Markup);
		assert.strictEqual(notebookEditor.notebook.cellAt(1).kind, vibecoda.NotebookCellKind.Code);
		assert.strictEqual(notebookEditor.notebook.cellAt(1).outputs.length, 1);
	});
});
