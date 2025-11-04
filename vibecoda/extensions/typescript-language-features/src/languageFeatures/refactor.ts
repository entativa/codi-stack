/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as path from 'path';
import * as vibecoda from 'vibecoda';
import { Utils } from 'vibecoda-uri';
import { Command, CommandManager } from '../commands/commandManager';
import { LearnMoreAboutRefactoringsCommand } from '../commands/learnMoreAboutRefactorings';
import { DocumentSelector } from '../configuration/documentSelector';
import * as fileSchemes from '../configuration/fileSchemes';
import { Schemes } from '../configuration/schemes';
import { TelemetryReporter } from '../logging/telemetry';
import { API } from '../tsServer/api';
import { CachedResponse } from '../tsServer/cachedResponse';
import type * as Proto from '../tsServer/protocol/protocol';
import * as PConst from '../tsServer/protocol/protocol.const';
import * as typeConverters from '../typeConverters';
import { ClientCapability, ITypeScriptServiceClient } from '../typescriptService';
import { coalesce } from '../utils/arrays';
import { nulToken } from '../utils/cancellation';
import FormattingOptionsManager from './fileConfigurationManager';
import { CompositeCommand, EditorChatFollowUp } from './util/copilot';
import { conditionalRegistration, requireSomeCapability } from './util/dependentRegistration';

function toWorkspaceEdit(client: ITypeScriptServiceClient, edits: readonly Proto.FileCodeEdits[]): vibecoda.WorkspaceEdit {
	const workspaceEdit = new vibecoda.WorkspaceEdit();
	for (const edit of edits) {
		const resource = client.toResource(edit.fileName);
		if (resource.scheme === fileSchemes.file) {
			workspaceEdit.createFile(resource, { ignoreIfExists: true });
		}
	}
	typeConverters.WorkspaceEdit.withFileCodeEdits(workspaceEdit, client, edits);
	return workspaceEdit;
}


namespace DidApplyRefactoringCommand {
	export interface Args {
		readonly action: string;
		readonly trigger: vibecoda.CodeActionTriggerKind;
	}
}

class DidApplyRefactoringCommand implements Command {
	public static readonly ID = '_typescript.didApplyRefactoring';
	public readonly id = DidApplyRefactoringCommand.ID;

	constructor(
		private readonly telemetryReporter: TelemetryReporter
	) { }

	public async execute(args: DidApplyRefactoringCommand.Args): Promise<void> {
		/* __GDPR__
			"refactor.execute" : {
				"owner": "mjbvz",
				"action" : { "classification": "PublicNonPersonalData", "purpose": "FeatureInsight" },
				"trigger" : { "classification": "PublicNonPersonalData", "purpose": "FeatureInsight" },
				"${include}": [
					"${TypeScriptCommonProperties}"
				]
			}
		*/
		this.telemetryReporter.logTelemetry('refactor.execute', {
			action: args.action,
			trigger: args.trigger,
		});
	}
}
namespace SelectRefactorCommand {
	export interface Args {
		readonly document: vibecoda.TextDocument;
		readonly refactor: Proto.ApplicableRefactorInfo;
		readonly rangeOrSelection: vibecoda.Range | vibecoda.Selection;
		readonly trigger: vibecoda.CodeActionTriggerKind;
	}
}

class SelectRefactorCommand implements Command {
	public static readonly ID = '_typescript.selectRefactoring';
	public readonly id = SelectRefactorCommand.ID;

	constructor(
		private readonly client: ITypeScriptServiceClient,
	) { }

	public async execute(args: SelectRefactorCommand.Args): Promise<void> {
		const file = this.client.toOpenTsFilePath(args.document);
		if (!file) {
			return;
		}

		const selected = await vibecoda.window.showQuickPick(args.refactor.actions.map((action): vibecoda.QuickPickItem & { action: Proto.RefactorActionInfo } => ({
			action,
			label: action.name,
			description: action.description,
		})));
		if (!selected) {
			return;
		}

		const tsAction = new InlinedCodeAction(this.client, args.document, args.refactor, selected.action, args.rangeOrSelection, args.trigger);
		await tsAction.resolve(nulToken);

		if (tsAction.edit) {
			if (!(await vibecoda.workspace.applyEdit(tsAction.edit, { isRefactoring: true }))) {
				vibecoda.window.showErrorMessage(vibecoda.l10n.t("Could not apply refactoring"));
				return;
			}
		}

		if (tsAction.command) {
			await vibecoda.commands.executeCommand(tsAction.command.command, ...(tsAction.command.arguments ?? []));
		}
	}
}

