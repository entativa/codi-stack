/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

/**
 * Helpers for converting FROM vibecoda types TO ts types
 */

import * as vibecoda from 'vibecoda';
import type * as Proto from './tsServer/protocol/protocol';
import * as PConst from './tsServer/protocol/protocol.const';
import { ITypeScriptServiceClient } from './typescriptService';

export namespace Range {
	export const fromTextSpan = (span: Proto.TextSpan): vibecoda.Range =>
		fromLocations(span.start, span.end);

	export const toTextSpan = (range: vibecoda.Range): Proto.TextSpan => ({
		start: Position.toLocation(range.start),
		end: Position.toLocation(range.end)
	});

	export const fromLocations = (start: Proto.Location, end: Proto.Location): vibecoda.Range =>
		new vibecoda.Range(
			Math.max(0, start.line - 1), Math.max(start.offset - 1, 0),
			Math.max(0, end.line - 1), Math.max(0, end.offset - 1));

	export const toFileRange = (range: vibecoda.Range): Proto.FileRange => ({
		startLine: range.start.line + 1,
		startOffset: range.start.character + 1,
		endLine: range.end.line + 1,
		endOffset: range.end.character + 1
	});

	export const toFileRangeRequestArgs = (file: string, range: vibecoda.Range): Proto.FileRangeRequestArgs => ({
		file,
		...toFileRange(range)
	});

	export const toFileRangesRequestArgs = (file: string, ranges: vibecoda.Range[]): Proto.FileRangesRequestArgs => ({
		file,
		ranges: ranges.map(toFileRange)
	});

	export const toFormattingRequestArgs = (file: string, range: vibecoda.Range): Proto.FormatRequestArgs => ({
		file,
		line: range.start.line + 1,
		offset: range.start.character + 1,
		endLine: range.end.line + 1,
		endOffset: range.end.character + 1
	});
}

export namespace Position {
	export const fromLocation = (tslocation: Proto.Location): vibecoda.Position =>
		new vibecoda.Position(tslocation.line - 1, tslocation.offset - 1);

	export const toLocation = (vsPosition: vibecoda.Position): Proto.Location => ({
		line: vsPosition.line + 1,
		offset: vsPosition.character + 1,
	});

	export const toFileLocationRequestArgs = (file: string, position: vibecoda.Position): Proto.FileLocationRequestArgs => ({
		file,
		line: position.line + 1,
		offset: position.character + 1,
	});
}

export namespace Location {
	export const fromTextSpan = (resource: vibecoda.Uri, tsTextSpan: Proto.TextSpan): vibecoda.Location =>
		new vibecoda.Location(resource, Range.fromTextSpan(tsTextSpan));
}

export namespace TextEdit {
	export const fromCodeEdit = (edit: Proto.CodeEdit): vibecoda.TextEdit =>
		new vibecoda.TextEdit(
			Range.fromTextSpan(edit),
			edit.newText);
}

export namespace WorkspaceEdit {
	export function fromFileCodeEdits(
		client: ITypeScriptServiceClient,
		edits: Iterable<Proto.FileCodeEdits>
	): vibecoda.WorkspaceEdit {
		return withFileCodeEdits(new vibecoda.WorkspaceEdit(), client, edits);
	}

	export function withFileCodeEdits(
		workspaceEdit: vibecoda.WorkspaceEdit,
		client: ITypeScriptServiceClient,
		edits: Iterable<Proto.FileCodeEdits>
	): vibecoda.WorkspaceEdit {
		for (const edit of edits) {
			const resource = client.toResource(edit.fileName);
			for (const textChange of edit.textChanges) {
				workspaceEdit.replace(resource,
					Range.fromTextSpan(textChange),
					textChange.newText);
			}
		}

		return workspaceEdit;
	}
}

export namespace SymbolKind {
	export function fromProtocolScriptElementKind(kind: Proto.ScriptElementKind) {
		switch (kind) {
			case PConst.Kind.module: return vibecoda.SymbolKind.Module;
			case PConst.Kind.class: return vibecoda.SymbolKind.Class;
			case PConst.Kind.enum: return vibecoda.SymbolKind.Enum;
			case PConst.Kind.enumMember: return vibecoda.SymbolKind.EnumMember;
			case PConst.Kind.interface: return vibecoda.SymbolKind.Interface;
			case PConst.Kind.indexSignature: return vibecoda.SymbolKind.Method;
			case PConst.Kind.callSignature: return vibecoda.SymbolKind.Method;
			case PConst.Kind.method: return vibecoda.SymbolKind.Method;
			case PConst.Kind.memberVariable: return vibecoda.SymbolKind.Property;
			case PConst.Kind.memberGetAccessor: return vibecoda.SymbolKind.Property;
			case PConst.Kind.memberSetAccessor: return vibecoda.SymbolKind.Property;
			case PConst.Kind.variable: return vibecoda.SymbolKind.Variable;
			case PConst.Kind.let: return vibecoda.SymbolKind.Variable;
			case PConst.Kind.const: return vibecoda.SymbolKind.Variable;
			case PConst.Kind.localVariable: return vibecoda.SymbolKind.Variable;
			case PConst.Kind.alias: return vibecoda.SymbolKind.Variable;
			case PConst.Kind.function: return vibecoda.SymbolKind.Function;
			case PConst.Kind.localFunction: return vibecoda.SymbolKind.Function;
			case PConst.Kind.constructSignature: return vibecoda.SymbolKind.Constructor;
			case PConst.Kind.constructorImplementation: return vibecoda.SymbolKind.Constructor;
			case PConst.Kind.typeParameter: return vibecoda.SymbolKind.TypeParameter;
			case PConst.Kind.string: return vibecoda.SymbolKind.String;
			default: return vibecoda.SymbolKind.Variable;
		}
	}
}

export namespace CompletionTriggerKind {
	export function toProtocolCompletionTriggerKind(kind: vibecoda.CompletionTriggerKind): Proto.CompletionTriggerKind {
		switch (kind) {
			case vibecoda.CompletionTriggerKind.Invoke: return 1;
			case vibecoda.CompletionTriggerKind.TriggerCharacter: return 2;
			case vibecoda.CompletionTriggerKind.TriggerForIncompleteCompletions: return 3;
		}
	}
}

export namespace OrganizeImportsMode {
	export function toProtocolOrganizeImportsMode(mode: PConst.OrganizeImportsMode): Proto.OrganizeImportsMode {
		switch (mode) {
			case PConst.OrganizeImportsMode.All: return 'All' as Proto.OrganizeImportsMode.All;
			case PConst.OrganizeImportsMode.SortAndCombine: return 'SortAndCombine' as Proto.OrganizeImportsMode.SortAndCombine;
			case PConst.OrganizeImportsMode.RemoveUnused: return 'RemoveUnused' as Proto.OrganizeImportsMode.RemoveUnused;
		}
	}
}
