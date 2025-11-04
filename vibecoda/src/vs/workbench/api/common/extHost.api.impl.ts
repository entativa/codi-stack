/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import type * as vibecoda from 'vibecoda';
import { CancellationTokenSource } from '../../../base/common/cancellation.js';
import * as errors from '../../../base/common/errors.js';
import { Emitter, Event } from '../../../base/common/event.js';
import { combinedDisposable } from '../../../base/common/lifecycle.js';
import { Schemas, matchesScheme } from '../../../base/common/network.js';
import Severity from '../../../base/common/severity.js';
import { URI } from '../../../base/common/uri.js';
import { TextEditorCursorStyle } from '../../../editor/common/config/editorOptions.js';
import { score, targetsNotebooks } from '../../../editor/common/languageSelector.js';
import * as languageConfiguration from '../../../editor/common/languages/languageConfiguration.js';
import { OverviewRulerLane } from '../../../editor/common/model.js';
import { ExtensionError, ExtensionIdentifierSet, IExtensionDescription } from '../../../platform/extensions/common/extensions.js';
import * as files from '../../../platform/files/common/files.js';
import { ServicesAccessor } from '../../../platform/instantiation/common/instantiation.js';
import { ILogService, ILoggerService, LogLevel } from '../../../platform/log/common/log.js';
import { getRemoteName } from '../../../platform/remote/common/remoteHosts.js';
import { TelemetryTrustedValue } from '../../../platform/telemetry/common/telemetryUtils.js';
import { EditSessionIdentityMatch } from '../../../platform/workspace/common/editSessions.js';
import { DebugConfigurationProviderTriggerKind } from '../../contrib/debug/common/debug.js';
import { ExtensionDescriptionRegistry } from '../../services/extensions/common/extensionDescriptionRegistry.js';
import { UIKind } from '../../services/extensions/common/extensionHostProtocol.js';
import { checkProposedApiEnabled, isProposedApiEnabled } from '../../services/extensions/common/extensions.js';
import { ProxyIdentifier } from '../../services/extensions/common/proxyIdentifier.js';
import { AISearchKeyword, ExcludeSettingOptions, TextSearchCompleteMessageType, TextSearchContext2, TextSearchMatch2 } from '../../services/search/common/searchExtTypes.js';
import { CandidatePortSource, ExtHostContext, ExtHostLogLevelServiceShape, MainContext } from './extHost.protocol.js';
import { ExtHostRelatedInformation } from './extHostAiRelatedInformation.js';
import { ExtHostAiSettingsSearch } from './extHostAiSettingsSearch.js';
import { ExtHostApiCommands } from './extHostApiCommands.js';
import { IExtHostApiDeprecationService } from './extHostApiDeprecationService.js';
import { IExtHostAuthentication } from './extHostAuthentication.js';
import { ExtHostBulkEdits } from './extHostBulkEdits.js';
import { ExtHostChatAgents2 } from './extHostChatAgents2.js';
import { ExtHostChatOutputRenderer } from './extHostChatOutputRenderer.js';
import { ExtHostChatSessions } from './extHostChatSessions.js';
import { ExtHostChatStatus } from './extHostChatStatus.js';
import { ExtHostClipboard } from './extHostClipboard.js';
import { ExtHostEditorInsets } from './extHostCodeInsets.js';
import { ExtHostCodeMapper } from './extHostCodeMapper.js';
import { IExtHostCommands } from './extHostCommands.js';
import { createExtHostComments } from './extHostComments.js';
import { ExtHostConfigProvider, IExtHostConfiguration } from './extHostConfiguration.js';
import { ExtHostCustomEditors } from './extHostCustomEditors.js';
import { IExtHostDataChannels } from './extHostDataChannels.js';
import { IExtHostDebugService } from './extHostDebugService.js';
import { IExtHostDecorations } from './extHostDecorations.js';
import { ExtHostDiagnostics } from './extHostDiagnostics.js';
import { ExtHostDialogs } from './extHostDialogs.js';
import { ExtHostDocumentContentProvider } from './extHostDocumentContentProviders.js';
import { ExtHostDocumentSaveParticipant } from './extHostDocumentSaveParticipant.js';
import { ExtHostDocuments } from './extHostDocuments.js';
import { IExtHostDocumentsAndEditors } from './extHostDocumentsAndEditors.js';
import { IExtHostEditorTabs } from './extHostEditorTabs.js';
import { ExtHostEmbeddings } from './extHostEmbedding.js';
import { ExtHostAiEmbeddingVector } from './extHostEmbeddingVector.js';
import { Extension, IExtHostExtensionService } from './extHostExtensionService.js';
import { ExtHostFileSystem } from './extHostFileSystem.js';
import { IExtHostConsumerFileSystem } from './extHostFileSystemConsumer.js';
import { ExtHostFileSystemEventService, FileSystemWatcherCreateOptions } from './extHostFileSystemEventService.js';
import { IExtHostFileSystemInfo } from './extHostFileSystemInfo.js';
import { IExtHostInitDataService } from './extHostInitDataService.js';
import { ExtHostInteractive } from './extHostInteractive.js';
import { ExtHostLabelService } from './extHostLabelService.js';
import { ExtHostLanguageFeatures } from './extHostLanguageFeatures.js';
import { ExtHostLanguageModelTools } from './extHostLanguageModelTools.js';
import { IExtHostLanguageModels } from './extHostLanguageModels.js';
import { ExtHostLanguages } from './extHostLanguages.js';
import { IExtHostLocalizationService } from './extHostLocalizationService.js';
import { IExtHostManagedSockets } from './extHostManagedSockets.js';
import { IExtHostMpcService } from './extHostMcp.js';
import { ExtHostMessageService } from './extHostMessageService.js';
import { ExtHostNotebookController } from './extHostNotebook.js';
import { ExtHostNotebookDocumentSaveParticipant } from './extHostNotebookDocumentSaveParticipant.js';
import { ExtHostNotebookDocuments } from './extHostNotebookDocuments.js';
import { ExtHostNotebookEditors } from './extHostNotebookEditors.js';
import { ExtHostNotebookKernels } from './extHostNotebookKernels.js';
import { ExtHostNotebookRenderers } from './extHostNotebookRenderers.js';
import { IExtHostOutputService } from './extHostOutput.js';
import { ExtHostProfileContentHandlers } from './extHostProfileContentHandler.js';
import { IExtHostProgress } from './extHostProgress.js';
import { ExtHostQuickDiff } from './extHostQuickDiff.js';
import { createExtHostQuickOpen } from './extHostQuickOpen.js';
import { IExtHostRpcService } from './extHostRpcService.js';
import { ExtHostSCM } from './extHostSCM.js';
import { IExtHostSearch } from './extHostSearch.js';
import { IExtHostSecretState } from './extHostSecretState.js';
import { ExtHostShare } from './extHostShare.js';
import { ExtHostSpeech } from './extHostSpeech.js';
import { ExtHostStatusBar } from './extHostStatusBar.js';
import { IExtHostStorage } from './extHostStorage.js';
import { IExtensionStoragePaths } from './extHostStoragePaths.js';
import { IExtHostTask } from './extHostTask.js';
import { ExtHostTelemetryLogger, IExtHostTelemetry, isNewAppInstall } from './extHostTelemetry.js';
import { IExtHostTerminalService } from './extHostTerminalService.js';
import { IExtHostTerminalShellIntegration } from './extHostTerminalShellIntegration.js';
import { IExtHostTesting } from './extHostTesting.js';
import { ExtHostEditors } from './extHostTextEditors.js';
import { ExtHostTheming } from './extHostTheming.js';
import { ExtHostTimeline } from './extHostTimeline.js';
import { ExtHostTreeViews } from './extHostTreeViews.js';
import { IExtHostTunnelService } from './extHostTunnelService.js';
import * as typeConverters from './extHostTypeConverters.js';
import * as extHostTypes from './extHostTypes.js';
import { ExtHostUriOpeners } from './extHostUriOpener.js';
import { IURITransformerService } from './extHostUriTransformerService.js';
import { IExtHostUrlsService } from './extHostUrls.js';
import { ExtHostWebviews } from './extHostWebview.js';
import { ExtHostWebviewPanels } from './extHostWebviewPanels.js';
import { ExtHostWebviewViews } from './extHostWebviewView.js';
import { IExtHostWindow } from './extHostWindow.js';
import { IExtHostWorkspace } from './extHostWorkspace.js';
import { ExtHostChatContext } from './extHostChatContext.js';

export interface IExtensionRegistries {
	mine: ExtensionDescriptionRegistry;
	all: ExtensionDescriptionRegistry;
}

export interface IExtensionApiFactory {
	(extension: IExtensionDescription, extensionInfo: IExtensionRegistries, configProvider: ExtHostConfigProvider): typeof vibecoda;
}

/**
 * This method instantiates and returns the extension API surface
 */