namespace MoveToFileRefactorCommand {
	export interface Args {
		readonly document: vibecoda.TextDocument;
		readonly action: Proto.RefactorActionInfo;
		readonly range: vibecoda.Range;
		readonly trigger: vibecoda.CodeActionTriggerKind;
	}
}

class MoveToFileRefactorCommand implements Command {
	public static readonly ID = '_typescript.moveToFileRefactoring';
	public readonly id = MoveToFileRefactorCommand.ID;

	constructor(
		private readonly client: ITypeScriptServiceClient,
		private readonly didApplyCommand: DidApplyRefactoringCommand
	) { }

	public async execute(args: MoveToFileRefactorCommand.Args): Promise<void> {
		const file = this.client.toOpenTsFilePath(args.document);
		if (!file) {
			return;
		}

		const targetFile = await this.getTargetFile(args.document, file, args.range);
		if (!targetFile || targetFile.toString() === file.toString()) {
			return;
		}

		const fileSuggestionArgs: Proto.GetEditsForRefactorRequestArgs = {
			...typeConverters.Range.toFileRangeRequestArgs(file, args.range),
			action: 'Move to file',
			refactor: 'Move to file',
			interactiveRefactorArguments: { targetFile },
		};

		const response = await this.client.execute('getEditsForRefactor', fileSuggestionArgs, nulToken);
		if (response.type !== 'response' || !response.body) {
			return;
		}
		const edit = toWorkspaceEdit(this.client, response.body.edits);
		if (!(await vibecoda.workspace.applyEdit(edit, { isRefactoring: true }))) {
			vibecoda.window.showErrorMessage(vibecoda.l10n.t("Could not apply refactoring"));
			return;
		}

		await this.didApplyCommand.execute({ action: args.action.name, trigger: args.trigger });
	}

	private async getTargetFile(document: vibecoda.TextDocument, file: string, range: vibecoda.Range): Promise<string | undefined> {
		const args = typeConverters.Range.toFileRangeRequestArgs(file, range);
		const response = await this.client.execute('getMoveToRefactoringFileSuggestions', args, nulToken);
		if (response.type !== 'response' || !response.body) {
			return;
		}
		const body = response.body;

		type DestinationItem = vibecoda.QuickPickItem & { readonly file?: string };
		const selectExistingFileItem: vibecoda.QuickPickItem = { label: vibecoda.l10n.t("Select existing file...") };
		const selectNewFileItem: vibecoda.QuickPickItem = { label: vibecoda.l10n.t("Enter new file path...") };

		const workspaceFolder = vibecoda.workspace.getWorkspaceFolder(document.uri);
		const quickPick = vibecoda.window.createQuickPick<DestinationItem>();
		quickPick.ignoreFocusOut = true;

		// true so we don't skip computing in the first call
		let quickPickInRelativeMode = true;
		const updateItems = () => {
			const relativeQuery = ['./', '../'].find(str => quickPick.value.startsWith(str));
			if (quickPickInRelativeMode === false && !!relativeQuery === false) {
				return;
			}
			quickPickInRelativeMode = !!relativeQuery;
			const destinationItems = body.files.map((file): DestinationItem | undefined => {
				const uri = this.client.toResource(file);
				const parentDir = Utils.dirname(uri);
				const filename = Utils.basename(uri);

				let description: string | undefined;
				if (workspaceFolder) {
					if (uri.scheme === Schemes.file) {
						description = path.relative(workspaceFolder.uri.fsPath, parentDir.fsPath);
					} else {
						description = path.posix.relative(workspaceFolder.uri.path, parentDir.path);
					}
					if (relativeQuery) {
						const convertRelativePath = (str: string) => {
							return !str.startsWith('../') ? `./${str}` : str;
						};

						const relativePath = convertRelativePath(path.relative(path.dirname(document.uri.fsPath), uri.fsPath));
						if (!relativePath.startsWith(relativeQuery)) {
							return;
						}
						description = relativePath;
					}
				} else {
					description = parentDir.fsPath;
				}

				return {
					file,
					label: Utils.basename(uri),
					description: relativeQuery ? description : path.join(description, filename),
				};
			});
			quickPick.items = [
				selectExistingFileItem,
				selectNewFileItem,
				{ label: vibecoda.l10n.t("destination files"), kind: vibecoda.QuickPickItemKind.Separator },
				...coalesce(destinationItems)
			];
		};
		quickPick.title = vibecoda.l10n.t("Move to File");
		quickPick.placeholder = vibecoda.l10n.t("Enter file path");
		quickPick.matchOnDescription = true;
		quickPick.onDidChangeValue(updateItems);
		updateItems();

		const picked = await new Promise<DestinationItem | undefined>(resolve => {
			quickPick.onDidAccept(() => {
				resolve(quickPick.selectedItems[0]);
				quickPick.dispose();
			});
			quickPick.onDidHide(() => {
				resolve(undefined);
				quickPick.dispose();
			});
			quickPick.show();
		});
		if (!picked) {
			return;
		}

		if (picked === selectExistingFileItem) {
			const picked = await vibecoda.window.showOpenDialog({
				title: vibecoda.l10n.t("Select move destination"),
				openLabel: vibecoda.l10n.t("Move to File"),
				defaultUri: Utils.dirname(document.uri),
			});
			return picked?.length ? this.client.toTsFilePath(picked[0]) : undefined;
		} else if (picked === selectNewFileItem) {
			const picked = await vibecoda.window.showSaveDialog({
				title: vibecoda.l10n.t("Select move destination"),
				saveLabel: vibecoda.l10n.t("Move to File"),
				defaultUri: this.client.toResource(response.body.newFileName),
			});
			return picked ? this.client.toTsFilePath(picked) : undefined;
		} else {
			return picked.file;
		}
	}
}

