/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as vibecoda from 'vibecoda';
import { assertNoRpc, assertNoRpcFromEntry, disposeAll } from '../utils';

suite('vibecoda', function () {

	const dispo: vibecoda.Disposable[] = [];

	teardown(() => {
		assertNoRpc();
		disposeAll(dispo);
	});

	test('no rpc', function () {
		assertNoRpc();
	});

	test('no rpc, createDiagnosticCollection()', function () {
		const item = vibecoda.languages.createDiagnosticCollection();
		dispo.push(item);
		assertNoRpcFromEntry([item, 'DiagnosticCollection']);
	});

	test('no rpc, createTextEditorDecorationType(...)', function () {
		const item = vibecoda.window.createTextEditorDecorationType({});
		dispo.push(item);
		assertNoRpcFromEntry([item, 'TextEditorDecorationType']);
	});

	test('no rpc, createOutputChannel(...)', function () {
		const item = vibecoda.window.createOutputChannel('hello');
		dispo.push(item);
		assertNoRpcFromEntry([item, 'OutputChannel']);
	});

	test('no rpc, createDiagnosticCollection(...)', function () {
		const item = vibecoda.languages.createDiagnosticCollection();
		dispo.push(item);
		assertNoRpcFromEntry([item, 'DiagnosticCollection']);
	});

	test('no rpc, createQuickPick(...)', function () {
		const item = vibecoda.window.createQuickPick();
		dispo.push(item);
		assertNoRpcFromEntry([item, 'QuickPick']);
	});

	test('no rpc, createInputBox(...)', function () {
		const item = vibecoda.window.createInputBox();
		dispo.push(item);
		assertNoRpcFromEntry([item, 'InputBox']);
	});

	test('no rpc, createStatusBarItem(...)', function () {
		const item = vibecoda.window.createStatusBarItem();
		dispo.push(item);
		assertNoRpcFromEntry([item, 'StatusBarItem']);
	});

	test('no rpc, createSourceControl(...)', function () {
		const item = vibecoda.scm.createSourceControl('foo', 'Hello');
		dispo.push(item);
		assertNoRpcFromEntry([item, 'SourceControl']);
	});

	test('no rpc, createCommentController(...)', function () {
		const item = vibecoda.comments.createCommentController('foo', 'Hello');
		dispo.push(item);
		assertNoRpcFromEntry([item, 'CommentController']);
	});

	test('no rpc, createWebviewPanel(...)', function () {
		const item = vibecoda.window.createWebviewPanel('webview', 'Hello', vibecoda.ViewColumn.Active);
		dispo.push(item);
		assertNoRpcFromEntry([item, 'WebviewPanel']);
	});

	test('no rpc, createTreeView(...)', function () {
		const treeDataProvider = new class implements vibecoda.TreeDataProvider<string> {
			getTreeItem(element: string): vibecoda.TreeItem | Thenable<vibecoda.TreeItem> {
				return new vibecoda.TreeItem(element);
			}
			getChildren(_element?: string): vibecoda.ProviderResult<string[]> {
				return ['foo', 'bar'];
			}
		};
		const item = vibecoda.window.createTreeView('test.treeId', { treeDataProvider });
		dispo.push(item);
		assertNoRpcFromEntry([item, 'TreeView']);
	});


	test('no rpc, createNotebookController(...)', function () {
		const ctrl = vibecoda.notebooks.createNotebookController('foo', 'bar', '');
		dispo.push(ctrl);
		assertNoRpcFromEntry([ctrl, 'NotebookController']);
	});

	test('no rpc, createTerminal(...)', function () {
		const ctrl = vibecoda.window.createTerminal({ name: 'termi' });
		dispo.push(ctrl);
		assertNoRpcFromEntry([ctrl, 'Terminal']);
	});

	test('no rpc, createFileSystemWatcher(...)', function () {
		const item = vibecoda.workspace.createFileSystemWatcher('**/*.ts');
		dispo.push(item);
		assertNoRpcFromEntry([item, 'FileSystemWatcher']);
	});

	test('no rpc, createTestController(...)', function () {
		const item = vibecoda.tests.createTestController('iii', 'lll');
		dispo.push(item);
		assertNoRpcFromEntry([item, 'TestController']);
	});

	test('no rpc, createLanguageStatusItem(...)', function () {
		const item = vibecoda.languages.createLanguageStatusItem('i', '*');
		dispo.push(item);
		assertNoRpcFromEntry([item, 'LanguageStatusItem']);
	});
});