export function createApiFactoryAndRegisterActors(accessor: ServicesAccessor): IExtensionApiFactory {

	// services
	const initData = accessor.get(IExtHostInitDataService);
	const extHostFileSystemInfo = accessor.get(IExtHostFileSystemInfo);
	const extHostConsumerFileSystem = accessor.get(IExtHostConsumerFileSystem);
	const extensionService = accessor.get(IExtHostExtensionService);
	const extHostWorkspace = accessor.get(IExtHostWorkspace);
	const extHostTelemetry = accessor.get(IExtHostTelemetry);
	const extHostConfiguration = accessor.get(IExtHostConfiguration);
	const uriTransformer = accessor.get(IURITransformerService);
	const rpcProtocol = accessor.get(IExtHostRpcService);
	const extHostStorage = accessor.get(IExtHostStorage);
	const extensionStoragePaths = accessor.get(IExtensionStoragePaths);
	const extHostLoggerService = accessor.get(ILoggerService);
	const extHostLogService = accessor.get(ILogService);
	const extHostTunnelService = accessor.get(IExtHostTunnelService);
	const extHostApiDeprecation = accessor.get(IExtHostApiDeprecationService);
	const extHostWindow = accessor.get(IExtHostWindow);
	const extHostUrls = accessor.get(IExtHostUrlsService);
	const extHostSecretState = accessor.get(IExtHostSecretState);
	const extHostEditorTabs = accessor.get(IExtHostEditorTabs);
	const extHostManagedSockets = accessor.get(IExtHostManagedSockets);
	const extHostProgress = accessor.get(IExtHostProgress);
	const extHostAuthentication = accessor.get(IExtHostAuthentication);
	const extHostLanguageModels = accessor.get(IExtHostLanguageModels);
	const extHostMcp = accessor.get(IExtHostMpcService);
	const extHostDataChannels = accessor.get(IExtHostDataChannels);

	// register addressable instances
	rpcProtocol.set(ExtHostContext.ExtHostFileSystemInfo, extHostFileSystemInfo);
	// eslint-disable-next-line local/code-no-any-casts
	rpcProtocol.set(ExtHostContext.ExtHostLogLevelServiceShape, <ExtHostLogLevelServiceShape><any>extHostLoggerService);
	rpcProtocol.set(ExtHostContext.ExtHostWorkspace, extHostWorkspace);
	rpcProtocol.set(ExtHostContext.ExtHostConfiguration, extHostConfiguration);
	rpcProtocol.set(ExtHostContext.ExtHostExtensionService, extensionService);
	rpcProtocol.set(ExtHostContext.ExtHostStorage, extHostStorage);
	rpcProtocol.set(ExtHostContext.ExtHostTunnelService, extHostTunnelService);
	rpcProtocol.set(ExtHostContext.ExtHostWindow, extHostWindow);
	rpcProtocol.set(ExtHostContext.ExtHostUrls, extHostUrls);
	rpcProtocol.set(ExtHostContext.ExtHostSecretState, extHostSecretState);
	rpcProtocol.set(ExtHostContext.ExtHostTelemetry, extHostTelemetry);
	rpcProtocol.set(ExtHostContext.ExtHostEditorTabs, extHostEditorTabs);
	rpcProtocol.set(ExtHostContext.ExtHostManagedSockets, extHostManagedSockets);
	rpcProtocol.set(ExtHostContext.ExtHostProgress, extHostProgress);
	rpcProtocol.set(ExtHostContext.ExtHostAuthentication, extHostAuthentication);
	rpcProtocol.set(ExtHostContext.ExtHostChatProvider, extHostLanguageModels);
	rpcProtocol.set(ExtHostContext.ExtHostDataChannels, extHostDataChannels);

	// automatically create and register addressable instances
	const extHostDecorations = rpcProtocol.set(ExtHostContext.ExtHostDecorations, accessor.get(IExtHostDecorations));
	const extHostDocumentsAndEditors = rpcProtocol.set(ExtHostContext.ExtHostDocumentsAndEditors, accessor.get(IExtHostDocumentsAndEditors));
	const extHostCommands = rpcProtocol.set(ExtHostContext.ExtHostCommands, accessor.get(IExtHostCommands));
	const extHostTerminalService = rpcProtocol.set(ExtHostContext.ExtHostTerminalService, accessor.get(IExtHostTerminalService));
	const extHostTerminalShellIntegration = rpcProtocol.set(ExtHostContext.ExtHostTerminalShellIntegration, accessor.get(IExtHostTerminalShellIntegration));
	const extHostDebugService = rpcProtocol.set(ExtHostContext.ExtHostDebugService, accessor.get(IExtHostDebugService));
	const extHostSearch = rpcProtocol.set(ExtHostContext.ExtHostSearch, accessor.get(IExtHostSearch));
	const extHostTask = rpcProtocol.set(ExtHostContext.ExtHostTask, accessor.get(IExtHostTask));
	const extHostOutputService = rpcProtocol.set(ExtHostContext.ExtHostOutputService, accessor.get(IExtHostOutputService));
	const extHostLocalization = rpcProtocol.set(ExtHostContext.ExtHostLocalization, accessor.get(IExtHostLocalizationService));

	// manually create and register addressable instances
	const extHostDocuments = rpcProtocol.set(ExtHostContext.ExtHostDocuments, new ExtHostDocuments(rpcProtocol, extHostDocumentsAndEditors));
	const extHostDocumentContentProviders = rpcProtocol.set(ExtHostContext.ExtHostDocumentContentProviders, new ExtHostDocumentContentProvider(rpcProtocol, extHostDocumentsAndEditors, extHostLogService));
	const extHostDocumentSaveParticipant = rpcProtocol.set(ExtHostContext.ExtHostDocumentSaveParticipant, new ExtHostDocumentSaveParticipant(extHostLogService, extHostDocuments, rpcProtocol.getProxy(MainContext.MainThreadBulkEdits)));
	const extHostNotebook = rpcProtocol.set(ExtHostContext.ExtHostNotebook, new ExtHostNotebookController(rpcProtocol, extHostCommands, extHostDocumentsAndEditors, extHostDocuments, extHostConsumerFileSystem, extHostSearch, extHostLogService));
	const extHostNotebookDocuments = rpcProtocol.set(ExtHostContext.ExtHostNotebookDocuments, new ExtHostNotebookDocuments(extHostNotebook));
	const extHostNotebookEditors = rpcProtocol.set(ExtHostContext.ExtHostNotebookEditors, new ExtHostNotebookEditors(extHostLogService, extHostNotebook));
	const extHostNotebookKernels = rpcProtocol.set(ExtHostContext.ExtHostNotebookKernels, new ExtHostNotebookKernels(rpcProtocol, initData, extHostNotebook, extHostCommands, extHostLogService));
	const extHostNotebookRenderers = rpcProtocol.set(ExtHostContext.ExtHostNotebookRenderers, new ExtHostNotebookRenderers(rpcProtocol, extHostNotebook));
	const extHostNotebookDocumentSaveParticipant = rpcProtocol.set(ExtHostContext.ExtHostNotebookDocumentSaveParticipant, new ExtHostNotebookDocumentSaveParticipant(extHostLogService, extHostNotebook, rpcProtocol.getProxy(MainContext.MainThreadBulkEdits)));
	const extHostEditors = rpcProtocol.set(ExtHostContext.ExtHostEditors, new ExtHostEditors(rpcProtocol, extHostDocumentsAndEditors));
	const extHostTreeViews = rpcProtocol.set(ExtHostContext.ExtHostTreeViews, new ExtHostTreeViews(rpcProtocol.getProxy(MainContext.MainThreadTreeViews), extHostCommands, extHostLogService));
	const extHostEditorInsets = rpcProtocol.set(ExtHostContext.ExtHostEditorInsets, new ExtHostEditorInsets(rpcProtocol.getProxy(MainContext.MainThreadEditorInsets), extHostEditors, initData.remote));
	const extHostDiagnostics = rpcProtocol.set(ExtHostContext.ExtHostDiagnostics, new ExtHostDiagnostics(rpcProtocol, extHostLogService, extHostFileSystemInfo, extHostDocumentsAndEditors));
	const extHostLanguages = rpcProtocol.set(ExtHostContext.ExtHostLanguages, new ExtHostLanguages(rpcProtocol, extHostDocuments, extHostCommands.converter, uriTransformer));
	const extHostLanguageFeatures = rpcProtocol.set(ExtHostContext.ExtHostLanguageFeatures, new ExtHostLanguageFeatures(rpcProtocol, uriTransformer, extHostDocuments, extHostCommands, extHostDiagnostics, extHostLogService, extHostApiDeprecation, extHostTelemetry));
	const extHostCodeMapper = rpcProtocol.set(ExtHostContext.ExtHostCodeMapper, new ExtHostCodeMapper(rpcProtocol));
	const extHostFileSystem = rpcProtocol.set(ExtHostContext.ExtHostFileSystem, new ExtHostFileSystem(rpcProtocol, extHostLanguageFeatures));
	const extHostFileSystemEvent = rpcProtocol.set(ExtHostContext.ExtHostFileSystemEventService, new ExtHostFileSystemEventService(rpcProtocol, extHostLogService, extHostDocumentsAndEditors));
	const extHostQuickOpen = rpcProtocol.set(ExtHostContext.ExtHostQuickOpen, createExtHostQuickOpen(rpcProtocol, extHostWorkspace, extHostCommands));
	const extHostSCM = rpcProtocol.set(ExtHostContext.ExtHostSCM, new ExtHostSCM(rpcProtocol, extHostCommands, extHostDocuments, extHostLogService));
	const extHostQuickDiff = rpcProtocol.set(ExtHostContext.ExtHostQuickDiff, new ExtHostQuickDiff(rpcProtocol, uriTransformer));
	const extHostShare = rpcProtocol.set(ExtHostContext.ExtHostShare, new ExtHostShare(rpcProtocol, uriTransformer));
	const extHostComment = rpcProtocol.set(ExtHostContext.ExtHostComments, createExtHostComments(rpcProtocol, extHostCommands, extHostDocuments));
	const extHostLabelService = rpcProtocol.set(ExtHostContext.ExtHostLabelService, new ExtHostLabelService(rpcProtocol));
	const extHostTheming = rpcProtocol.set(ExtHostContext.ExtHostTheming, new ExtHostTheming(rpcProtocol));
	const extHostTimeline = rpcProtocol.set(ExtHostContext.ExtHostTimeline, new ExtHostTimeline(rpcProtocol, extHostCommands));
	const extHostWebviews = rpcProtocol.set(ExtHostContext.ExtHostWebviews, new ExtHostWebviews(rpcProtocol, initData.remote, extHostWorkspace, extHostLogService, extHostApiDeprecation));
	const extHostWebviewPanels = rpcProtocol.set(ExtHostContext.ExtHostWebviewPanels, new ExtHostWebviewPanels(rpcProtocol, extHostWebviews, extHostWorkspace));
	const extHostCustomEditors = rpcProtocol.set(ExtHostContext.ExtHostCustomEditors, new ExtHostCustomEditors(rpcProtocol, extHostDocuments, extensionStoragePaths, extHostWebviews, extHostWebviewPanels));
	const extHostWebviewViews = rpcProtocol.set(ExtHostContext.ExtHostWebviewViews, new ExtHostWebviewViews(rpcProtocol, extHostWebviews));
	const extHostTesting = rpcProtocol.set(ExtHostContext.ExtHostTesting, accessor.get(IExtHostTesting));
	const extHostUriOpeners = rpcProtocol.set(ExtHostContext.ExtHostUriOpeners, new ExtHostUriOpeners(rpcProtocol));
	const extHostProfileContentHandlers = rpcProtocol.set(ExtHostContext.ExtHostProfileContentHandlers, new ExtHostProfileContentHandlers(rpcProtocol));
	const extHostChatOutputRenderer = rpcProtocol.set(ExtHostContext.ExtHostChatOutputRenderer, new ExtHostChatOutputRenderer(rpcProtocol, extHostWebviews));
	rpcProtocol.set(ExtHostContext.ExtHostInteractive, new ExtHostInteractive(rpcProtocol, extHostNotebook, extHostDocumentsAndEditors, extHostCommands, extHostLogService));
	const extHostLanguageModelTools = rpcProtocol.set(ExtHostContext.ExtHostLanguageModelTools, new ExtHostLanguageModelTools(rpcProtocol, extHostLanguageModels));
	const extHostChatSessions = rpcProtocol.set(ExtHostContext.ExtHostChatSessions, new ExtHostChatSessions(extHostCommands, extHostLanguageModels, rpcProtocol, extHostLogService));
	const extHostChatAgents2 = rpcProtocol.set(ExtHostContext.ExtHostChatAgents2, new ExtHostChatAgents2(rpcProtocol, extHostLogService, extHostCommands, extHostDocuments, extHostLanguageModels, extHostDiagnostics, extHostLanguageModelTools));
	const extHostChatContext = rpcProtocol.set(ExtHostContext.ExtHostChatContext, new ExtHostChatContext(rpcProtocol));
	const extHostAiRelatedInformation = rpcProtocol.set(ExtHostContext.ExtHostAiRelatedInformation, new ExtHostRelatedInformation(rpcProtocol));
	const extHostAiEmbeddingVector = rpcProtocol.set(ExtHostContext.ExtHostAiEmbeddingVector, new ExtHostAiEmbeddingVector(rpcProtocol));
	const extHostAiSettingsSearch = rpcProtocol.set(ExtHostContext.ExtHostAiSettingsSearch, new ExtHostAiSettingsSearch(rpcProtocol));
	const extHostStatusBar = rpcProtocol.set(ExtHostContext.ExtHostStatusBar, new ExtHostStatusBar(rpcProtocol, extHostCommands.converter));
	const extHostSpeech = rpcProtocol.set(ExtHostContext.ExtHostSpeech, new ExtHostSpeech(rpcProtocol));
	const extHostEmbeddings = rpcProtocol.set(ExtHostContext.ExtHostEmbeddings, new ExtHostEmbeddings(rpcProtocol));

	rpcProtocol.set(ExtHostContext.ExtHostMcp, accessor.get(IExtHostMpcService));

	// Check that no named customers are missing
	const expected = Object.values<ProxyIdentifier<any>>(ExtHostContext);
	rpcProtocol.assertRegistered(expected);

	// Other instances
	const extHostBulkEdits = new ExtHostBulkEdits(rpcProtocol, extHostDocumentsAndEditors);
	const extHostClipboard = new ExtHostClipboard(rpcProtocol);
	const extHostMessageService = new ExtHostMessageService(rpcProtocol, extHostLogService);
	const extHostDialogs = new ExtHostDialogs(rpcProtocol);
	const extHostChatStatus = new ExtHostChatStatus(rpcProtocol);

	// Register API-ish commands
	ExtHostApiCommands.register(extHostCommands);

	return function (extension: IExtensionDescription, extensionInfo: IExtensionRegistries, configProvider: ExtHostConfigProvider): typeof vibecoda {

		// Wraps an event with error handling and telemetry so that we know what extension fails
		// handling events. This will prevent us from reporting this as "our" error-telemetry and
		// allows for better blaming
		function _asExtensionEvent<T>(actual: vibecoda.Event<T>): vibecoda.Event<T> {
			return (listener, thisArgs, disposables) => {
				const handle = actual(e => {
					try {
						listener.call(thisArgs, e);
					} catch (err) {
						errors.onUnexpectedExternalError(new ExtensionError(extension.identifier, err, 'FAILED to handle event'));
					}
				});
				disposables?.push(handle);
				return handle;
			};
		}


		// Check document selectors for being overly generic. Technically this isn't a problem but
		// in practice many extensions say they support `fooLang` but need fs-access to do so. Those
		// extension should specify then the `file`-scheme, e.g. `{ scheme: 'fooLang', language: 'fooLang' }`
		// We only inform once, it is not a warning because we just want to raise awareness and because
		// we cannot say if the extension is doing it right or wrong...
		const checkSelector = (function () {
			let done = !extension.isUnderDevelopment;
			function informOnce() {
				if (!done) {
					extHostLogService.info(`Extension '${extension.identifier.value}' uses a document selector without scheme. Learn more about this: https://go.microsoft.com/fwlink/?linkid=872305`);
					done = true;
				}
			}
			return function perform(selector: vibecoda.DocumentSelector): vibecoda.DocumentSelector {
				if (Array.isArray(selector)) {
					selector.forEach(perform);
				} else if (typeof selector === 'string') {
					informOnce();
				} else {
					const filter = selector as vibecoda.DocumentFilter; // TODO: microsoft/TypeScript#42768
					if (typeof filter.scheme === 'undefined') {
						informOnce();
					}
					if (typeof filter.exclusive === 'boolean') {
						checkProposedApiEnabled(extension, 'documentFiltersExclusive');
					}
				}
				return selector;
			};
		})();

		const authentication: typeof vibecoda.authentication = {
			getSession(providerId: string, scopesOrChallenge: readonly string[] | vibecoda.AuthenticationWwwAuthenticateRequest, options?: vibecoda.AuthenticationGetSessionOptions) {
				if (
					(typeof options?.forceNewSession === 'object' && options.forceNewSession.learnMore) ||
					(typeof options?.createIfNone === 'object' && options.createIfNone.learnMore)
				) {
					checkProposedApiEnabled(extension, 'authLearnMore');
				}
				if (options?.authorizationServer) {
					checkProposedApiEnabled(extension, 'authIssuers');
				}
				// eslint-disable-next-line local/code-no-any-casts
				return extHostAuthentication.getSession(extension, providerId, scopesOrChallenge, options as any);
			},
			getAccounts(providerId: string) {
				return extHostAuthentication.getAccounts(providerId);
			},
			// TODO: remove this after GHPR and Codespaces move off of it
			async hasSession(providerId: string, scopes: readonly string[]) {
				checkProposedApiEnabled(extension, 'authSession');
				// eslint-disable-next-line local/code-no-any-casts
				return !!(await extHostAuthentication.getSession(extension, providerId, scopes, { silent: true } as any));
			},
			get onDidChangeSessions(): vibecoda.Event<vibecoda.AuthenticationSessionsChangeEvent> {
				return _asExtensionEvent(extHostAuthentication.getExtensionScopedSessionsEvent(extension.identifier.value));
			},
			registerAuthenticationProvider(id: string, label: string, provider: vibecoda.AuthenticationProvider, options?: vibecoda.AuthenticationProviderOptions): vibecoda.Disposable {
				if (options?.supportedAuthorizationServers) {
					checkProposedApiEnabled(extension, 'authIssuers');
				}
				return extHostAuthentication.registerAuthenticationProvider(id, label, provider, options);
			}
		};

		// namespace: commands
		const commands: typeof vibecoda.commands = {
			registerCommand(id: string, command: <T>(...args: any[]) => T | Thenable<T>, thisArgs?: any): vibecoda.Disposable {
				return extHostCommands.registerCommand(true, id, command, thisArgs, undefined, extension);
			},
			registerTextEditorCommand(id: string, callback: (textEditor: vibecoda.TextEditor, edit: vibecoda.TextEditorEdit, ...args: any[]) => void, thisArg?: any): vibecoda.Disposable {
				return extHostCommands.registerCommand(true, id, (...args: any[]): any => {
					const activeTextEditor = extHostEditors.getActiveTextEditor();
					if (!activeTextEditor) {
						extHostLogService.warn('Cannot execute ' + id + ' because there is no active text editor.');
						return undefined;
					}

					return activeTextEditor.edit((edit: vibecoda.TextEditorEdit) => {
						callback.apply(thisArg, [activeTextEditor, edit, ...args]);

					}).then((result) => {
						if (!result) {
							extHostLogService.warn('Edits from command ' + id + ' were not applied.');
						}
					}, (err) => {
						extHostLogService.warn('An error occurred while running command ' + id, err);
					});
				}, undefined, undefined, extension);
			},
			registerDiffInformationCommand: (id: string, callback: (diff: vibecoda.LineChange[], ...args: any[]) => any, thisArg?: any): vibecoda.Disposable => {
				checkProposedApiEnabled(extension, 'diffCommand');
				return extHostCommands.registerCommand(true, id, async (...args: any[]): Promise<any> => {
					const activeTextEditor = extHostDocumentsAndEditors.activeEditor(true);
					if (!activeTextEditor) {
						extHostLogService.warn('Cannot execute ' + id + ' because there is no active text editor.');
						return undefined;
					}

					const diff = await extHostEditors.getDiffInformation(activeTextEditor.id);
					callback.apply(thisArg, [diff, ...args]);
				}, undefined, undefined, extension);
			},
			executeCommand<T>(id: string, ...args: any[]): Thenable<T> {
				return extHostCommands.executeCommand<T>(id, ...args);
			},
			getCommands(filterInternal: boolean = false): Thenable<string[]> {
				return extHostCommands.getCommands(filterInternal);
			}
		};

		// namespace: env
		const env: typeof vibecoda.env = {
			get machineId() { return initData.telemetryInfo.machineId; },
			get devDeviceId() {
				checkProposedApiEnabled(extension, 'devDeviceId');
				return initData.telemetryInfo.devDeviceId ?? initData.telemetryInfo.machineId;
			},
			get sessionId() { return initData.telemetryInfo.sessionId; },
			get language() { return initData.environment.appLanguage; },
			get appName() { return initData.environment.appName; },
			get appRoot() { return initData.environment.appRoot?.fsPath ?? ''; },
			get appHost() { return initData.environment.appHost; },
			get uriScheme() { return initData.environment.appUriScheme; },
			get clipboard(): vibecoda.Clipboard { return extHostClipboard.value; },
			get shell() {
				return extHostTerminalService.getDefaultShell(false);
			},
			get onDidChangeShell() {
				return _asExtensionEvent(extHostTerminalService.onDidChangeShell);
			},
			get isTelemetryEnabled() {
				return extHostTelemetry.getTelemetryConfiguration();
			},
			get onDidChangeTelemetryEnabled(): vibecoda.Event<boolean> {
				return _asExtensionEvent(extHostTelemetry.onDidChangeTelemetryEnabled);
			},
			get telemetryConfiguration(): vibecoda.TelemetryConfiguration {
				checkProposedApiEnabled(extension, 'telemetry');
				return extHostTelemetry.getTelemetryDetails();
			},
			get onDidChangeTelemetryConfiguration(): vibecoda.Event<vibecoda.TelemetryConfiguration> {
				checkProposedApiEnabled(extension, 'telemetry');
				return _asExtensionEvent(extHostTelemetry.onDidChangeTelemetryConfiguration);
			},
			get isNewAppInstall() {
				return isNewAppInstall(initData.telemetryInfo.firstSessionDate);
			},
			createTelemetryLogger(sender: vibecoda.TelemetrySender, options?: vibecoda.TelemetryLoggerOptions): vibecoda.TelemetryLogger {
				ExtHostTelemetryLogger.validateSender(sender);
				return extHostTelemetry.instantiateLogger(extension, sender, options);
			},
			async openExternal(uri: URI, options?: { allowContributedOpeners?: boolean | string }) {
				return extHostWindow.openUri(uri, {
					allowTunneling: initData.remote.isRemote ?? (initData.remote.authority ? await extHostTunnelService.hasTunnelProvider() : false),
					allowContributedOpeners: options?.allowContributedOpeners,
				});
			},
			async asExternalUri(uri: URI) {
				if (uri.scheme === initData.environment.appUriScheme) {
					return extHostUrls.createAppUri(uri);
				}

				try {
					return await extHostWindow.asExternalUri(uri, { allowTunneling: !!initData.remote.authority });
				} catch (err) {
					if (matchesScheme(uri, Schemas.http) || matchesScheme(uri, Schemas.https)) {
						return uri;
					}

					throw err;
				}
			},
			get remoteName() {
				return getRemoteName(initData.remote.authority);
			},
			get remoteAuthority() {
				checkProposedApiEnabled(extension, 'resolvers');
				return initData.remote.authority;
			},
			get uiKind() {
				return initData.uiKind;
			},
			get logLevel() {
				return extHostLogService.getLevel();
			},
			get onDidChangeLogLevel() {
				return _asExtensionEvent(extHostLogService.onDidChangeLogLevel);
			},
			get appQuality(): string | undefined {
				checkProposedApiEnabled(extension, 'resolvers');
				return initData.quality;
			},
			get appCommit(): string | undefined {
				checkProposedApiEnabled(extension, 'resolvers');
				return initData.commit;
			},
			getDataChannel<T>(channelId: string): vibecoda.DataChannel<T> {
				checkProposedApiEnabled(extension, 'dataChannels');
				return extHostDataChannels.createDataChannel(extension, channelId);
			}
		};
		if (!initData.environment.extensionTestsLocationURI) {
			// allow to patch env-function when running tests
			Object.freeze(env);
		}

		// namespace: tests
		const tests: typeof vibecoda.tests = {
			createTestController(provider, label, refreshHandler?: (token: vibecoda.CancellationToken) => Thenable<void> | void) {
				return extHostTesting.createTestController(extension, provider, label, refreshHandler);
			},
			createTestObserver() {
				checkProposedApiEnabled(extension, 'testObserver');
				return extHostTesting.createTestObserver();
			},
			runTests(provider) {
				checkProposedApiEnabled(extension, 'testObserver');
				return extHostTesting.runTests(provider);
			},
			registerTestFollowupProvider(provider) {
				checkProposedApiEnabled(extension, 'testObserver');
				return extHostTesting.registerTestFollowupProvider(provider);
			},
			get onDidChangeTestResults() {
				checkProposedApiEnabled(extension, 'testObserver');
				return _asExtensionEvent(extHostTesting.onResultsChanged);
			},
			get testResults() {
				checkProposedApiEnabled(extension, 'testObserver');
				return extHostTesting.results;
			},
		};

		// namespace: extensions
		const extensionKind = initData.remote.isRemote
			? extHostTypes.ExtensionKind.Workspace
			: extHostTypes.ExtensionKind.UI;

		const extensions: typeof vibecoda.extensions = {
			getExtension(extensionId: string, includeFromDifferentExtensionHosts?: boolean): vibecoda.Extension<any> | undefined {
				if (!isProposedApiEnabled(extension, 'extensionsAny')) {
					includeFromDifferentExtensionHosts = false;
				}
				const mine = extensionInfo.mine.getExtensionDescription(extensionId);
				if (mine) {
					return new Extension(extensionService, extension.identifier, mine, extensionKind, false);
				}
				if (includeFromDifferentExtensionHosts) {
					const foreign = extensionInfo.all.getExtensionDescription(extensionId);
					if (foreign) {
						return new Extension(extensionService, extension.identifier, foreign, extensionKind /* TODO@alexdima THIS IS WRONG */, true);
					}
				}
				return undefined;
			},
			get all(): vibecoda.Extension<any>[] {
				const result: vibecoda.Extension<any>[] = [];
				for (const desc of extensionInfo.mine.getAllExtensionDescriptions()) {
					result.push(new Extension(extensionService, extension.identifier, desc, extensionKind, false));
				}
				return result;
			},
			get allAcrossExtensionHosts(): vibecoda.Extension<any>[] {
				checkProposedApiEnabled(extension, 'extensionsAny');
				const local = new ExtensionIdentifierSet(extensionInfo.mine.getAllExtensionDescriptions().map(desc => desc.identifier));
				const result: vibecoda.Extension<any>[] = [];
				for (const desc of extensionInfo.all.getAllExtensionDescriptions()) {
					const isFromDifferentExtensionHost = !local.has(desc.identifier);
					result.push(new Extension(extensionService, extension.identifier, desc, extensionKind /* TODO@alexdima THIS IS WRONG */, isFromDifferentExtensionHost));
				}
				return result;
			},
			get onDidChange() {
				if (isProposedApiEnabled(extension, 'extensionsAny')) {
					return _asExtensionEvent(Event.any(extensionInfo.mine.onDidChange, extensionInfo.all.onDidChange));
				}
				return _asExtensionEvent(extensionInfo.mine.onDidChange);
			}
		};

		// namespace: languages
		const languages: typeof vibecoda.languages = {
			createDiagnosticCollection(name?: string): vibecoda.DiagnosticCollection {
				return extHostDiagnostics.createDiagnosticCollection(extension.identifier, name);
			},
			get onDidChangeDiagnostics() {
				return _asExtensionEvent(extHostDiagnostics.onDidChangeDiagnostics);
			},
			getDiagnostics: (resource?: vibecoda.Uri) => {
				// eslint-disable-next-line local/code-no-any-casts
				return <any>extHostDiagnostics.getDiagnostics(resource);
			},
			getLanguages(): Thenable<string[]> {
				return extHostLanguages.getLanguages();
			},
			setTextDocumentLanguage(document: vibecoda.TextDocument, languageId: string): Thenable<vibecoda.TextDocument> {
				return extHostLanguages.changeLanguage(document.uri, languageId);
			},
			match(selector: vibecoda.DocumentSelector, document: vibecoda.TextDocument): number {
				const interalSelector = typeConverters.LanguageSelector.from(selector);
				let notebook: vibecoda.NotebookDocument | undefined;
				if (targetsNotebooks(interalSelector)) {
					notebook = extHostNotebook.notebookDocuments.find(value => value.apiNotebook.getCells().find(c => c.document === document))?.apiNotebook;
				}
				return score(interalSelector, document.uri, document.languageId, true, notebook?.uri, notebook?.notebookType);
			},
			registerCodeActionsProvider(selector: vibecoda.DocumentSelector, provider: vibecoda.CodeActionProvider, metadata?: vibecoda.CodeActionProviderMetadata): vibecoda.Disposable {
				return extHostLanguageFeatures.registerCodeActionProvider(extension, checkSelector(selector), provider, metadata);
			},
			registerDocumentPasteEditProvider(selector: vibecoda.DocumentSelector, provider: vibecoda.DocumentPasteEditProvider, metadata: vibecoda.DocumentPasteProviderMetadata): vibecoda.Disposable {
				return extHostLanguageFeatures.registerDocumentPasteEditProvider(extension, checkSelector(selector), provider, metadata);
			},
			registerCodeLensProvider(selector: vibecoda.DocumentSelector, provider: vibecoda.CodeLensProvider): vibecoda.Disposable {
				return extHostLanguageFeatures.registerCodeLensProvider(extension, checkSelector(selector), provider);
			},
			registerDefinitionProvider(selector: vibecoda.DocumentSelector, provider: vibecoda.DefinitionProvider): vibecoda.Disposable {
				return extHostLanguageFeatures.registerDefinitionProvider(extension, checkSelector(selector), provider);
			},
			registerDeclarationProvider(selector: vibecoda.DocumentSelector, provider: vibecoda.DeclarationProvider): vibecoda.Disposable {
				return extHostLanguageFeatures.registerDeclarationProvider(extension, checkSelector(selector), provider);
			},
			registerImplementationProvider(selector: vibecoda.DocumentSelector, provider: vibecoda.ImplementationProvider): vibecoda.Disposable {
				return extHostLanguageFeatures.registerImplementationProvider(extension, checkSelector(selector), provider);
			},
			registerTypeDefinitionProvider(selector: vibecoda.DocumentSelector, provider: vibecoda.TypeDefinitionProvider): vibecoda.Disposable {
				return extHostLanguageFeatures.registerTypeDefinitionProvider(extension, checkSelector(selector), provider);
			},
			registerHoverProvider(selector: vibecoda.DocumentSelector, provider: vibecoda.HoverProvider): vibecoda.Disposable {
				return extHostLanguageFeatures.registerHoverProvider(extension, checkSelector(selector), provider, extension.identifier);
			},
			registerEvaluatableExpressionProvider(selector: vibecoda.DocumentSelector, provider: vibecoda.EvaluatableExpressionProvider): vibecoda.Disposable {
				return extHostLanguageFeatures.registerEvaluatableExpressionProvider(extension, checkSelector(selector), provider, extension.identifier);
			},
			registerInlineValuesProvider(selector: vibecoda.DocumentSelector, provider: vibecoda.InlineValuesProvider): vibecoda.Disposable {
				return extHostLanguageFeatures.registerInlineValuesProvider(extension, checkSelector(selector), provider, extension.identifier);
			},
			registerDocumentHighlightProvider(selector: vibecoda.DocumentSelector, provider: vibecoda.DocumentHighlightProvider): vibecoda.Disposable {
				return extHostLanguageFeatures.registerDocumentHighlightProvider(extension, checkSelector(selector), provider);
			},
			registerMultiDocumentHighlightProvider(selector: vibecoda.DocumentSelector, provider: vibecoda.MultiDocumentHighlightProvider): vibecoda.Disposable {
				return extHostLanguageFeatures.registerMultiDocumentHighlightProvider(extension, checkSelector(selector), provider);
			},
			registerLinkedEditingRangeProvider(selector: vibecoda.DocumentSelector, provider: vibecoda.LinkedEditingRangeProvider): vibecoda.Disposable {
				return extHostLanguageFeatures.registerLinkedEditingRangeProvider(extension, checkSelector(selector), provider);
			},
			registerReferenceProvider(selector: vibecoda.DocumentSelector, provider: vibecoda.ReferenceProvider): vibecoda.Disposable {
				return extHostLanguageFeatures.registerReferenceProvider(extension, checkSelector(selector), provider);
			},
			registerRenameProvider(selector: vibecoda.DocumentSelector, provider: vibecoda.RenameProvider): vibecoda.Disposable {
				return extHostLanguageFeatures.registerRenameProvider(extension, checkSelector(selector), provider);
			},
			registerNewSymbolNamesProvider(selector: vibecoda.DocumentSelector, provider: vibecoda.NewSymbolNamesProvider): vibecoda.Disposable {
				checkProposedApiEnabled(extension, 'newSymbolNamesProvider');
				return extHostLanguageFeatures.registerNewSymbolNamesProvider(extension, checkSelector(selector), provider);
			},
			registerDocumentSymbolProvider(selector: vibecoda.DocumentSelector, provider: vibecoda.DocumentSymbolProvider, metadata?: vibecoda.DocumentSymbolProviderMetadata): vibecoda.Disposable {
				return extHostLanguageFeatures.registerDocumentSymbolProvider(extension, checkSelector(selector), provider, metadata);
			},
			registerWorkspaceSymbolProvider(provider: vibecoda.WorkspaceSymbolProvider): vibecoda.Disposable {
				return extHostLanguageFeatures.registerWorkspaceSymbolProvider(extension, provider);
			},
			registerDocumentFormattingEditProvider(selector: vibecoda.DocumentSelector, provider: vibecoda.DocumentFormattingEditProvider): vibecoda.Disposable {
				return extHostLanguageFeatures.registerDocumentFormattingEditProvider(extension, checkSelector(selector), provider);
			},
			registerDocumentRangeFormattingEditProvider(selector: vibecoda.DocumentSelector, provider: vibecoda.DocumentRangeFormattingEditProvider): vibecoda.Disposable {
				return extHostLanguageFeatures.registerDocumentRangeFormattingEditProvider(extension, checkSelector(selector), provider);
			},
			registerOnTypeFormattingEditProvider(selector: vibecoda.DocumentSelector, provider: vibecoda.OnTypeFormattingEditProvider, firstTriggerCharacter: string, ...moreTriggerCharacters: string[]): vibecoda.Disposable {
				return extHostLanguageFeatures.registerOnTypeFormattingEditProvider(extension, checkSelector(selector), provider, [firstTriggerCharacter].concat(moreTriggerCharacters));
			},
			registerDocumentSemanticTokensProvider(selector: vibecoda.DocumentSelector, provider: vibecoda.DocumentSemanticTokensProvider, legend: vibecoda.SemanticTokensLegend): vibecoda.Disposable {
				return extHostLanguageFeatures.registerDocumentSemanticTokensProvider(extension, checkSelector(selector), provider, legend);
			},
			registerDocumentRangeSemanticTokensProvider(selector: vibecoda.DocumentSelector, provider: vibecoda.DocumentRangeSemanticTokensProvider, legend: vibecoda.SemanticTokensLegend): vibecoda.Disposable {
				return extHostLanguageFeatures.registerDocumentRangeSemanticTokensProvider(extension, checkSelector(selector), provider, legend);
			},
			registerSignatureHelpProvider(selector: vibecoda.DocumentSelector, provider: vibecoda.SignatureHelpProvider, firstItem?: string | vibecoda.SignatureHelpProviderMetadata, ...remaining: string[]): vibecoda.Disposable {
				if (typeof firstItem === 'object') {
					return extHostLanguageFeatures.registerSignatureHelpProvider(extension, checkSelector(selector), provider, firstItem);
				}
				return extHostLanguageFeatures.registerSignatureHelpProvider(extension, checkSelector(selector), provider, typeof firstItem === 'undefined' ? [] : [firstItem, ...remaining]);
			},
			registerCompletionItemProvider(selector: vibecoda.DocumentSelector, provider: vibecoda.CompletionItemProvider, ...triggerCharacters: string[]): vibecoda.Disposable {
				return extHostLanguageFeatures.registerCompletionItemProvider(extension, checkSelector(selector), provider, triggerCharacters);
			},
			registerInlineCompletionItemProvider(selector: vibecoda.DocumentSelector, provider: vibecoda.InlineCompletionItemProvider, metadata?: vibecoda.InlineCompletionItemProviderMetadata): vibecoda.Disposable {
				if (provider.handleDidShowCompletionItem) {
					checkProposedApiEnabled(extension, 'inlineCompletionsAdditions');
				}
				if (provider.handleDidPartiallyAcceptCompletionItem) {
					checkProposedApiEnabled(extension, 'inlineCompletionsAdditions');
				}
				if (metadata) {
					checkProposedApiEnabled(extension, 'inlineCompletionsAdditions');
				}
				return extHostLanguageFeatures.registerInlineCompletionsProvider(extension, checkSelector(selector), provider, metadata);
			},
			get inlineCompletionsUnificationState() {
				checkProposedApiEnabled(extension, 'inlineCompletionsAdditions');
				return extHostLanguageFeatures.inlineCompletionsUnificationState;
			},
			onDidChangeCompletionsUnificationState(listener, thisArg?, disposables?) {
				checkProposedApiEnabled(extension, 'inlineCompletionsAdditions');
				return _asExtensionEvent(extHostLanguageFeatures.onDidChangeInlineCompletionsUnificationState)(listener, thisArg, disposables);
			},
			registerDocumentLinkProvider(selector: vibecoda.DocumentSelector, provider: vibecoda.DocumentLinkProvider): vibecoda.Disposable {
				return extHostLanguageFeatures.registerDocumentLinkProvider(extension, checkSelector(selector), provider);
			},
			registerColorProvider(selector: vibecoda.DocumentSelector, provider: vibecoda.DocumentColorProvider): vibecoda.Disposable {
				return extHostLanguageFeatures.registerColorProvider(extension, checkSelector(selector), provider);
			},
			registerFoldingRangeProvider(selector: vibecoda.DocumentSelector, provider: vibecoda.FoldingRangeProvider): vibecoda.Disposable {
				return extHostLanguageFeatures.registerFoldingRangeProvider(extension, checkSelector(selector), provider);
			},
			registerSelectionRangeProvider(selector: vibecoda.DocumentSelector, provider: vibecoda.SelectionRangeProvider): vibecoda.Disposable {
				return extHostLanguageFeatures.registerSelectionRangeProvider(extension, selector, provider);
			},
			registerCallHierarchyProvider(selector: vibecoda.DocumentSelector, provider: vibecoda.CallHierarchyProvider): vibecoda.Disposable {
				return extHostLanguageFeatures.registerCallHierarchyProvider(extension, selector, provider);
			},
			registerTypeHierarchyProvider(selector: vibecoda.DocumentSelector, provider: vibecoda.TypeHierarchyProvider): vibecoda.Disposable {
				return extHostLanguageFeatures.registerTypeHierarchyProvider(extension, selector, provider);
			},
			setLanguageConfiguration: (language: string, configuration: vibecoda.LanguageConfiguration): vibecoda.Disposable => {
				return extHostLanguageFeatures.setLanguageConfiguration(extension, language, configuration);
			},
			getTokenInformationAtPosition(doc: vibecoda.TextDocument, pos: vibecoda.Position) {
				checkProposedApiEnabled(extension, 'tokenInformation');
				return extHostLanguages.tokenAtPosition(doc, pos);
			},
			registerInlayHintsProvider(selector: vibecoda.DocumentSelector, provider: vibecoda.InlayHintsProvider): vibecoda.Disposable {
				return extHostLanguageFeatures.registerInlayHintsProvider(extension, selector, provider);
			},
			createLanguageStatusItem(id: string, selector: vibecoda.DocumentSelector): vibecoda.LanguageStatusItem {
				return extHostLanguages.createLanguageStatusItem(extension, id, selector);
			},
			registerDocumentDropEditProvider(selector: vibecoda.DocumentSelector, provider: vibecoda.DocumentDropEditProvider, metadata?: vibecoda.DocumentDropEditProviderMetadata): vibecoda.Disposable {
				return extHostLanguageFeatures.registerDocumentOnDropEditProvider(extension, selector, provider, metadata);
			}
		};

		// namespace: window
		const window: typeof vibecoda.window = {
			get activeTextEditor() {
				return extHostEditors.getActiveTextEditor();
			},
			get visibleTextEditors() {
				return extHostEditors.getVisibleTextEditors();
			},
			get activeTerminal() {
				return extHostTerminalService.activeTerminal;
			},
			get terminals() {
				return extHostTerminalService.terminals;
			},
			async showTextDocument(documentOrUri: vibecoda.TextDocument | vibecoda.Uri, columnOrOptions?: vibecoda.ViewColumn | vibecoda.TextDocumentShowOptions, preserveFocus?: boolean): Promise<vibecoda.TextEditor> {
				if (URI.isUri(documentOrUri) && documentOrUri.scheme === Schemas.vibecodaRemote && !documentOrUri.authority) {
					extHostApiDeprecation.report('workspace.showTextDocument', extension, `A URI of 'vibecoda-remote' scheme requires an authority.`);
				}
				const document = await (URI.isUri(documentOrUri)
					? Promise.resolve(workspace.openTextDocument(documentOrUri))
					: Promise.resolve(<vibecoda.TextDocument>documentOrUri));

				return extHostEditors.showTextDocument(document, columnOrOptions, preserveFocus);
			},
			createTextEditorDecorationType(options: vibecoda.DecorationRenderOptions): vibecoda.TextEditorDecorationType {
				return extHostEditors.createTextEditorDecorationType(extension, options);
			},
			onDidChangeActiveTextEditor(listener, thisArg?, disposables?) {
				return _asExtensionEvent(extHostEditors.onDidChangeActiveTextEditor)(listener, thisArg, disposables);
			},
			onDidChangeVisibleTextEditors(listener, thisArg, disposables) {
				return _asExtensionEvent(extHostEditors.onDidChangeVisibleTextEditors)(listener, thisArg, disposables);
			},
			onDidChangeTextEditorSelection(listener: (e: vibecoda.TextEditorSelectionChangeEvent) => any, thisArgs?: any, disposables?: extHostTypes.Disposable[]) {
				return _asExtensionEvent(extHostEditors.onDidChangeTextEditorSelection)(listener, thisArgs, disposables);
			},
			onDidChangeTextEditorOptions(listener: (e: vibecoda.TextEditorOptionsChangeEvent) => any, thisArgs?: any, disposables?: extHostTypes.Disposable[]) {
				return _asExtensionEvent(extHostEditors.onDidChangeTextEditorOptions)(listener, thisArgs, disposables);
			},
			onDidChangeTextEditorVisibleRanges(listener: (e: vibecoda.TextEditorVisibleRangesChangeEvent) => any, thisArgs?: any, disposables?: extHostTypes.Disposable[]) {
				return _asExtensionEvent(extHostEditors.onDidChangeTextEditorVisibleRanges)(listener, thisArgs, disposables);
			},
			onDidChangeTextEditorViewColumn(listener, thisArg?, disposables?) {
				return _asExtensionEvent(extHostEditors.onDidChangeTextEditorViewColumn)(listener, thisArg, disposables);
			},
			onDidChangeTextEditorDiffInformation(listener, thisArg?, disposables?) {
				checkProposedApiEnabled(extension, 'textEditorDiffInformation');
				return _asExtensionEvent(extHostEditors.onDidChangeTextEditorDiffInformation)(listener, thisArg, disposables);
			},
			onDidCloseTerminal(listener, thisArg?, disposables?) {
				return _asExtensionEvent(extHostTerminalService.onDidCloseTerminal)(listener, thisArg, disposables);
			},
			onDidOpenTerminal(listener, thisArg?, disposables?) {
				return _asExtensionEvent(extHostTerminalService.onDidOpenTerminal)(listener, thisArg, disposables);
			},
			onDidChangeActiveTerminal(listener, thisArg?, disposables?) {
				return _asExtensionEvent(extHostTerminalService.onDidChangeActiveTerminal)(listener, thisArg, disposables);
			},
			onDidChangeTerminalDimensions(listener, thisArg?, disposables?) {
				checkProposedApiEnabled(extension, 'terminalDimensions');
				return _asExtensionEvent(extHostTerminalService.onDidChangeTerminalDimensions)(listener, thisArg, disposables);
			},
			onDidChangeTerminalState(listener, thisArg?, disposables?) {
				return _asExtensionEvent(extHostTerminalService.onDidChangeTerminalState)(listener, thisArg, disposables);
			},
			onDidWriteTerminalData(listener, thisArg?, disposables?) {
				checkProposedApiEnabled(extension, 'terminalDataWriteEvent');
				return _asExtensionEvent(extHostTerminalService.onDidWriteTerminalData)(listener, thisArg, disposables);
			},
			onDidExecuteTerminalCommand(listener, thisArg?, disposables?) {
				checkProposedApiEnabled(extension, 'terminalExecuteCommandEvent');
				return _asExtensionEvent(extHostTerminalService.onDidExecuteTerminalCommand)(listener, thisArg, disposables);
			},
			onDidChangeTerminalShellIntegration(listener, thisArg?, disposables?) {
				return _asExtensionEvent(extHostTerminalShellIntegration.onDidChangeTerminalShellIntegration)(listener, thisArg, disposables);
			},
			onDidStartTerminalShellExecution(listener, thisArg?, disposables?) {
				return _asExtensionEvent(extHostTerminalShellIntegration.onDidStartTerminalShellExecution)(listener, thisArg, disposables);
			},
			onDidEndTerminalShellExecution(listener, thisArg?, disposables?) {
				return _asExtensionEvent(extHostTerminalShellIntegration.onDidEndTerminalShellExecution)(listener, thisArg, disposables);
			},
			get state() {
				return extHostWindow.getState();
			},
			onDidChangeWindowState(listener, thisArg?, disposables?) {
				return _asExtensionEvent(extHostWindow.onDidChangeWindowState)(listener, thisArg, disposables);
			},
			showInformationMessage(message: string, ...rest: Array<vibecoda.MessageOptions | string | vibecoda.MessageItem>) {
				return <Thenable<any>>extHostMessageService.showMessage(extension, Severity.Info, message, rest[0], <Array<string | vibecoda.MessageItem>>rest.slice(1));
			},
			showWarningMessage(message: string, ...rest: Array<vibecoda.MessageOptions | string | vibecoda.MessageItem>) {
				return <Thenable<any>>extHostMessageService.showMessage(extension, Severity.Warning, message, rest[0], <Array<string | vibecoda.MessageItem>>rest.slice(1));
			},
			showErrorMessage(message: string, ...rest: Array<vibecoda.MessageOptions | string | vibecoda.MessageItem>) {
				return <Thenable<any>>extHostMessageService.showMessage(extension, Severity.Error, message, rest[0], <Array<string | vibecoda.MessageItem>>rest.slice(1));
			},
			showQuickPick(items: any, options?: vibecoda.QuickPickOptions, token?: vibecoda.CancellationToken): any {
				return extHostQuickOpen.showQuickPick(extension, items, options, token);
			},
			showWorkspaceFolderPick(options?: vibecoda.WorkspaceFolderPickOptions) {
				return extHostQuickOpen.showWorkspaceFolderPick(options);
			},
			showInputBox(options?: vibecoda.InputBoxOptions, token?: vibecoda.CancellationToken) {
				return extHostQuickOpen.showInput(options, token);
			},
			showOpenDialog(options) {
				return extHostDialogs.showOpenDialog(options);
			},
			showSaveDialog(options) {
				return extHostDialogs.showSaveDialog(options);
			},
			createStatusBarItem(alignmentOrId?: vibecoda.StatusBarAlignment | string, priorityOrAlignment?: number | vibecoda.StatusBarAlignment, priorityArg?: number): vibecoda.StatusBarItem {
				let id: string | undefined;
				let alignment: number | undefined;
				let priority: number | undefined;

				if (typeof alignmentOrId === 'string') {
					id = alignmentOrId;
					alignment = priorityOrAlignment;
					priority = priorityArg;
				} else {
					alignment = alignmentOrId;
					priority = priorityOrAlignment;
				}

				return extHostStatusBar.createStatusBarEntry(extension, id, alignment, priority);
			},
			setStatusBarMessage(text: string, timeoutOrThenable?: number | Thenable<any>): vibecoda.Disposable {
				return extHostStatusBar.setStatusBarMessage(text, timeoutOrThenable);
			},
			withScmProgress<R>(task: (progress: vibecoda.Progress<number>) => Thenable<R>) {
				extHostApiDeprecation.report('window.withScmProgress', extension,
					`Use 'withProgress' instead.`);

				return extHostProgress.withProgress(extension, { location: extHostTypes.ProgressLocation.SourceControl }, (progress, token) => task({ report(n: number) { /*noop*/ } }));
			},
			withProgress<R>(options: vibecoda.ProgressOptions, task: (progress: vibecoda.Progress<{ message?: string; worked?: number }>, token: vibecoda.CancellationToken) => Thenable<R>) {
				return extHostProgress.withProgress(extension, options, task);
			},
			createOutputChannel(name: string, options: string | { log: true } | undefined): any {
				return extHostOutputService.createOutputChannel(name, options, extension);
			},
			createWebviewPanel(viewType: string, title: string, showOptions: vibecoda.ViewColumn | { viewColumn: vibecoda.ViewColumn; preserveFocus?: boolean }, options?: vibecoda.WebviewPanelOptions & vibecoda.WebviewOptions): vibecoda.WebviewPanel {
				return extHostWebviewPanels.createWebviewPanel(extension, viewType, title, showOptions, options);
			},
			createWebviewTextEditorInset(editor: vibecoda.TextEditor, line: number, height: number, options?: vibecoda.WebviewOptions): vibecoda.WebviewEditorInset {
				checkProposedApiEnabled(extension, 'editorInsets');
				return extHostEditorInsets.createWebviewEditorInset(editor, line, height, options, extension);
			},
			createTerminal(nameOrOptions?: vibecoda.TerminalOptions | vibecoda.ExtensionTerminalOptions | string, shellPath?: string, shellArgs?: readonly string[] | string): vibecoda.Terminal {
				if (typeof nameOrOptions === 'object') {
					if ('pty' in nameOrOptions) {
						return extHostTerminalService.createExtensionTerminal(nameOrOptions);
					}
					return extHostTerminalService.createTerminalFromOptions(nameOrOptions);
				}
				return extHostTerminalService.createTerminal(nameOrOptions, shellPath, shellArgs);
			},
			registerTerminalLinkProvider(provider: vibecoda.TerminalLinkProvider): vibecoda.Disposable {
				return extHostTerminalService.registerLinkProvider(provider);
			},
			registerTerminalProfileProvider(id: string, provider: vibecoda.TerminalProfileProvider): vibecoda.Disposable {
				return extHostTerminalService.registerProfileProvider(extension, id, provider);
			},
			registerTerminalCompletionProvider(provider: vibecoda.TerminalCompletionProvider<vibecoda.TerminalCompletionItem>, ...triggerCharacters: string[]): vibecoda.Disposable {
				return extHostTerminalService.registerTerminalCompletionProvider(extension, provider, ...triggerCharacters);
			},
			registerTerminalQuickFixProvider(id: string, provider: vibecoda.TerminalQuickFixProvider): vibecoda.Disposable {
				checkProposedApiEnabled(extension, 'terminalQuickFixProvider');
				return extHostTerminalService.registerTerminalQuickFixProvider(id, extension.identifier.value, provider);
			},
			registerTreeDataProvider(viewId: string, treeDataProvider: vibecoda.TreeDataProvider<any>): vibecoda.Disposable {
				return extHostTreeViews.registerTreeDataProvider(viewId, treeDataProvider, extension);
			},
			createTreeView(viewId: string, options: { treeDataProvider: vibecoda.TreeDataProvider<any> }): vibecoda.TreeView<any> {
				return extHostTreeViews.createTreeView(viewId, options, extension);
			},
			registerWebviewPanelSerializer: (viewType: string, serializer: vibecoda.WebviewPanelSerializer) => {
				return extHostWebviewPanels.registerWebviewPanelSerializer(extension, viewType, serializer);
			},
			registerCustomEditorProvider: (viewType: string, provider: vibecoda.CustomTextEditorProvider | vibecoda.CustomReadonlyEditorProvider, options: { webviewOptions?: vibecoda.WebviewPanelOptions; supportsMultipleEditorsPerDocument?: boolean } = {}) => {
				return extHostCustomEditors.registerCustomEditorProvider(extension, viewType, provider, options);
			},
			registerFileDecorationProvider(provider: vibecoda.FileDecorationProvider) {
				return extHostDecorations.registerFileDecorationProvider(provider, extension);
			},
			registerUriHandler(handler: vibecoda.UriHandler) {
				return extHostUrls.registerUriHandler(extension, handler);
			},
			createQuickPick<T extends vibecoda.QuickPickItem>(): vibecoda.QuickPick<T> {
				return extHostQuickOpen.createQuickPick(extension);
			},
			createInputBox(): vibecoda.InputBox {
				return extHostQuickOpen.createInputBox(extension);
			},
			get activeColorTheme(): vibecoda.ColorTheme {
				return extHostTheming.activeColorTheme;
			},
			onDidChangeActiveColorTheme(listener, thisArg?, disposables?) {
				return _asExtensionEvent(extHostTheming.onDidChangeActiveColorTheme)(listener, thisArg, disposables);
			},
			registerWebviewViewProvider(viewId: string, provider: vibecoda.WebviewViewProvider, options?: {
				webviewOptions?: {
					retainContextWhenHidden?: boolean;
				};
			}) {
				return extHostWebviewViews.registerWebviewViewProvider(extension, viewId, provider, options?.webviewOptions);
			},
			get activeNotebookEditor(): vibecoda.NotebookEditor | undefined {
				return extHostNotebook.activeNotebookEditor;
			},
			onDidChangeActiveNotebookEditor(listener, thisArgs?, disposables?) {
				return _asExtensionEvent(extHostNotebook.onDidChangeActiveNotebookEditor)(listener, thisArgs, disposables);
			},
			get visibleNotebookEditors() {
				return extHostNotebook.visibleNotebookEditors;
			},
			get onDidChangeVisibleNotebookEditors() {
				return _asExtensionEvent(extHostNotebook.onDidChangeVisibleNotebookEditors);
			},
			onDidChangeNotebookEditorSelection(listener, thisArgs?, disposables?) {
				return _asExtensionEvent(extHostNotebookEditors.onDidChangeNotebookEditorSelection)(listener, thisArgs, disposables);
			},
			onDidChangeNotebookEditorVisibleRanges(listener, thisArgs?, disposables?) {
				return _asExtensionEvent(extHostNotebookEditors.onDidChangeNotebookEditorVisibleRanges)(listener, thisArgs, disposables);
			},
			showNotebookDocument(document, options?) {
				return extHostNotebook.showNotebookDocument(document, options);
			},
			registerExternalUriOpener(id: string, opener: vibecoda.ExternalUriOpener, metadata: vibecoda.ExternalUriOpenerMetadata) {
				checkProposedApiEnabled(extension, 'externalUriOpener');
				return extHostUriOpeners.registerExternalUriOpener(extension.identifier, id, opener, metadata);
			},
			registerProfileContentHandler(id: string, handler: vibecoda.ProfileContentHandler) {
				checkProposedApiEnabled(extension, 'profileContentHandlers');
				return extHostProfileContentHandlers.registerProfileContentHandler(extension, id, handler);
			},
			registerQuickDiffProvider(selector: vibecoda.DocumentSelector, quickDiffProvider: vibecoda.QuickDiffProvider, id: string, label: string, rootUri?: vibecoda.Uri): vibecoda.Disposable {
				checkProposedApiEnabled(extension, 'quickDiffProvider');
				return extHostQuickDiff.registerQuickDiffProvider(extension, checkSelector(selector), quickDiffProvider, id, label, rootUri);
			},
			get tabGroups(): vibecoda.TabGroups {
				return extHostEditorTabs.tabGroups;
			},
			registerShareProvider(selector: vibecoda.DocumentSelector, provider: vibecoda.ShareProvider): vibecoda.Disposable {
				checkProposedApiEnabled(extension, 'shareProvider');
				return extHostShare.registerShareProvider(checkSelector(selector), provider);
			},
			get nativeHandle(): Uint8Array | undefined {
				checkProposedApiEnabled(extension, 'nativeWindowHandle');
				return extHostWindow.nativeHandle;
			},
			createChatStatusItem: (id: string) => {
				checkProposedApiEnabled(extension, 'chatStatusItem');
				return extHostChatStatus.createChatStatusItem(extension, id);
			},
		};

		// namespace: workspace

		const workspace: typeof vibecoda.workspace = {
			get rootPath() {
				extHostApiDeprecation.report('workspace.rootPath', extension,
					`Please use 'workspace.workspaceFolders' instead. More details: https://aka.ms/vibecoda-eliminating-rootpath`);

				return extHostWorkspace.getPath();
			},
			set rootPath(value) {
				throw new errors.ReadonlyError('rootPath');
			},
			getWorkspaceFolder(resource) {
				return extHostWorkspace.getWorkspaceFolder(resource);
			},
			get workspaceFolders() {
				return extHostWorkspace.getWorkspaceFolders();
			},
			get name() {
				return extHostWorkspace.name;
			},
			set name(value) {
				throw new errors.ReadonlyError('name');
			},
			get workspaceFile() {
				return extHostWorkspace.workspaceFile;
			},
			set workspaceFile(value) {
				throw new errors.ReadonlyError('workspaceFile');
			},
			updateWorkspaceFolders: (index, deleteCount, ...workspaceFoldersToAdd) => {
				return extHostWorkspace.updateWorkspaceFolders(extension, index, deleteCount || 0, ...workspaceFoldersToAdd);
			},
			onDidChangeWorkspaceFolders: function (listener, thisArgs?, disposables?) {
				return _asExtensionEvent(extHostWorkspace.onDidChangeWorkspace)(listener, thisArgs, disposables);
			},
			asRelativePath: (pathOrUri, includeWorkspace?) => {
				return extHostWorkspace.getRelativePath(pathOrUri, includeWorkspace);
			},
			findFiles: (include, exclude, maxResults?, token?) => {
				// Note, undefined/null have different meanings on "exclude"
				return extHostWorkspace.findFiles(include, exclude, maxResults, extension.identifier, token);
			},
			findFiles2: (filePattern: vibecoda.GlobPattern[], options?: vibecoda.FindFiles2Options, token?: vibecoda.CancellationToken): Thenable<vibecoda.Uri[]> => {
				checkProposedApiEnabled(extension, 'findFiles2');
				return extHostWorkspace.findFiles2(filePattern, options, extension.identifier, token);
			},
			findTextInFiles: (query: vibecoda.TextSearchQuery, optionsOrCallback: vibecoda.FindTextInFilesOptions | ((result: vibecoda.TextSearchResult) => void), callbackOrToken?: vibecoda.CancellationToken | ((result: vibecoda.TextSearchResult) => void), token?: vibecoda.CancellationToken) => {
				checkProposedApiEnabled(extension, 'findTextInFiles');
				let options: vibecoda.FindTextInFilesOptions;
				let callback: (result: vibecoda.TextSearchResult) => void;

				if (typeof optionsOrCallback === 'object') {
					options = optionsOrCallback;
					callback = callbackOrToken as (result: vibecoda.TextSearchResult) => void;
				} else {
					options = {};
					callback = optionsOrCallback;
					token = callbackOrToken as vibecoda.CancellationToken;
				}

				return extHostWorkspace.findTextInFiles(query, options || {}, callback, extension.identifier, token);
			},
			findTextInFiles2: (query: vibecoda.TextSearchQuery2, options?: vibecoda.FindTextInFilesOptions2, token?: vibecoda.CancellationToken): vibecoda.FindTextInFilesResponse => {
				checkProposedApiEnabled(extension, 'findTextInFiles2');
				checkProposedApiEnabled(extension, 'textSearchProvider2');
				return extHostWorkspace.findTextInFiles2(query, options, extension.identifier, token);
			},
			save: (uri) => {
				return extHostWorkspace.save(uri);
			},
			saveAs: (uri) => {
				return extHostWorkspace.saveAs(uri);
			},
			saveAll: (includeUntitled?) => {
				return extHostWorkspace.saveAll(includeUntitled);
			},
			applyEdit(edit: vibecoda.WorkspaceEdit, metadata?: vibecoda.WorkspaceEditMetadata): Thenable<boolean> {
				return extHostBulkEdits.applyWorkspaceEdit(edit, extension, metadata);
			},
			createFileSystemWatcher: (pattern, optionsOrIgnoreCreate, ignoreChange?, ignoreDelete?): vibecoda.FileSystemWatcher => {
				const options: FileSystemWatcherCreateOptions = {
					ignoreCreateEvents: Boolean(optionsOrIgnoreCreate),
					ignoreChangeEvents: Boolean(ignoreChange),
					ignoreDeleteEvents: Boolean(ignoreDelete),
				};

				return extHostFileSystemEvent.createFileSystemWatcher(extHostWorkspace, configProvider, extension, pattern, options);
			},
			get textDocuments() {
				return extHostDocuments.getAllDocumentData().map(data => data.document);
			},
			set textDocuments(value) {
				throw new errors.ReadonlyError('textDocuments');
			},
			openTextDocument(uriOrFileNameOrOptions?: vibecoda.Uri | string | { language?: string; content?: string; encoding?: string }, options?: { encoding?: string }) {
				let uriPromise: Thenable<URI>;

				options = (options ?? uriOrFileNameOrOptions) as ({ language?: string; content?: string; encoding?: string } | undefined);

				if (typeof uriOrFileNameOrOptions === 'string') {
					uriPromise = Promise.resolve(URI.file(uriOrFileNameOrOptions));
				} else if (URI.isUri(uriOrFileNameOrOptions)) {
					uriPromise = Promise.resolve(uriOrFileNameOrOptions);
				} else if (!options || typeof options === 'object') {
					uriPromise = extHostDocuments.createDocumentData(options);
				} else {
					throw new Error('illegal argument - uriOrFileNameOrOptions');
				}

				return uriPromise.then(uri => {
					extHostLogService.trace(`openTextDocument from ${extension.identifier}`);
					if (uri.scheme === Schemas.vibecodaRemote && !uri.authority) {
						extHostApiDeprecation.report('workspace.openTextDocument', extension, `A URI of 'vibecoda-remote' scheme requires an authority.`);
					}
					return extHostDocuments.ensureDocumentData(uri, options).then(documentData => {
						return documentData.document;
					});
				});
			},
			onDidOpenTextDocument: (listener, thisArgs?, disposables?) => {
				return _asExtensionEvent(extHostDocuments.onDidAddDocument)(listener, thisArgs, disposables);
			},
			onDidCloseTextDocument: (listener, thisArgs?, disposables?) => {
				return _asExtensionEvent(extHostDocuments.onDidRemoveDocument)(listener, thisArgs, disposables);
			},
			onDidChangeTextDocument: (listener, thisArgs?, disposables?) => {
				if (isProposedApiEnabled(extension, 'textDocumentChangeReason')) {
					return _asExtensionEvent(extHostDocuments.onDidChangeDocumentWithReason)(listener, thisArgs, disposables);
				}
				return _asExtensionEvent(extHostDocuments.onDidChangeDocument)(listener, thisArgs, disposables);
			},
			onDidSaveTextDocument: (listener, thisArgs?, disposables?) => {
				return _asExtensionEvent(extHostDocuments.onDidSaveDocument)(listener, thisArgs, disposables);
			},
			onWillSaveTextDocument: (listener, thisArgs?, disposables?) => {
				return _asExtensionEvent(extHostDocumentSaveParticipant.getOnWillSaveTextDocumentEvent(extension))(listener, thisArgs, disposables);
			},
			get notebookDocuments(): vibecoda.NotebookDocument[] {
				return extHostNotebook.notebookDocuments.map(d => d.apiNotebook);
			},
			async openNotebookDocument(uriOrType?: URI | string, content?: vibecoda.NotebookData) {
				let uri: URI;
				if (URI.isUri(uriOrType)) {
					uri = uriOrType;
					await extHostNotebook.openNotebookDocument(uriOrType);
				} else if (typeof uriOrType === 'string') {
					uri = URI.revive(await extHostNotebook.createNotebookDocument({ viewType: uriOrType, content }));
				} else {
					throw new Error('Invalid arguments');
				}
				return extHostNotebook.getNotebookDocument(uri).apiNotebook;
			},
			onDidSaveNotebookDocument(listener, thisArg, disposables) {
				return _asExtensionEvent(extHostNotebookDocuments.onDidSaveNotebookDocument)(listener, thisArg, disposables);
			},
			onDidChangeNotebookDocument(listener, thisArg, disposables) {
				return _asExtensionEvent(extHostNotebookDocuments.onDidChangeNotebookDocument)(listener, thisArg, disposables);
			},
			onWillSaveNotebookDocument(listener, thisArg, disposables) {
				return _asExtensionEvent(extHostNotebookDocumentSaveParticipant.getOnWillSaveNotebookDocumentEvent(extension))(listener, thisArg, disposables);
			},
			get onDidOpenNotebookDocument() {
				return _asExtensionEvent(extHostNotebook.onDidOpenNotebookDocument);
			},
			get onDidCloseNotebookDocument() {
				return _asExtensionEvent(extHostNotebook.onDidCloseNotebookDocument);
			},
			registerNotebookSerializer(viewType: string, serializer: vibecoda.NotebookSerializer, options?: vibecoda.NotebookDocumentContentOptions, registration?: vibecoda.NotebookRegistrationData) {
				return extHostNotebook.registerNotebookSerializer(extension, viewType, serializer, options, isProposedApiEnabled(extension, 'notebookLiveShare') ? registration : undefined);
			},
			onDidChangeConfiguration: (listener: (_: any) => any, thisArgs?: any, disposables?: extHostTypes.Disposable[]) => {
				return _asExtensionEvent(configProvider.onDidChangeConfiguration)(listener, thisArgs, disposables);
			},
			getConfiguration(section?: string, scope?: vibecoda.ConfigurationScope | null): vibecoda.WorkspaceConfiguration {
				scope = arguments.length === 1 ? undefined : scope;
				return configProvider.getConfiguration(section, scope, extension);
			},
			registerTextDocumentContentProvider(scheme: string, provider: vibecoda.TextDocumentContentProvider) {
				return extHostDocumentContentProviders.registerTextDocumentContentProvider(scheme, provider);
			},
			registerTaskProvider: (type: string, provider: vibecoda.TaskProvider) => {
				extHostApiDeprecation.report('window.registerTaskProvider', extension,
					`Use the corresponding function on the 'tasks' namespace instead`);

				return extHostTask.registerTaskProvider(extension, type, provider);
			},
			registerFileSystemProvider(scheme, provider, options) {
				return combinedDisposable(
					extHostFileSystem.registerFileSystemProvider(extension, scheme, provider, options),
					extHostConsumerFileSystem.addFileSystemProvider(scheme, provider, options)
				);
			},
			get fs() {
				return extHostConsumerFileSystem.value;
			},
			registerFileSearchProvider: (scheme: string, provider: vibecoda.FileSearchProvider) => {
				checkProposedApiEnabled(extension, 'fileSearchProvider');
				return extHostSearch.registerFileSearchProviderOld(scheme, provider);
			},
			registerTextSearchProvider: (scheme: string, provider: vibecoda.TextSearchProvider) => {
				checkProposedApiEnabled(extension, 'textSearchProvider');
				return extHostSearch.registerTextSearchProviderOld(scheme, provider);
			},
			registerAITextSearchProvider: (scheme: string, provider: vibecoda.AITextSearchProvider) => {
				// there are some dependencies on textSearchProvider, so we need to check for both
				checkProposedApiEnabled(extension, 'aiTextSearchProvider');
				checkProposedApiEnabled(extension, 'textSearchProvider2');
				return extHostSearch.registerAITextSearchProvider(scheme, provider);
			},
			registerFileSearchProvider2: (scheme: string, provider: vibecoda.FileSearchProvider2) => {
				checkProposedApiEnabled(extension, 'fileSearchProvider2');
				return extHostSearch.registerFileSearchProvider(scheme, provider);
			},
			registerTextSearchProvider2: (scheme: string, provider: vibecoda.TextSearchProvider2) => {
				checkProposedApiEnabled(extension, 'textSearchProvider2');
				return extHostSearch.registerTextSearchProvider(scheme, provider);
			},
			registerRemoteAuthorityResolver: (authorityPrefix: string, resolver: vibecoda.RemoteAuthorityResolver) => {
				checkProposedApiEnabled(extension, 'resolvers');
				return extensionService.registerRemoteAuthorityResolver(authorityPrefix, resolver);
			},
			registerResourceLabelFormatter: (formatter: vibecoda.ResourceLabelFormatter) => {
				checkProposedApiEnabled(extension, 'resolvers');
				return extHostLabelService.$registerResourceLabelFormatter(formatter);
			},
			getRemoteExecServer: (authority: string) => {
				checkProposedApiEnabled(extension, 'resolvers');
				return extensionService.getRemoteExecServer(authority);
			},
			onDidCreateFiles: (listener, thisArg, disposables) => {
				return _asExtensionEvent(extHostFileSystemEvent.onDidCreateFile)(listener, thisArg, disposables);
			},
			onDidDeleteFiles: (listener, thisArg, disposables) => {
				return _asExtensionEvent(extHostFileSystemEvent.onDidDeleteFile)(listener, thisArg, disposables);
			},
			onDidRenameFiles: (listener, thisArg, disposables) => {
				return _asExtensionEvent(extHostFileSystemEvent.onDidRenameFile)(listener, thisArg, disposables);
			},
			onWillCreateFiles: (listener: (e: vibecoda.FileWillCreateEvent) => any, thisArg?: any, disposables?: vibecoda.Disposable[]) => {
				return _asExtensionEvent(extHostFileSystemEvent.getOnWillCreateFileEvent(extension))(listener, thisArg, disposables);
			},
			onWillDeleteFiles: (listener: (e: vibecoda.FileWillDeleteEvent) => any, thisArg?: any, disposables?: vibecoda.Disposable[]) => {
				return _asExtensionEvent(extHostFileSystemEvent.getOnWillDeleteFileEvent(extension))(listener, thisArg, disposables);
			},
			onWillRenameFiles: (listener: (e: vibecoda.FileWillRenameEvent) => any, thisArg?: any, disposables?: vibecoda.Disposable[]) => {
				return _asExtensionEvent(extHostFileSystemEvent.getOnWillRenameFileEvent(extension))(listener, thisArg, disposables);
			},
			openTunnel: (forward: vibecoda.TunnelOptions) => {
				checkProposedApiEnabled(extension, 'tunnels');
				return extHostTunnelService.openTunnel(extension, forward).then(value => {
					if (!value) {
						throw new Error('cannot open tunnel');
					}
					return value;
				});
			},
			get tunnels() {
				checkProposedApiEnabled(extension, 'tunnels');
				return extHostTunnelService.getTunnels();
			},
			onDidChangeTunnels: (listener, thisArg?, disposables?) => {
				checkProposedApiEnabled(extension, 'tunnels');
				return _asExtensionEvent(extHostTunnelService.onDidChangeTunnels)(listener, thisArg, disposables);
			},
			registerPortAttributesProvider: (portSelector: vibecoda.PortAttributesSelector, provider: vibecoda.PortAttributesProvider) => {
				checkProposedApiEnabled(extension, 'portsAttributes');
				return extHostTunnelService.registerPortsAttributesProvider(portSelector, provider);
			},
			registerTunnelProvider: (tunnelProvider: vibecoda.TunnelProvider, information: vibecoda.TunnelInformation) => {
				checkProposedApiEnabled(extension, 'tunnelFactory');
				return extHostTunnelService.registerTunnelProvider(tunnelProvider, information);
			},
			registerTimelineProvider: (scheme: string | string[], provider: vibecoda.TimelineProvider) => {
				checkProposedApiEnabled(extension, 'timeline');
				return extHostTimeline.registerTimelineProvider(scheme, provider, extension.identifier, extHostCommands.converter);
			},
			get isTrusted() {
				return extHostWorkspace.trusted;
			},
			requestWorkspaceTrust: (options?: vibecoda.WorkspaceTrustRequestOptions) => {
				checkProposedApiEnabled(extension, 'workspaceTrust');
				return extHostWorkspace.requestWorkspaceTrust(options);
			},
			onDidGrantWorkspaceTrust: (listener, thisArgs?, disposables?) => {
				return _asExtensionEvent(extHostWorkspace.onDidGrantWorkspaceTrust)(listener, thisArgs, disposables);
			},
			registerEditSessionIdentityProvider: (scheme: string, provider: vibecoda.EditSessionIdentityProvider) => {
				checkProposedApiEnabled(extension, 'editSessionIdentityProvider');
				return extHostWorkspace.registerEditSessionIdentityProvider(scheme, provider);
			},
			onWillCreateEditSessionIdentity: (listener, thisArgs?, disposables?) => {
				checkProposedApiEnabled(extension, 'editSessionIdentityProvider');
				return _asExtensionEvent(extHostWorkspace.getOnWillCreateEditSessionIdentityEvent(extension))(listener, thisArgs, disposables);
			},
			registerCanonicalUriProvider: (scheme: string, provider: vibecoda.CanonicalUriProvider) => {
				checkProposedApiEnabled(extension, 'canonicalUriProvider');
				return extHostWorkspace.registerCanonicalUriProvider(scheme, provider);
			},
			getCanonicalUri: (uri: vibecoda.Uri, options: vibecoda.CanonicalUriRequestOptions, token: vibecoda.CancellationToken) => {
				checkProposedApiEnabled(extension, 'canonicalUriProvider');
				return extHostWorkspace.provideCanonicalUri(uri, options, token);
			},
			decode(content: Uint8Array, options?: { uri?: vibecoda.Uri; encoding?: string }) {
				return extHostWorkspace.decode(content, options);
			},
			encode(content: string, options?: { uri?: vibecoda.Uri; encoding?: string }) {
				return extHostWorkspace.encode(content, options);
			},
		};

		// namespace: scm
		const scm: typeof vibecoda.scm = {
			get inputBox() {
				extHostApiDeprecation.report('scm.inputBox', extension,
					`Use 'SourceControl.inputBox' instead`);

				return extHostSCM.getLastInputBox(extension)!; // Strict null override - Deprecated api
			},
			createSourceControl(id: string, label: string, rootUri?: vibecoda.Uri, iconPath?: vibecoda.IconPath, parent?: vibecoda.SourceControl): vibecoda.SourceControl {
				if (iconPath || parent) {
					checkProposedApiEnabled(extension, 'scmProviderOptions');
				}
				return extHostSCM.createSourceControl(extension, id, label, rootUri, iconPath, parent);
			}
		};

		// namespace: comments
		const comments: typeof vibecoda.comments = {
			createCommentController(id: string, label: string) {
				return extHostComment.createCommentController(extension, id, label);
			}
		};

		// namespace: debug
		const debug: typeof vibecoda.debug = {
			get activeDebugSession() {
				return extHostDebugService.activeDebugSession;
			},
			get activeDebugConsole() {
				return extHostDebugService.activeDebugConsole;
			},
			get breakpoints() {
				return extHostDebugService.breakpoints;
			},
			get activeStackItem() {
				return extHostDebugService.activeStackItem;
			},
			registerDebugVisualizationProvider(id, provider) {
				checkProposedApiEnabled(extension, 'debugVisualization');
				return extHostDebugService.registerDebugVisualizationProvider(extension, id, provider);
			},
			registerDebugVisualizationTreeProvider(id, provider) {
				checkProposedApiEnabled(extension, 'debugVisualization');
				return extHostDebugService.registerDebugVisualizationTree(extension, id, provider);
			},
			onDidStartDebugSession(listener, thisArg?, disposables?) {
				return _asExtensionEvent(extHostDebugService.onDidStartDebugSession)(listener, thisArg, disposables);
			},
			onDidTerminateDebugSession(listener, thisArg?, disposables?) {
				return _asExtensionEvent(extHostDebugService.onDidTerminateDebugSession)(listener, thisArg, disposables);
			},
			onDidChangeActiveDebugSession(listener, thisArg?, disposables?) {
				return _asExtensionEvent(extHostDebugService.onDidChangeActiveDebugSession)(listener, thisArg, disposables);
			},
			onDidReceiveDebugSessionCustomEvent(listener, thisArg?, disposables?) {
				return _asExtensionEvent(extHostDebugService.onDidReceiveDebugSessionCustomEvent)(listener, thisArg, disposables);
			},
			onDidChangeBreakpoints(listener, thisArgs?, disposables?) {
				return _asExtensionEvent(extHostDebugService.onDidChangeBreakpoints)(listener, thisArgs, disposables);
			},
			onDidChangeActiveStackItem(listener, thisArg?, disposables?) {
				return _asExtensionEvent(extHostDebugService.onDidChangeActiveStackItem)(listener, thisArg, disposables);
			},
			registerDebugConfigurationProvider(debugType: string, provider: vibecoda.DebugConfigurationProvider, triggerKind?: vibecoda.DebugConfigurationProviderTriggerKind) {
				return extHostDebugService.registerDebugConfigurationProvider(debugType, provider, triggerKind || DebugConfigurationProviderTriggerKind.Initial);
			},
			registerDebugAdapterDescriptorFactory(debugType: string, factory: vibecoda.DebugAdapterDescriptorFactory) {
				return extHostDebugService.registerDebugAdapterDescriptorFactory(extension, debugType, factory);
			},
			registerDebugAdapterTrackerFactory(debugType: string, factory: vibecoda.DebugAdapterTrackerFactory) {
				return extHostDebugService.registerDebugAdapterTrackerFactory(debugType, factory);
			},
			startDebugging(folder: vibecoda.WorkspaceFolder | undefined, nameOrConfig: string | vibecoda.DebugConfiguration, parentSessionOrOptions?: vibecoda.DebugSession | vibecoda.DebugSessionOptions) {
				if (!parentSessionOrOptions || (typeof parentSessionOrOptions === 'object' && 'configuration' in parentSessionOrOptions)) {
					return extHostDebugService.startDebugging(folder, nameOrConfig, { parentSession: parentSessionOrOptions });
				}
				return extHostDebugService.startDebugging(folder, nameOrConfig, parentSessionOrOptions || {});
			},
			stopDebugging(session?: vibecoda.DebugSession) {
				return extHostDebugService.stopDebugging(session);
			},
			addBreakpoints(breakpoints: readonly vibecoda.Breakpoint[]) {
				return extHostDebugService.addBreakpoints(breakpoints);
			},
			removeBreakpoints(breakpoints: readonly vibecoda.Breakpoint[]) {
				return extHostDebugService.removeBreakpoints(breakpoints);
			},
			asDebugSourceUri(source: vibecoda.DebugProtocolSource, session?: vibecoda.DebugSession): vibecoda.Uri {
				return extHostDebugService.asDebugSourceUri(source, session);
			}
		};

		const tasks: typeof vibecoda.tasks = {
			registerTaskProvider: (type: string, provider: vibecoda.TaskProvider) => {
				return extHostTask.registerTaskProvider(extension, type, provider);
			},
			fetchTasks: (filter?: vibecoda.TaskFilter): Thenable<vibecoda.Task[]> => {
				return extHostTask.fetchTasks(filter);
			},
			executeTask: (task: vibecoda.Task): Thenable<vibecoda.TaskExecution> => {
				return extHostTask.executeTask(extension, task);
			},
			get taskExecutions(): vibecoda.TaskExecution[] {
				return extHostTask.taskExecutions;
			},
			onDidStartTask: (listener: (e: vibecoda.TaskStartEvent) => any, thisArgs?: any, disposables?) => {
				const wrappedListener = (event: vibecoda.TaskStartEvent) => {
					if (!isProposedApiEnabled(extension, 'taskExecutionTerminal')) {
						if (event?.execution?.terminal !== undefined) {
							event.execution.terminal = undefined;
						}
					}
					const eventWithExecution = {
						...event,
						execution: event.execution
					};
					return listener.call(thisArgs, eventWithExecution);
				};
				return _asExtensionEvent(extHostTask.onDidStartTask)(wrappedListener, thisArgs, disposables);
			},
			onDidEndTask: (listeners, thisArgs?, disposables?) => {
				return _asExtensionEvent(extHostTask.onDidEndTask)(listeners, thisArgs, disposables);
			},
			onDidStartTaskProcess: (listeners, thisArgs?, disposables?) => {
				return _asExtensionEvent(extHostTask.onDidStartTaskProcess)(listeners, thisArgs, disposables);
			},
			onDidEndTaskProcess: (listeners, thisArgs?, disposables?) => {
				return _asExtensionEvent(extHostTask.onDidEndTaskProcess)(listeners, thisArgs, disposables);
			},
			onDidStartTaskProblemMatchers: (listeners, thisArgs?, disposables?) => {
				checkProposedApiEnabled(extension, 'taskProblemMatcherStatus');
				return _asExtensionEvent(extHostTask.onDidStartTaskProblemMatchers)(listeners, thisArgs, disposables);
			},
			onDidEndTaskProblemMatchers: (listeners, thisArgs?, disposables?) => {
				checkProposedApiEnabled(extension, 'taskProblemMatcherStatus');
				return _asExtensionEvent(extHostTask.onDidEndTaskProblemMatchers)(listeners, thisArgs, disposables);
			}
		};

		// namespace: notebook
		const notebooks: typeof vibecoda.notebooks = {
			createNotebookController(id: string, notebookType: string, label: string, handler?, rendererScripts?: vibecoda.NotebookRendererScript[]) {
				return extHostNotebookKernels.createNotebookController(extension, id, notebookType, label, handler, isProposedApiEnabled(extension, 'notebookMessaging') ? rendererScripts : undefined);
			},
			registerNotebookCellStatusBarItemProvider: (notebookType: string, provider: vibecoda.NotebookCellStatusBarItemProvider) => {
				return extHostNotebook.registerNotebookCellStatusBarItemProvider(extension, notebookType, provider);
			},
			createRendererMessaging(rendererId) {
				return extHostNotebookRenderers.createRendererMessaging(extension, rendererId);
			},
			createNotebookControllerDetectionTask(notebookType: string) {
				checkProposedApiEnabled(extension, 'notebookKernelSource');
				return extHostNotebookKernels.createNotebookControllerDetectionTask(extension, notebookType);
			},
			registerKernelSourceActionProvider(notebookType: string, provider: vibecoda.NotebookKernelSourceActionProvider) {
				checkProposedApiEnabled(extension, 'notebookKernelSource');
				return extHostNotebookKernels.registerKernelSourceActionProvider(extension, notebookType, provider);
			},
		};

		// namespace: l10n
		const l10n: typeof vibecoda.l10n = {
			t(...params: [message: string, ...args: Array<string | number | boolean>] | [message: string, args: Record<string, any>] | [{ message: string; args?: Array<string | number | boolean> | Record<string, any>; comment: string | string[] }]): string {
				if (typeof params[0] === 'string') {
					const key = params.shift() as string;

					// We have either rest args which are Array<string | number | boolean> or an array with a single Record<string, any>.
					// This ensures we get a Record<string | number, any> which will be formatted correctly.
					const argsFormatted = !params || typeof params[0] !== 'object' ? params : params[0];
					return extHostLocalization.getMessage(extension.identifier.value, { message: key, args: argsFormatted as Record<string | number, any> | undefined });
				}

				return extHostLocalization.getMessage(extension.identifier.value, params[0]);
			},
			get bundle() {
				return extHostLocalization.getBundle(extension.identifier.value);
			},
			get uri() {
				return extHostLocalization.getBundleUri(extension.identifier.value);
			}
		};

		// namespace: interactive
		const interactive: typeof vibecoda.interactive = {
			transferActiveChat(toWorkspace: vibecoda.Uri) {
				checkProposedApiEnabled(extension, 'interactive');
				return extHostChatAgents2.transferActiveChat(toWorkspace);
			}
		};

		// namespace: ai
		const ai: typeof vibecoda.ai = {
			getRelatedInformation(query: string, types: vibecoda.RelatedInformationType[]): Thenable<vibecoda.RelatedInformationResult[]> {
				checkProposedApiEnabled(extension, 'aiRelatedInformation');
				return extHostAiRelatedInformation.getRelatedInformation(extension, query, types);
			},
			registerRelatedInformationProvider(type: vibecoda.RelatedInformationType, provider: vibecoda.RelatedInformationProvider) {
				checkProposedApiEnabled(extension, 'aiRelatedInformation');
				return extHostAiRelatedInformation.registerRelatedInformationProvider(extension, type, provider);
			},
			registerEmbeddingVectorProvider(model: string, provider: vibecoda.EmbeddingVectorProvider) {
				checkProposedApiEnabled(extension, 'aiRelatedInformation');
				return extHostAiEmbeddingVector.registerEmbeddingVectorProvider(extension, model, provider);
			},
			registerSettingsSearchProvider(provider: vibecoda.SettingsSearchProvider) {
				checkProposedApiEnabled(extension, 'aiSettingsSearch');
				return extHostAiSettingsSearch.registerSettingsSearchProvider(extension, provider);
			}
		};

		// namespace: chatregisterMcpServerDefinitionProvider
		const chat: typeof vibecoda.chat = {
			registerMappedEditsProvider(_selector: vibecoda.DocumentSelector, _provider: vibecoda.MappedEditsProvider) {
				checkProposedApiEnabled(extension, 'mappedEditsProvider');
				// no longer supported
				return { dispose() { } };
			},
			registerMappedEditsProvider2(provider: vibecoda.MappedEditsProvider2) {
				checkProposedApiEnabled(extension, 'mappedEditsProvider');
				return extHostCodeMapper.registerMappedEditsProvider(extension, provider);
			},
			createChatParticipant(id: string, handler: vibecoda.ChatExtendedRequestHandler) {
				return extHostChatAgents2.createChatAgent(extension, id, handler);
			},
			createDynamicChatParticipant(id: string, dynamicProps: vibecoda.DynamicChatParticipantProps, handler: vibecoda.ChatExtendedRequestHandler): vibecoda.ChatParticipant {
				checkProposedApiEnabled(extension, 'chatParticipantPrivate');
				return extHostChatAgents2.createDynamicChatAgent(extension, id, dynamicProps, handler);
			},
			registerChatParticipantDetectionProvider(provider: vibecoda.ChatParticipantDetectionProvider) {
				checkProposedApiEnabled(extension, 'chatParticipantPrivate');
				return extHostChatAgents2.registerChatParticipantDetectionProvider(extension, provider);
			},
			registerRelatedFilesProvider(provider: vibecoda.ChatRelatedFilesProvider, metadata: vibecoda.ChatRelatedFilesProviderMetadata) {
				checkProposedApiEnabled(extension, 'chatEditing');
				return extHostChatAgents2.registerRelatedFilesProvider(extension, provider, metadata);
			},
			onDidDisposeChatSession: (listeners, thisArgs?, disposables?) => {
				checkProposedApiEnabled(extension, 'chatParticipantPrivate');
				return _asExtensionEvent(extHostChatAgents2.onDidDisposeChatSession)(listeners, thisArgs, disposables);
			},
			registerChatSessionItemProvider: (chatSessionType: string, provider: vibecoda.ChatSessionItemProvider) => {
				checkProposedApiEnabled(extension, 'chatSessionsProvider');
				return extHostChatSessions.registerChatSessionItemProvider(extension, chatSessionType, provider);
			},
			registerChatSessionContentProvider(scheme: string, provider: vibecoda.ChatSessionContentProvider, chatParticipant: vibecoda.ChatParticipant, capabilities?: vibecoda.ChatSessionCapabilities) {
				checkProposedApiEnabled(extension, 'chatSessionsProvider');
				return extHostChatSessions.registerChatSessionContentProvider(extension, scheme, chatParticipant, provider, capabilities);
			},
			registerChatOutputRenderer: (viewType: string, renderer: vibecoda.ChatOutputRenderer) => {
				checkProposedApiEnabled(extension, 'chatOutputRenderer');
				return extHostChatOutputRenderer.registerChatOutputRenderer(extension, viewType, renderer);
			},
			registerChatContextProvider(selector: vibecoda.DocumentSelector, id: string, provider: vibecoda.ChatContextProvider): vibecoda.Disposable {
				checkProposedApiEnabled(extension, 'chatContextProvider');
				return extHostChatContext.registerChatContextProvider(checkSelector(selector), `${extension.id}-${id}`, provider);
			},
		};

		// namespace: lm
		const lm: typeof vibecoda.lm = {
			selectChatModels: (selector) => {
				return extHostLanguageModels.selectLanguageModels(extension, selector ?? {});
			},
			onDidChangeChatModels: (listener, thisArgs?, disposables?) => {
				return extHostLanguageModels.onDidChangeProviders(listener, thisArgs, disposables);
			},
			registerLanguageModelChatProvider: (vendor, provider) => {
				return extHostLanguageModels.registerLanguageModelChatProvider(extension, vendor, provider);
			},
			get isModelProxyAvailable() {
				checkProposedApiEnabled(extension, 'languageModelProxy');
				return extHostLanguageModels.isModelProxyAvailable;
			},
			onDidChangeModelProxyAvailability: (listener, thisArgs?, disposables?) => {
				checkProposedApiEnabled(extension, 'languageModelProxy');
				return extHostLanguageModels.onDidChangeModelProxyAvailability(listener, thisArgs, disposables);
			},
			getModelProxy: () => {
				checkProposedApiEnabled(extension, 'languageModelProxy');
				return extHostLanguageModels.getModelProxy(extension);
			},
			registerLanguageModelProxyProvider: (provider) => {
				checkProposedApiEnabled(extension, 'chatParticipantPrivate');
				return extHostLanguageModels.registerLanguageModelProxyProvider(extension, provider);
			},
			// --- embeddings
			get embeddingModels() {
				checkProposedApiEnabled(extension, 'embeddings');
				return extHostEmbeddings.embeddingsModels;
			},
			onDidChangeEmbeddingModels: (listener, thisArgs?, disposables?) => {
				checkProposedApiEnabled(extension, 'embeddings');
				return extHostEmbeddings.onDidChange(listener, thisArgs, disposables);
			},
			registerEmbeddingsProvider(embeddingsModel, provider) {
				checkProposedApiEnabled(extension, 'embeddings');
				return extHostEmbeddings.registerEmbeddingsProvider(extension, embeddingsModel, provider);
			},
			async computeEmbeddings(embeddingsModel, input, token?): Promise<any> {
				checkProposedApiEnabled(extension, 'embeddings');
				if (typeof input === 'string') {
					return extHostEmbeddings.computeEmbeddings(embeddingsModel, input, token);
				} else {
					return extHostEmbeddings.computeEmbeddings(embeddingsModel, input, token);
				}
			},
			registerTool<T>(name: string, tool: vibecoda.LanguageModelTool<T>) {
				return extHostLanguageModelTools.registerTool(extension, name, tool);
			},
			invokeTool<T>(name: string, parameters: vibecoda.LanguageModelToolInvocationOptions<T>, token?: vibecoda.CancellationToken) {
				return extHostLanguageModelTools.invokeTool(extension, name, parameters, token);
			},
			get tools() {
				return extHostLanguageModelTools.getTools(extension);
			},
			fileIsIgnored(uri: vibecoda.Uri, token?: vibecoda.CancellationToken) {
				return extHostLanguageModels.fileIsIgnored(extension, uri, token);
			},
			registerIgnoredFileProvider(provider: vibecoda.LanguageModelIgnoredFileProvider) {
				return extHostLanguageModels.registerIgnoredFileProvider(extension, provider);
			},
			registerMcpServerDefinitionProvider(id, provider) {
				return extHostMcp.registerMcpConfigurationProvider(extension, id, provider);
			},
			onDidChangeChatRequestTools(...args) {
				checkProposedApiEnabled(extension, 'chatParticipantAdditions');
				return _asExtensionEvent(extHostChatAgents2.onDidChangeChatRequestTools)(...args);
			}
		};

		// namespace: speech
		const speech: typeof vibecoda.speech = {
			registerSpeechProvider(id: string, provider: vibecoda.SpeechProvider) {
				checkProposedApiEnabled(extension, 'speech');
				return extHostSpeech.registerProvider(extension.identifier, id, provider);
			}
		};

		// eslint-disable-next-line local/code-no-dangerous-type-assertions
		return <typeof vibecoda>{
			version: initData.version,
			// namespaces
			ai,
			authentication,
			commands,
			comments,
			chat,
			debug,
			env,
			extensions,
			interactive,
			l10n,
			languages,
			lm,
			notebooks,
			scm,
			speech,
			tasks,
			tests,
			window,
			workspace,
			// types
			Breakpoint: extHostTypes.Breakpoint,
			TerminalOutputAnchor: extHostTypes.TerminalOutputAnchor,
			ChatResultFeedbackKind: extHostTypes.ChatResultFeedbackKind,
			ChatVariableLevel: extHostTypes.ChatVariableLevel,
			ChatCompletionItem: extHostTypes.ChatCompletionItem,
			ChatReferenceDiagnostic: extHostTypes.ChatReferenceDiagnostic,
			CallHierarchyIncomingCall: extHostTypes.CallHierarchyIncomingCall,
			CallHierarchyItem: extHostTypes.CallHierarchyItem,
			CallHierarchyOutgoingCall: extHostTypes.CallHierarchyOutgoingCall,
			CancellationError: errors.CancellationError,
			CancellationTokenSource: CancellationTokenSource,
			CandidatePortSource: CandidatePortSource,
			CodeAction: extHostTypes.CodeAction,
			CodeActionKind: extHostTypes.CodeActionKind,
			CodeActionTriggerKind: extHostTypes.CodeActionTriggerKind,
			CodeLens: extHostTypes.CodeLens,
			Color: extHostTypes.Color,
			ColorInformation: extHostTypes.ColorInformation,
			ColorPresentation: extHostTypes.ColorPresentation,
			ColorThemeKind: extHostTypes.ColorThemeKind,
			CommentMode: extHostTypes.CommentMode,
			CommentState: extHostTypes.CommentState,
			CommentThreadCollapsibleState: extHostTypes.CommentThreadCollapsibleState,
			CommentThreadState: extHostTypes.CommentThreadState,
			CommentThreadApplicability: extHostTypes.CommentThreadApplicability,
			CommentThreadFocus: extHostTypes.CommentThreadFocus,
			CompletionItem: extHostTypes.CompletionItem,
			CompletionItemKind: extHostTypes.CompletionItemKind,
			CompletionItemTag: extHostTypes.CompletionItemTag,
			CompletionList: extHostTypes.CompletionList,
			CompletionTriggerKind: extHostTypes.CompletionTriggerKind,
			ConfigurationTarget: extHostTypes.ConfigurationTarget,
			CustomExecution: extHostTypes.CustomExecution,
			DebugAdapterExecutable: extHostTypes.DebugAdapterExecutable,
			DebugAdapterInlineImplementation: extHostTypes.DebugAdapterInlineImplementation,
			DebugAdapterNamedPipeServer: extHostTypes.DebugAdapterNamedPipeServer,
			DebugAdapterServer: extHostTypes.DebugAdapterServer,
			DebugConfigurationProviderTriggerKind: DebugConfigurationProviderTriggerKind,
			DebugConsoleMode: extHostTypes.DebugConsoleMode,
			DebugVisualization: extHostTypes.DebugVisualization,
			DecorationRangeBehavior: extHostTypes.DecorationRangeBehavior,
			Diagnostic: extHostTypes.Diagnostic,
			DiagnosticRelatedInformation: extHostTypes.DiagnosticRelatedInformation,
			DiagnosticSeverity: extHostTypes.DiagnosticSeverity,
			DiagnosticTag: extHostTypes.DiagnosticTag,
			Disposable: extHostTypes.Disposable,
			DocumentHighlight: extHostTypes.DocumentHighlight,
			DocumentHighlightKind: extHostTypes.DocumentHighlightKind,
			MultiDocumentHighlight: extHostTypes.MultiDocumentHighlight,
			DocumentLink: extHostTypes.DocumentLink,
			DocumentSymbol: extHostTypes.DocumentSymbol,
			EndOfLine: extHostTypes.EndOfLine,
			EnvironmentVariableMutatorType: extHostTypes.EnvironmentVariableMutatorType,
			EvaluatableExpression: extHostTypes.EvaluatableExpression,
			InlineValueText: extHostTypes.InlineValueText,
			InlineValueVariableLookup: extHostTypes.InlineValueVariableLookup,
			InlineValueEvaluatableExpression: extHostTypes.InlineValueEvaluatableExpression,
			InlineCompletionTriggerKind: extHostTypes.InlineCompletionTriggerKind,
			InlineCompletionsDisposeReasonKind: extHostTypes.InlineCompletionsDisposeReasonKind,
			EventEmitter: Emitter,
			ExtensionKind: extHostTypes.ExtensionKind,
			ExtensionMode: extHostTypes.ExtensionMode,
			ExternalUriOpenerPriority: extHostTypes.ExternalUriOpenerPriority,
			FileChangeType: extHostTypes.FileChangeType,
			FileDecoration: extHostTypes.FileDecoration,
			FileDecoration2: extHostTypes.FileDecoration,
			FileSystemError: extHostTypes.FileSystemError,
			FileType: files.FileType,
			FilePermission: files.FilePermission,
			FoldingRange: extHostTypes.FoldingRange,
			FoldingRangeKind: extHostTypes.FoldingRangeKind,
			FunctionBreakpoint: extHostTypes.FunctionBreakpoint,
			InlineCompletionItem: extHostTypes.InlineSuggestion,
			InlineCompletionList: extHostTypes.InlineSuggestionList,
			Hover: extHostTypes.Hover,
			VerboseHover: extHostTypes.VerboseHover,
			HoverVerbosityAction: extHostTypes.HoverVerbosityAction,
			IndentAction: languageConfiguration.IndentAction,
			Location: extHostTypes.Location,
			MarkdownString: extHostTypes.MarkdownString,
			OverviewRulerLane: OverviewRulerLane,
			ParameterInformation: extHostTypes.ParameterInformation,
			PortAutoForwardAction: extHostTypes.PortAutoForwardAction,
			Position: extHostTypes.Position,
			ProcessExecution: extHostTypes.ProcessExecution,
			ProgressLocation: extHostTypes.ProgressLocation,
			QuickInputButtonLocation: extHostTypes.QuickInputButtonLocation,
			QuickInputButtons: extHostTypes.QuickInputButtons,
			Range: extHostTypes.Range,
			RelativePattern: extHostTypes.RelativePattern,
			Selection: extHostTypes.Selection,
			SelectionRange: extHostTypes.SelectionRange,
			SemanticTokens: extHostTypes.SemanticTokens,
			SemanticTokensBuilder: extHostTypes.SemanticTokensBuilder,
			SemanticTokensEdit: extHostTypes.SemanticTokensEdit,
			SemanticTokensEdits: extHostTypes.SemanticTokensEdits,
			SemanticTokensLegend: extHostTypes.SemanticTokensLegend,
			ShellExecution: extHostTypes.ShellExecution,
			ShellQuoting: extHostTypes.ShellQuoting,
			SignatureHelp: extHostTypes.SignatureHelp,
			SignatureHelpTriggerKind: extHostTypes.SignatureHelpTriggerKind,
			SignatureInformation: extHostTypes.SignatureInformation,
			SnippetString: extHostTypes.SnippetString,
			SourceBreakpoint: extHostTypes.SourceBreakpoint,
			StandardTokenType: extHostTypes.StandardTokenType,
			StatusBarAlignment: extHostTypes.StatusBarAlignment,
			SymbolInformation: extHostTypes.SymbolInformation,
			SymbolKind: extHostTypes.SymbolKind,
			SymbolTag: extHostTypes.SymbolTag,
			Task: extHostTypes.Task,
			TaskEventKind: extHostTypes.TaskEventKind,
			TaskGroup: extHostTypes.TaskGroup,
			TaskPanelKind: extHostTypes.TaskPanelKind,
			TaskRevealKind: extHostTypes.TaskRevealKind,
			TaskScope: extHostTypes.TaskScope,
			TerminalLink: extHostTypes.TerminalLink,
			TerminalQuickFixTerminalCommand: extHostTypes.TerminalQuickFixCommand,
			TerminalQuickFixOpener: extHostTypes.TerminalQuickFixOpener,
			TerminalLocation: extHostTypes.TerminalLocation,
			TerminalProfile: extHostTypes.TerminalProfile,
			TerminalExitReason: extHostTypes.TerminalExitReason,
			TerminalShellExecutionCommandLineConfidence: extHostTypes.TerminalShellExecutionCommandLineConfidence,
			TerminalCompletionItem: extHostTypes.TerminalCompletionItem,
			TerminalCompletionItemKind: extHostTypes.TerminalCompletionItemKind,
			TerminalCompletionList: extHostTypes.TerminalCompletionList,
			TerminalShellType: extHostTypes.TerminalShellType,
			TextDocumentSaveReason: extHostTypes.TextDocumentSaveReason,
			TextEdit: extHostTypes.TextEdit,
			SnippetTextEdit: extHostTypes.SnippetTextEdit,
			TextEditorCursorStyle: TextEditorCursorStyle,
			TextEditorChangeKind: extHostTypes.TextEditorChangeKind,
			TextEditorLineNumbersStyle: extHostTypes.TextEditorLineNumbersStyle,
			TextEditorRevealType: extHostTypes.TextEditorRevealType,
			TextEditorSelectionChangeKind: extHostTypes.TextEditorSelectionChangeKind,
			SyntaxTokenType: extHostTypes.SyntaxTokenType,
			TextDocumentChangeReason: extHostTypes.TextDocumentChangeReason,
			ThemeColor: extHostTypes.ThemeColor,
			ThemeIcon: extHostTypes.ThemeIcon,
			TreeItem: extHostTypes.TreeItem,
			TreeItemCheckboxState: extHostTypes.TreeItemCheckboxState,
			TreeItemCollapsibleState: extHostTypes.TreeItemCollapsibleState,
			TypeHierarchyItem: extHostTypes.TypeHierarchyItem,
			UIKind: UIKind,
			Uri: URI,
			ViewColumn: extHostTypes.ViewColumn,
			WorkspaceEdit: extHostTypes.WorkspaceEdit,
			// proposed api types
			DocumentPasteTriggerKind: extHostTypes.DocumentPasteTriggerKind,
			DocumentDropEdit: extHostTypes.DocumentDropEdit,
			DocumentDropOrPasteEditKind: extHostTypes.DocumentDropOrPasteEditKind,
			DocumentPasteEdit: extHostTypes.DocumentPasteEdit,
			InlayHint: extHostTypes.InlayHint,
			InlayHintLabelPart: extHostTypes.InlayHintLabelPart,
			InlayHintKind: extHostTypes.InlayHintKind,
			RemoteAuthorityResolverError: extHostTypes.RemoteAuthorityResolverError,
			ResolvedAuthority: extHostTypes.ResolvedAuthority,
			ManagedResolvedAuthority: extHostTypes.ManagedResolvedAuthority,
			SourceControlInputBoxValidationType: extHostTypes.SourceControlInputBoxValidationType,
			ExtensionRuntime: extHostTypes.ExtensionRuntime,
			TimelineItem: extHostTypes.TimelineItem,
			NotebookRange: extHostTypes.NotebookRange,
			NotebookCellKind: extHostTypes.NotebookCellKind,
			NotebookCellExecutionState: extHostTypes.NotebookCellExecutionState,
			NotebookCellData: extHostTypes.NotebookCellData,
			NotebookData: extHostTypes.NotebookData,
			NotebookRendererScript: extHostTypes.NotebookRendererScript,
			NotebookCellStatusBarAlignment: extHostTypes.NotebookCellStatusBarAlignment,
			NotebookEditorRevealType: extHostTypes.NotebookEditorRevealType,
			NotebookCellOutput: extHostTypes.NotebookCellOutput,
			NotebookCellOutputItem: extHostTypes.NotebookCellOutputItem,
			CellErrorStackFrame: extHostTypes.CellErrorStackFrame,
			NotebookCellStatusBarItem: extHostTypes.NotebookCellStatusBarItem,
			NotebookControllerAffinity: extHostTypes.NotebookControllerAffinity,
			NotebookControllerAffinity2: extHostTypes.NotebookControllerAffinity2,
			NotebookEdit: extHostTypes.NotebookEdit,
			NotebookKernelSourceAction: extHostTypes.NotebookKernelSourceAction,
			NotebookVariablesRequestKind: extHostTypes.NotebookVariablesRequestKind,
			PortAttributes: extHostTypes.PortAttributes,
			LinkedEditingRanges: extHostTypes.LinkedEditingRanges,
			TestResultState: extHostTypes.TestResultState,
			TestRunRequest: extHostTypes.TestRunRequest,
			TestMessage: extHostTypes.TestMessage,
			TestMessageStackFrame: extHostTypes.TestMessageStackFrame,
			TestTag: extHostTypes.TestTag,
			TestRunProfileKind: extHostTypes.TestRunProfileKind,
			TextSearchCompleteMessageType: TextSearchCompleteMessageType,
			DataTransfer: extHostTypes.DataTransfer,
			DataTransferItem: extHostTypes.DataTransferItem,
			TestCoverageCount: extHostTypes.TestCoverageCount,
			FileCoverage: extHostTypes.FileCoverage,
			StatementCoverage: extHostTypes.StatementCoverage,
			BranchCoverage: extHostTypes.BranchCoverage,
			DeclarationCoverage: extHostTypes.DeclarationCoverage,
			WorkspaceTrustState: extHostTypes.WorkspaceTrustState,
			LanguageStatusSeverity: extHostTypes.LanguageStatusSeverity,
			QuickPickItemKind: extHostTypes.QuickPickItemKind,
			InputBoxValidationSeverity: extHostTypes.InputBoxValidationSeverity,
			TabInputText: extHostTypes.TextTabInput,
			TabInputTextDiff: extHostTypes.TextDiffTabInput,
			TabInputTextMerge: extHostTypes.TextMergeTabInput,
			TabInputCustom: extHostTypes.CustomEditorTabInput,
			TabInputNotebook: extHostTypes.NotebookEditorTabInput,
			TabInputNotebookDiff: extHostTypes.NotebookDiffEditorTabInput,
			TabInputWebview: extHostTypes.WebviewEditorTabInput,
			TabInputTerminal: extHostTypes.TerminalEditorTabInput,
			TabInputInteractiveWindow: extHostTypes.InteractiveWindowInput,
			TabInputChat: extHostTypes.ChatEditorTabInput,
			TabInputTextMultiDiff: extHostTypes.TextMultiDiffTabInput,
			TelemetryTrustedValue: TelemetryTrustedValue,
			LogLevel: LogLevel,
			EditSessionIdentityMatch: EditSessionIdentityMatch,
			InteractiveSessionVoteDirection: extHostTypes.InteractiveSessionVoteDirection,
			ChatCopyKind: extHostTypes.ChatCopyKind,
			ChatEditingSessionActionOutcome: extHostTypes.ChatEditingSessionActionOutcome,
			InteractiveEditorResponseFeedbackKind: extHostTypes.InteractiveEditorResponseFeedbackKind,
			DebugStackFrame: extHostTypes.DebugStackFrame,
			DebugThread: extHostTypes.DebugThread,
			RelatedInformationType: extHostTypes.RelatedInformationType,
			SpeechToTextStatus: extHostTypes.SpeechToTextStatus,
			TextToSpeechStatus: extHostTypes.TextToSpeechStatus,
			PartialAcceptTriggerKind: extHostTypes.PartialAcceptTriggerKind,
			InlineCompletionEndOfLifeReasonKind: extHostTypes.InlineCompletionEndOfLifeReasonKind,
			InlineCompletionDisplayLocationKind: extHostTypes.InlineCompletionDisplayLocationKind,
			KeywordRecognitionStatus: extHostTypes.KeywordRecognitionStatus,
			ChatImageMimeType: extHostTypes.ChatImageMimeType,
			ChatResponseMarkdownPart: extHostTypes.ChatResponseMarkdownPart,
			ChatResponseFileTreePart: extHostTypes.ChatResponseFileTreePart,
			ChatResponseAnchorPart: extHostTypes.ChatResponseAnchorPart,
			ChatResponseProgressPart: extHostTypes.ChatResponseProgressPart,
			ChatResponseProgressPart2: extHostTypes.ChatResponseProgressPart2,
			ChatResponseThinkingProgressPart: extHostTypes.ChatResponseThinkingProgressPart,
			ChatResponseReferencePart: extHostTypes.ChatResponseReferencePart,
			ChatResponseReferencePart2: extHostTypes.ChatResponseReferencePart,
			ChatResponseCodeCitationPart: extHostTypes.ChatResponseCodeCitationPart,
			ChatResponseCodeblockUriPart: extHostTypes.ChatResponseCodeblockUriPart,
			ChatResponseWarningPart: extHostTypes.ChatResponseWarningPart,
			ChatResponseTextEditPart: extHostTypes.ChatResponseTextEditPart,
			ChatResponseNotebookEditPart: extHostTypes.ChatResponseNotebookEditPart,
			ChatResponseMarkdownWithVulnerabilitiesPart: extHostTypes.ChatResponseMarkdownWithVulnerabilitiesPart,
			ChatResponseCommandButtonPart: extHostTypes.ChatResponseCommandButtonPart,
			ChatResponseConfirmationPart: extHostTypes.ChatResponseConfirmationPart,
			ChatResponseMovePart: extHostTypes.ChatResponseMovePart,
			ChatResponseExtensionsPart: extHostTypes.ChatResponseExtensionsPart,
			ChatResponseExternalEditPart: extHostTypes.ChatResponseExternalEditPart,
			ChatResponsePullRequestPart: extHostTypes.ChatResponsePullRequestPart,
			ChatPrepareToolInvocationPart: extHostTypes.ChatPrepareToolInvocationPart,
			ChatResponseMultiDiffPart: extHostTypes.ChatResponseMultiDiffPart,
			ChatResponseReferencePartStatusKind: extHostTypes.ChatResponseReferencePartStatusKind,
			ChatResponseClearToPreviousToolInvocationReason: extHostTypes.ChatResponseClearToPreviousToolInvocationReason,
			ChatRequestTurn: extHostTypes.ChatRequestTurn,
			ChatRequestTurn2: extHostTypes.ChatRequestTurn,
			ChatResponseTurn: extHostTypes.ChatResponseTurn,
			ChatResponseTurn2: extHostTypes.ChatResponseTurn2,
			ChatToolInvocationPart: extHostTypes.ChatToolInvocationPart,
			ChatLocation: extHostTypes.ChatLocation,
			ChatSessionStatus: extHostTypes.ChatSessionStatus,
			ChatRequestEditorData: extHostTypes.ChatRequestEditorData,
			ChatRequestNotebookData: extHostTypes.ChatRequestNotebookData,
			ChatReferenceBinaryData: extHostTypes.ChatReferenceBinaryData,
			ChatRequestEditedFileEventKind: extHostTypes.ChatRequestEditedFileEventKind,
			LanguageModelChatMessageRole: extHostTypes.LanguageModelChatMessageRole,
			LanguageModelChatMessage: extHostTypes.LanguageModelChatMessage,
			LanguageModelChatMessage2: extHostTypes.LanguageModelChatMessage2,
			LanguageModelToolResultPart: extHostTypes.LanguageModelToolResultPart,
			LanguageModelToolResultPart2: extHostTypes.LanguageModelToolResultPart,
			LanguageModelTextPart: extHostTypes.LanguageModelTextPart,
			LanguageModelTextPart2: extHostTypes.LanguageModelTextPart,
			LanguageModelPartAudience: extHostTypes.LanguageModelPartAudience,
			ToolResultAudience: extHostTypes.LanguageModelPartAudience, // back compat
			LanguageModelToolCallPart: extHostTypes.LanguageModelToolCallPart,
			LanguageModelThinkingPart: extHostTypes.LanguageModelThinkingPart,
			LanguageModelError: extHostTypes.LanguageModelError,
			LanguageModelToolResult: extHostTypes.LanguageModelToolResult,
			LanguageModelToolResult2: extHostTypes.LanguageModelToolResult2,
			LanguageModelDataPart: extHostTypes.LanguageModelDataPart,
			LanguageModelDataPart2: extHostTypes.LanguageModelDataPart,
			LanguageModelToolExtensionSource: extHostTypes.LanguageModelToolExtensionSource,
			LanguageModelToolMCPSource: extHostTypes.LanguageModelToolMCPSource,
			ExtendedLanguageModelToolResult: extHostTypes.ExtendedLanguageModelToolResult,
			LanguageModelChatToolMode: extHostTypes.LanguageModelChatToolMode,
			LanguageModelPromptTsxPart: extHostTypes.LanguageModelPromptTsxPart,
			NewSymbolName: extHostTypes.NewSymbolName,
			NewSymbolNameTag: extHostTypes.NewSymbolNameTag,
			NewSymbolNameTriggerKind: extHostTypes.NewSymbolNameTriggerKind,
			ExcludeSettingOptions: ExcludeSettingOptions,
			TextSearchContext2: TextSearchContext2,
			TextSearchMatch2: TextSearchMatch2,
			AISearchKeyword: AISearchKeyword,
			TextSearchCompleteMessageTypeNew: TextSearchCompleteMessageType,
			ChatErrorLevel: extHostTypes.ChatErrorLevel,
			McpHttpServerDefinition: extHostTypes.McpHttpServerDefinition,
			McpHttpServerDefinition2: extHostTypes.McpHttpServerDefinition,
			McpStdioServerDefinition: extHostTypes.McpStdioServerDefinition,
			McpStdioServerDefinition2: extHostTypes.McpStdioServerDefinition,
			McpToolAvailability: extHostTypes.McpToolAvailability,
			SettingsSearchResultKind: extHostTypes.SettingsSearchResultKind
		};
	};
}
