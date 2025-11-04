/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import { illegalArgument } from '../../../base/common/errors.js';
import { MainThreadNotebookEditorsShape } from './extHost.protocol.js';
import * as extHostConverter from './extHostTypeConverters.js';
import * as extHostTypes from './extHostTypes.js';
import * as vibecoda from 'vibecoda';
import { ExtHostNotebookDocument } from './extHostNotebookDocument.js';
import { NotebookRange } from './extHostTypes.js';

export class ExtHostNotebookEditor {

	public static readonly apiEditorsToExtHost = new WeakMap<vibecoda.NotebookEditor, ExtHostNotebookEditor>();

	private _visible: boolean = false;

	private _editor?: vibecoda.NotebookEditor;

	constructor(
		readonly id: string,
		private readonly _proxy: MainThreadNotebookEditorsShape,
		readonly notebookData: ExtHostNotebookDocument,
		private _visibleRanges: vibecoda.NotebookRange[],
		private _selections: vibecoda.NotebookRange[],
		private _viewColumn: vibecoda.ViewColumn | undefined,
		private readonly viewType: string
	) { }

	get apiEditor(): vibecoda.NotebookEditor {
		if (!this._editor) {
			const that = this;
			this._editor = {
				get notebook() {
					return that.notebookData.apiNotebook;
				},
				get selection() {
					return that._selections[0];
				},
				set selection(selection: vibecoda.NotebookRange) {
					this.selections = [selection];
				},
				get selections() {
					return that._selections;
				},
				set selections(value: vibecoda.NotebookRange[]) {
					if (!Array.isArray(value) || !value.every(extHostTypes.NotebookRange.isNotebookRange)) {
						throw illegalArgument('selections');
					}
					that._selections = value.length === 0 ? [new NotebookRange(0, 0)] : value;
					that._trySetSelections(that._selections);
				},
				get visibleRanges() {
					return that._visibleRanges;
				},
				revealRange(range, revealType) {
					that._proxy.$tryRevealRange(
						that.id,
						extHostConverter.NotebookRange.from(range),
						revealType ?? extHostTypes.NotebookEditorRevealType.Default
					);
				},
				get viewColumn() {
					return that._viewColumn;
				},
				get replOptions() {
					if (that.viewType === 'repl') {
						return { appendIndex: this.notebook.cellCount - 1 };
					}
					return undefined;
				},
				[Symbol.for('debug.description')]() {
					return `NotebookEditor(${this.notebook.uri.toString()})`;
				}
			};

			ExtHostNotebookEditor.apiEditorsToExtHost.set(this._editor, this);
		}
		return this._editor;
	}

	get visible(): boolean {
		return this._visible;
	}

	_acceptVisibility(value: boolean) {
		this._visible = value;
	}

	_acceptVisibleRanges(value: vibecoda.NotebookRange[]): void {
		this._visibleRanges = value;
	}

	_acceptSelections(selections: vibecoda.NotebookRange[]): void {
		this._selections = selections;
	}

	private _trySetSelections(value: vibecoda.NotebookRange[]): void {
		this._proxy.$trySetSelections(this.id, value.map(extHostConverter.NotebookRange.from));
	}

	_acceptViewColumn(value: vibecoda.ViewColumn | undefined) {
		this._viewColumn = value;
	}
}
