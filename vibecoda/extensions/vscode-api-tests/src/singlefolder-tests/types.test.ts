/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as assert from 'assert';
import 'mocha';
import * as vibecoda from 'vibecoda';
import { assertNoRpc } from '../utils';

suite('vibecoda API - types', () => {

	teardown(assertNoRpc);

	test('static properties, es5 compat class', function () {
		assert.ok(vibecoda.ThemeIcon.File instanceof vibecoda.ThemeIcon);
		assert.ok(vibecoda.ThemeIcon.Folder instanceof vibecoda.ThemeIcon);
		assert.ok(vibecoda.CodeActionKind.Empty instanceof vibecoda.CodeActionKind);
		assert.ok(vibecoda.CodeActionKind.QuickFix instanceof vibecoda.CodeActionKind);
		assert.ok(vibecoda.CodeActionKind.Refactor instanceof vibecoda.CodeActionKind);
		assert.ok(vibecoda.CodeActionKind.RefactorExtract instanceof vibecoda.CodeActionKind);
		assert.ok(vibecoda.CodeActionKind.RefactorInline instanceof vibecoda.CodeActionKind);
		assert.ok(vibecoda.CodeActionKind.RefactorMove instanceof vibecoda.CodeActionKind);
		assert.ok(vibecoda.CodeActionKind.RefactorRewrite instanceof vibecoda.CodeActionKind);
		assert.ok(vibecoda.CodeActionKind.Source instanceof vibecoda.CodeActionKind);
		assert.ok(vibecoda.CodeActionKind.SourceOrganizeImports instanceof vibecoda.CodeActionKind);
		assert.ok(vibecoda.CodeActionKind.SourceFixAll instanceof vibecoda.CodeActionKind);
		// assert.ok(vibecoda.QuickInputButtons.Back instanceof vibecoda.QuickInputButtons); never was an instance

	});
});