interface CodeActionKind {
	readonly kind: vibecoda.CodeActionKind;
	matches(refactor: Proto.RefactorActionInfo): boolean;
}

const Extract_Function = Object.freeze<CodeActionKind>({
	kind: vibecoda.CodeActionKind.RefactorExtract.append('function'),
	matches: refactor => refactor.name.startsWith('function_')
});

const Extract_Constant = Object.freeze<CodeActionKind>({
	kind: vibecoda.CodeActionKind.RefactorExtract.append('constant'),
	matches: refactor => refactor.name.startsWith('constant_')
});

const Extract_Type = Object.freeze<CodeActionKind>({
	kind: vibecoda.CodeActionKind.RefactorExtract.append('type'),
	matches: refactor => refactor.name.startsWith('Extract to type alias')
});

const Extract_Interface = Object.freeze<CodeActionKind>({
	kind: vibecoda.CodeActionKind.RefactorExtract.append('interface'),
	matches: refactor => refactor.name.startsWith('Extract to interface')
});

const Move_File = Object.freeze<CodeActionKind>({
	kind: vibecoda.CodeActionKind.RefactorMove.append('file'),
	matches: refactor => refactor.name.startsWith('Move to file')
});

const Move_NewFile = Object.freeze<CodeActionKind>({
	kind: vibecoda.CodeActionKind.RefactorMove.append('newFile'),
	matches: refactor => refactor.name.startsWith('Move to a new file')
});

const Rewrite_Import = Object.freeze<CodeActionKind>({
	kind: vibecoda.CodeActionKind.RefactorRewrite.append('import'),
	matches: refactor => refactor.name.startsWith('Convert namespace import') || refactor.name.startsWith('Convert named imports')
});

const Rewrite_Export = Object.freeze<CodeActionKind>({
	kind: vibecoda.CodeActionKind.RefactorRewrite.append('export'),
	matches: refactor => refactor.name.startsWith('Convert default export') || refactor.name.startsWith('Convert named export')
});

const Rewrite_Arrow_Braces = Object.freeze<CodeActionKind>({
	kind: vibecoda.CodeActionKind.RefactorRewrite.append('arrow').append('braces'),
	matches: refactor => refactor.name.startsWith('Convert default export') || refactor.name.startsWith('Convert named export')
});

const Rewrite_Parameters_ToDestructured = Object.freeze<CodeActionKind>({
	kind: vibecoda.CodeActionKind.RefactorRewrite.append('parameters').append('toDestructured'),
	matches: refactor => refactor.name.startsWith('Convert parameters to destructured object')
});

