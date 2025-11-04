/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as vibecoda from 'vibecoda';
import { SymbolItemEditorHighlights } from './references-view';

export class EditorHighlights<T> {

	private readonly _decorationType = vibecoda.window.createTextEditorDecorationType({
		backgroundColor: new vibecoda.ThemeColor('editor.findMatchHighlightBackground'),
		rangeBehavior: vibecoda.DecorationRangeBehavior.ClosedClosed,
		overviewRulerLane: vibecoda.OverviewRulerLane.Center,
		overviewRulerColor: new vibecoda.ThemeColor('editor.findMatchHighlightBackground'),
	});

	private readonly disposables: vibecoda.Disposable[] = [];
	private readonly _ignore = new Set<string>();

	constructor(private readonly _view: vibecoda.TreeView<T>, private readonly _delegate: SymbolItemEditorHighlights<T>) {
		this.disposables.push(
			vibecoda.workspace.onDidChangeTextDocument(e => this._ignore.add(e.document.uri.toString())),
			vibecoda.window.onDidChangeActiveTextEditor(() => _view.visible && this.update()),
			_view.onDidChangeVisibility(e => e.visible ? this._show() : this._hide()),
			_view.onDidChangeSelection(() => {
				if (_view.visible) {
					this.update();
				}
			})
		);
		this._show();
	}

	dispose() {
		vibecoda.Disposable.from(...this.disposables).dispose();
		for (const editor of vibecoda.window.visibleTextEditors) {
			editor.setDecorations(this._decorationType, []);
		}
	}

	private _show(): void {
		const { activeTextEditor: editor } = vibecoda.window;
		if (!editor || !editor.viewColumn) {
			return;
		}
		if (this._ignore.has(editor.document.uri.toString())) {
			return;
		}
		const [anchor] = this._view.selection;
		if (!anchor) {
			return;
		}
		const ranges = this._delegate.getEditorHighlights(anchor, editor.document.uri);
		if (ranges) {
			editor.setDecorations(this._decorationType, ranges);
		}
	}

	private _hide(): void {
		for (const editor of vibecoda.window.visibleTextEditors) {
			editor.setDecorations(this._decorationType, []);
		}
	}

	update(): void {
		this._hide();
		this._show();
	}
}
