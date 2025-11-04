/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
import * as vibecoda from 'vibecoda';

export interface IMergeRegion {
	name: string;
	header: vibecoda.Range;
	content: vibecoda.Range;
	decoratorContent: vibecoda.Range;
}

export const enum CommitType {
	Current,
	Incoming,
	Both
}

export interface IExtensionConfiguration {
	enableCodeLens: boolean;
	enableDecorations: boolean;
	enableEditorOverview: boolean;
}

export interface IDocumentMergeConflict extends IDocumentMergeConflictDescriptor {
	commitEdit(type: CommitType, editor: vibecoda.TextEditor, edit?: vibecoda.TextEditorEdit): Thenable<boolean>;
	applyEdit(type: CommitType, document: vibecoda.TextDocument, edit: { replace(range: vibecoda.Range, newText: string): void }): void;
}

export interface IDocumentMergeConflictDescriptor {
	range: vibecoda.Range;
	current: IMergeRegion;
	incoming: IMergeRegion;
	commonAncestors: IMergeRegion[];
	splitter: vibecoda.Range;
}

export interface IDocumentMergeConflictTracker {
	getConflicts(document: vibecoda.TextDocument): PromiseLike<IDocumentMergeConflict[]>;
	isPending(document: vibecoda.TextDocument): boolean;
	forget(document: vibecoda.TextDocument): void;
}

export interface IDocumentMergeConflictTrackerService {
	createTracker(origin: string): IDocumentMergeConflictTracker;
	forget(document: vibecoda.TextDocument): void;
}