const Rewrite_Property_GenerateAccessors = Object.freeze<CodeActionKind>({
	kind: vibecoda.CodeActionKind.RefactorRewrite.append('property').append('generateAccessors'),
	matches: refactor => refactor.name.startsWith('Generate \'get\' and \'set\' accessors')
});

const allKnownCodeActionKinds = [
	Extract_Function,
	Extract_Constant,
	Extract_Type,
	Extract_Interface,
	Move_File,
	Move_NewFile,
	Rewrite_Import,
	Rewrite_Export,
	Rewrite_Arrow_Braces,
	Rewrite_Parameters_ToDestructured,
	Rewrite_Property_GenerateAccessors
];

class InlinedCodeAction extends vibecoda.CodeAction {
	constructor(
		public readonly client: ITypeScriptServiceClient,
		public readonly document: vibecoda.TextDocument,
		public readonly refactor: Proto.ApplicableRefactorInfo,
		public readonly action: Proto.RefactorActionInfo,
		public readonly range: vibecoda.Range,
		trigger: vibecoda.CodeActionTriggerKind,
	) {
		const title = action.description;
		super(title, InlinedCodeAction.getKind(action));

		if (action.notApplicableReason) {
			this.disabled = { reason: action.notApplicableReason };
		}

		this.command = {
			title,
			command: DidApplyRefactoringCommand.ID,
			arguments: [{ action: action.name, trigger } satisfies DidApplyRefactoringCommand.Args],
		};
	}

	public async resolve(token: vibecoda.CancellationToken): Promise<undefined> {
		const file = this.client.toOpenTsFilePath(this.document);
		if (!file) {
			return;
		}

		const args: Proto.GetEditsForRefactorRequestArgs = {
			...typeConverters.Range.toFileRangeRequestArgs(file, this.range),
			refactor: this.refactor.name,
			action: this.action.name,
		};

		const response = await this.client.execute('getEditsForRefactor', args, token);
		if (response.type !== 'response' || !response.body) {
			return;
		}

		this.edit = toWorkspaceEdit(this.client, response.body.edits);
		if (!this.edit.size) {
			vibecoda.window.showErrorMessage(vibecoda.l10n.t("Could not apply refactoring"));
			return;
		}

		if (response.body.renameLocation) {
			// Disable renames in interactive playground https://github.com/microsoft/vibecoda/issues/75137
			if (this.document.uri.scheme !== fileSchemes.walkThroughSnippet) {
				this.command = {
					command: CompositeCommand.ID,
					title: '',
					arguments: coalesce([
						this.command,
						{
							command: 'editor.action.rename',
							arguments: [[
								this.document.uri,
								typeConverters.Position.fromLocation(response.body.renameLocation)
							]]
						},
					])
				};
			}
		}
	}

	private static getKind(refactor: Proto.RefactorActionInfo) {
		if ((refactor as Proto.RefactorActionInfo & { kind?: string }).kind) {
			return vibecoda.CodeActionKind.Empty.append((refactor as Proto.RefactorActionInfo & { kind?: string }).kind!);
		}
		const match = allKnownCodeActionKinds.find(kind => kind.matches(refactor));
		return match ? match.kind : vibecoda.CodeActionKind.Refactor;
	}
}

class MoveToFileCodeAction extends vibecoda.CodeAction {
	constructor(
		document: vibecoda.TextDocument,
		action: Proto.RefactorActionInfo,
		range: vibecoda.Range,
		trigger: vibecoda.CodeActionTriggerKind,
	) {
		super(action.description, Move_File.kind);

		if (action.notApplicableReason) {
			this.disabled = { reason: action.notApplicableReason };
		}

		this.command = {
			title: action.description,
			command: MoveToFileRefactorCommand.ID,
			arguments: [{ action, document, range, trigger } satisfies MoveToFileRefactorCommand.Args]
		};
	}
}

class SelectCodeAction extends vibecoda.CodeAction {
	constructor(
		info: Proto.ApplicableRefactorInfo,
		document: vibecoda.TextDocument,
		rangeOrSelection: vibecoda.Range | vibecoda.Selection,
		trigger: vibecoda.CodeActionTriggerKind,
	) {
		super(info.description, vibecoda.CodeActionKind.Refactor);
		this.command = {
			title: info.description,
			command: SelectRefactorCommand.ID,
			arguments: [{ document, refactor: info, rangeOrSelection, trigger } satisfies SelectRefactorCommand.Args]
		};
	}
}
type TsCodeAction = InlinedCodeAction | MoveToFileCodeAction | SelectCodeAction;

class TypeScriptRefactorProvider implements vibecoda.CodeActionProvider<TsCodeAction> {

	private static readonly _declarationKinds = new Set([
		PConst.Kind.module,
		PConst.Kind.class,
		PConst.Kind.interface,
		PConst.Kind.function,
		PConst.Kind.enum,
		PConst.Kind.type,
		PConst.Kind.const,
		PConst.Kind.variable,
		PConst.Kind.let,
	]);

	private static isOnSignatureName(node: Proto.NavigationTree, range: vibecoda.Range): boolean {
		if (this._declarationKinds.has(node.kind)) {
			// Show when on the name span
			if (node.nameSpan) {
				const convertedSpan = typeConverters.Range.fromTextSpan(node.nameSpan);
				if (range.intersection(convertedSpan)) {
					return true;
				}
			}

			// Show when on the same line as an exported symbols without a name (handles default exports)
			if (!node.nameSpan && /\bexport\b/.test(node.kindModifiers) && node.spans.length) {
				const convertedSpan = typeConverters.Range.fromTextSpan(node.spans[0]);
				if (range.intersection(new vibecoda.Range(convertedSpan.start.line, 0, convertedSpan.start.line, Number.MAX_SAFE_INTEGER))) {
					return true;
				}
			}
		}

		// Show if on the signature of any children
		return node.childItems?.some(child => this.isOnSignatureName(child, range)) ?? false;
	}

	constructor(
		private readonly client: ITypeScriptServiceClient,
		private readonly cachedNavTree: CachedResponse<Proto.NavTreeResponse>,
		private readonly formattingOptionsManager: FormattingOptionsManager,
		commandManager: CommandManager,
		telemetryReporter: TelemetryReporter
	) {
		const didApplyRefactoringCommand = new DidApplyRefactoringCommand(telemetryReporter);
		commandManager.register(didApplyRefactoringCommand);

		commandManager.register(new CompositeCommand());
		commandManager.register(new SelectRefactorCommand(this.client));
		commandManager.register(new MoveToFileRefactorCommand(this.client, didApplyRefactoringCommand));
		commandManager.register(new EditorChatFollowUp(this.client, telemetryReporter));
	}

	public static readonly metadata: vibecoda.CodeActionProviderMetadata = {
		providedCodeActionKinds: [
			vibecoda.CodeActionKind.Refactor,
			...allKnownCodeActionKinds.map(x => x.kind),
		],
		documentation: [
			{
				kind: vibecoda.CodeActionKind.Refactor,
				command: {
					command: LearnMoreAboutRefactoringsCommand.id,
					title: vibecoda.l10n.t("Learn more about JS/TS refactorings")
				}
			}
		]
	};

	public async provideCodeActions(
		document: vibecoda.TextDocument,
		rangeOrSelection: vibecoda.Range | vibecoda.Selection,
		context: vibecoda.CodeActionContext,
		token: vibecoda.CancellationToken
	): Promise<TsCodeAction[] | undefined> {
		if (!this.shouldTrigger(context, rangeOrSelection)) {
			return undefined;
		}
		if (!this.client.toOpenTsFilePath(document)) {
			return undefined;
		}

		const response = await this.interruptGetErrIfNeeded(context, () => {
			const file = this.client.toOpenTsFilePath(document);
			if (!file) {
				return undefined;
			}

			this.formattingOptionsManager.ensureConfigurationForDocument(document, token);

			const args: Proto.GetApplicableRefactorsRequestArgs = {
				...typeConverters.Range.toFileRangeRequestArgs(file, rangeOrSelection),
				triggerReason: this.toTsTriggerReason(context),
				kind: context.only?.value,
				includeInteractiveActions: this.client.apiVersion.gte(API.v520),
			};
			return this.client.execute('getApplicableRefactors', args, token);
		});
		if (response?.type !== 'response' || !response.body) {
			return undefined;
		}

		const applicableRefactors = this.convertApplicableRefactors(document, context, response.body, rangeOrSelection);
		const actions = coalesce(await Promise.all(Array.from(applicableRefactors, async action => {
			if (this.client.apiVersion.lt(API.v430)) {
				// Don't show 'infer return type' refactoring unless it has been explicitly requested
				// https://github.com/microsoft/TypeScript/issues/42993
				if (!context.only && action.kind?.value === 'refactor.rewrite.function.returnType') {
					return undefined;
				}
			}

			// Don't include move actions on auto light bulb unless you are on a declaration name
			if (this.client.apiVersion.lt(API.v540) && context.triggerKind === vibecoda.CodeActionTriggerKind.Automatic) {
				if (action.kind?.value === Move_NewFile.kind.value || action.kind?.value === Move_File.kind.value) {
					const file = this.client.toOpenTsFilePath(document);
					if (!file) {
						return undefined;
					}

					const navTree = await this.cachedNavTree.execute(document, () => this.client.execute('navtree', { file }, token));
					if (navTree.type !== 'response' || !navTree.body || !TypeScriptRefactorProvider.isOnSignatureName(navTree.body, rangeOrSelection)) {
						return undefined;
					}
				}
			}

			return action;
		})));

		if (!context.only) {
			return actions;
		}

		return this.pruneInvalidActions(this.appendInvalidActions(actions), context.only, /* numberOfInvalid = */ 5);
	}

	private interruptGetErrIfNeeded<R>(context: vibecoda.CodeActionContext, f: () => R): R {
		// Only interrupt diagnostics computation when code actions are explicitly
		// (such as using the refactor command or a keybinding). This is a clear
		// user action so we want to return results as quickly as possible.
		if (context.triggerKind === vibecoda.CodeActionTriggerKind.Invoke) {
			return this.client.interruptGetErr(f);
		} else {
			return f();
		}
	}

	public async resolveCodeAction(
		codeAction: TsCodeAction,
		token: vibecoda.CancellationToken,
	): Promise<TsCodeAction> {
		if (codeAction instanceof InlinedCodeAction) {
			await codeAction.resolve(token);
		}
		return codeAction;
	}

	private toTsTriggerReason(context: vibecoda.CodeActionContext): Proto.RefactorTriggerReason | undefined {
		return context.triggerKind === vibecoda.CodeActionTriggerKind.Invoke ? 'invoked' : 'implicit';
	}

	private *convertApplicableRefactors(
		document: vibecoda.TextDocument,
		context: vibecoda.CodeActionContext,
		refactors: readonly Proto.ApplicableRefactorInfo[],
		rangeOrSelection: vibecoda.Range | vibecoda.Selection
	): Iterable<TsCodeAction> {
		for (const refactor of refactors) {
			if (refactor.inlineable === false) {
				yield new SelectCodeAction(refactor, document, rangeOrSelection, context.triggerKind);
			} else {
				for (const action of refactor.actions) {
					for (const codeAction of this.refactorActionToCodeActions(document, context, refactor, action, rangeOrSelection, refactor.actions)) {
						yield codeAction;
					}
				}
			}
		}
	}

	private refactorActionToCodeActions(
		document: vibecoda.TextDocument,
		context: vibecoda.CodeActionContext,
		refactor: Proto.ApplicableRefactorInfo,
		action: Proto.RefactorActionInfo,
		rangeOrSelection: vibecoda.Range | vibecoda.Selection,
		allActions: readonly Proto.RefactorActionInfo[],
	): TsCodeAction[] {
		const codeActions: TsCodeAction[] = [];
		if (action.name === 'Move to file') {
			codeActions.push(new MoveToFileCodeAction(document, action, rangeOrSelection, context.triggerKind));
		} else {
			codeActions.push(new InlinedCodeAction(this.client, document, refactor, action, rangeOrSelection, context.triggerKind));
		}
		for (const codeAction of codeActions) {
			codeAction.isPreferred = TypeScriptRefactorProvider.isPreferred(action, allActions);
		}
		return codeActions;
	}

	private shouldTrigger(context: vibecoda.CodeActionContext, rangeOrSelection: vibecoda.Range | vibecoda.Selection) {
		if (context.only && !vibecoda.CodeActionKind.Refactor.contains(context.only)) {
			return false;
		}
		if (context.triggerKind === vibecoda.CodeActionTriggerKind.Invoke) {
			return true;
		}
		return rangeOrSelection instanceof vibecoda.Selection;
	}

	private static isPreferred(
		action: Proto.RefactorActionInfo,
		allActions: readonly Proto.RefactorActionInfo[],
	): boolean {
		if (Extract_Constant.matches(action)) {
			// Only mark the action with the lowest scope as preferred
			const getScope = (name: string) => {
				const scope = name.match(/scope_(\d)/)?.[1];
				return scope ? +scope : undefined;
			};
			const scope = getScope(action.name);
			if (typeof scope !== 'number') {
				return false;
			}

			return allActions
				.filter(otherAtion => otherAtion !== action && Extract_Constant.matches(otherAtion))
				.every(otherAction => {
					const otherScope = getScope(otherAction.name);
					return typeof otherScope === 'number' ? scope < otherScope : true;
				});
		}
		if (Extract_Type.matches(action) || Extract_Interface.matches(action)) {
			return true;
		}
		return false;
	}

	private appendInvalidActions(actions: vibecoda.CodeAction[]): vibecoda.CodeAction[] {
		if (this.client.apiVersion.gte(API.v400)) {
			// Invalid actions come from TS server instead
			return actions;
		}

		if (!actions.some(action => action.kind && Extract_Constant.kind.contains(action.kind))) {
			const disabledAction = new vibecoda.CodeAction(
				vibecoda.l10n.t("Extract to constant"),
				Extract_Constant.kind);

			disabledAction.disabled = {
				reason: vibecoda.l10n.t("The current selection cannot be extracted"),
			};
			disabledAction.isPreferred = true;

			actions.push(disabledAction);
		}

		if (!actions.some(action => action.kind && Extract_Function.kind.contains(action.kind))) {
			const disabledAction = new vibecoda.CodeAction(
				vibecoda.l10n.t("Extract to function"),
				Extract_Function.kind);

			disabledAction.disabled = {
				reason: vibecoda.l10n.t("The current selection cannot be extracted"),
			};
			actions.push(disabledAction);
		}
		return actions;
	}

	private pruneInvalidActions(actions: vibecoda.CodeAction[], only?: vibecoda.CodeActionKind, numberOfInvalid?: number): vibecoda.CodeAction[] {
		if (this.client.apiVersion.lt(API.v400)) {
			// Older TS version don't return extra actions
			return actions;
		}

		const availableActions: vibecoda.CodeAction[] = [];
		const invalidCommonActions: vibecoda.CodeAction[] = [];
		const invalidUncommonActions: vibecoda.CodeAction[] = [];
		for (const action of actions) {
			if (!action.disabled) {
				availableActions.push(action);
				continue;
			}

			// These are the common refactors that we should always show if applicable.
			if (action.kind && (Extract_Constant.kind.contains(action.kind) || Extract_Function.kind.contains(action.kind))) {
				invalidCommonActions.push(action);
				continue;
			}

			// These are the remaining refactors that we can show if we haven't reached the max limit with just common refactors.
			invalidUncommonActions.push(action);
		}

		const prioritizedActions: vibecoda.CodeAction[] = [];
		prioritizedActions.push(...invalidCommonActions);
		prioritizedActions.push(...invalidUncommonActions);
		const topNInvalid = prioritizedActions.filter(action => !only || (action.kind && only.contains(action.kind))).slice(0, numberOfInvalid);
		availableActions.push(...topNInvalid);
		return availableActions;
	}
}

export function register(
	selector: DocumentSelector,
	client: ITypeScriptServiceClient,
	cachedNavTree: CachedResponse<Proto.NavTreeResponse>,
	formattingOptionsManager: FormattingOptionsManager,
	commandManager: CommandManager,
	telemetryReporter: TelemetryReporter,
) {
	return conditionalRegistration([
		requireSomeCapability(client, ClientCapability.Semantic),
	], () => {
		return vibecoda.languages.registerCodeActionsProvider(selector.semantic,
			new TypeScriptRefactorProvider(client, cachedNavTree, formattingOptionsManager, commandManager, telemetryReporter),
			TypeScriptRefactorProvider.metadata);
	});
}
