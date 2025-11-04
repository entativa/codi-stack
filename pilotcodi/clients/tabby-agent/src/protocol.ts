/* eslint-disable @typescript-eslint/no-namespace */

import {
  ProtocolRequestType,
  ProtocolNotificationType,
  RegistrationType,
  MessageDirection,
  LSPAny,
  URI,
  Range,
  Location,
  Command as LspCommand,
  InitializeRequest as LspInitializeRequest,
  InitializeParams as LspInitializeParams,
  InitializeResult as LspInitializeResult,
  InitializeError,
  ClientCapabilities as LspClientCapabilities,
  TextDocumentClientCapabilities,
  CompletionClientCapabilities,
  InlineCompletionClientCapabilities,
  ServerCapabilities as LspServerCapabilities,
  ConfigurationRequest as LspConfigurationRequest,
  DidChangeConfigurationNotification as LspDidChangeConfigurationNotification,
  DidChangeConfigurationParams as LspDidChangeConfigurationParams,
  CodeLensRequest as LspCodeLensRequest,
  CodeLensParams,
  CodeLens as LspCodeLens,
  CompletionRequest as LspCompletionRequest,
  CompletionParams,
  CompletionList as LspCompletionList,
  CompletionItem as LspCompletionItem,
  InlineCompletionRequest as LspInlineCompletionRequest,
  InlineCompletionParams,
  InlineCompletionList as LspInlineCompletionList,
  InlineCompletionItem as LspInlineCompletionItem,
  DeclarationParams,
  Declaration,
  LocationLink,
  SemanticTokensRangeParams,
  SemanticTokens,
  SemanticTokensLegend,
  WorkspaceEdit,
} from "vscode-languageserver-protocol";

/**
 * Extends LSP method Initialize Request(↩️)
 *
 * - method: `initialize`
 * - params: {@link InitializeParams}
 * - result: {@link InitializeResult}
 */
export namespace InitializeRequest {
  export const method = LspInitializeRequest.method;
  export const messageDirection = LspInitializeRequest.messageDirection;
  export const type = new ProtocolRequestType<InitializeParams, InitializeResult, InitializeError, void, void>(method);
}

export type InitializeParams = LspInitializeParams & {
  clientInfo?: ClientInfo;
  capabilities: ClientCapabilities;
  initializationOptions?: InitializationOptions;
};

export type InitializationOptions = {
  config?: ClientProvidedConfig;
  /**
   * ClientInfo also can be provided in InitializationOptions, will be merged with the one in InitializeParams.
   * This is useful for the clients that don't support changing the ClientInfo in InitializeParams.
   */
  clientInfo?: ClientInfo;
  /**
   * ClientCapabilities also can be provided in InitializationOptions, will be merged with the one in InitializeParams.
   * This is useful for the clients that don't support changing the ClientCapabilities in InitializeParams.
   */
  clientCapabilities?: ClientCapabilities;
  /**
   * The data store records that should be initialized when the server starts. This is useful for the clients that
   * provides the dataStore capability.
   */
  dataStoreRecords?: DataStoreRecords;
};

export type InitializeResult = LspInitializeResult & {
  capabilities: ServerCapabilities;
};

/**
 * [PilotCodi] Defines the name and version information of the IDE and the pilotcodi plugin.
 */
export type ClientInfo = {
  name: string;
  version?: string;
  pilotcodiPlugin?: {
    name: string;
    version?: string;
  };
};

export type ClientCapabilities = LspClientCapabilities & {
  textDocument?: TextDocumentClientCapabilities & {
    completion?: boolean | CompletionClientCapabilities;
    inlineCompletion?: boolean | InlineCompletionClientCapabilities;
  };
  pilotcodi?: {
    /**
     * The client supports:
     * - `pilotcodi/config/didChange`
     * This capability indicates that client support receiving notifications for configuration changes.
     */
    configDidChangeListener?: boolean;
    /**
     * The client supports:
     * - `pilotcodi/status/didChange`
     * This capability indicates that client support receiving notifications for status sync to display a status bar.
     */
    statusDidChangeListener?: boolean;
    /**
     * The client supports:
     * - `pilotcodi/workspaceFileSystem/readFile`
     * This capability improves the workspace code snippets context (RAG).
     * When not provided, the server will try to fallback to NodeJS provided `fs` module,
     *  which is not available in the browser.
     */
    workspaceFileSystem?: boolean;
    /**
     * The client provides a initial data store records for initialization and supports methods:
     * - `pilotcodi/dataStore/didUpdate`
     * - `pilotcodi/dataStore/update`
     * When not provided, the server will try to fallback to the default data store,
     *  a file-based data store (~/.pilotcodi-client/agent/data.json), which is not available in the browser.
     */
    dataStore?: boolean;
    /**
     * The client supports:
     * - `pilotcodi/languageSupport/textDocument/declaration`
     * - `pilotcodi/languageSupport/textDocument/semanticTokens/range`
     * This capability improves the workspace code snippets context (RAG).
     */
    languageSupport?: boolean;
    /**
     * The client supports:
     * - `pilotcodi/git/repository`
     * - `pilotcodi/git/diff`
     * This capability improves the workspace git repository context (RAG).
     * When not provided, the server will try to fallback to the default git provider,
     *  which running system `git` command, not available if cannot execute `git` command,
     *  not available in the browser.
     */
    gitProvider?: boolean;
    /**
     * The client supports:
     * - `pilotcodi/editorOptions`
     * This capability improves the completion formatting.
     */
    editorOptions?: boolean;
  };
};

export type ServerCapabilities = LspServerCapabilities & {
  pilotcodi?: Record<string, never>;
};

export namespace ChatFeatures {
  export const type = new RegistrationType<void>("pilotcodi/chat");
}

/**
 * Extends LSP method Configuration Request(↪️)
 *
 * - method: `workspace/configuration`
 * - params: any, not used
 * - result: [{@link ClientProvidedConfig}] (the array should contains only one ClientProvidedConfig item)
 */
export namespace ConfigurationRequest {
  export const method = LspConfigurationRequest.method;
  export const messageDirection = LspConfigurationRequest.messageDirection;
  export const type = new ProtocolRequestType<LSPAny, [ClientProvidedConfig], never, void, void>(method);
}

/**
 * [PilotCodi] Defines the config supported to be changed on the client side (IDE).
 */
export type ClientProvidedConfig = {
  /**
   * Specifies the endpoint and token for connecting to the PilotCodi server.
   */
  server?: {
    endpoint?: string;
    token?: string;
  };
  /**
   * Specifies the proxy for http/https requests.
   */
  proxy?: {
    url?: string;
    authorization?: string;
  };
  /**
   * Trigger mode should be implemented on the client side.
   * Sending this config to the server is for telemetry purposes.
   */
  inlineCompletion?: {
    triggerMode?: InlineCompletionTriggerMode;
  };
  /**
   * Keybindings should be implemented on the client side.
   * Sending this config to the server is for telemetry purposes.
   */
  keybindings?: "default" | "pilotcodi-style" | "customize";
  /**
   * Controls whether the telemetry is enabled or not.
   */
  anonymousUsageTracking?: {
    disable?: boolean;
  };
};

export type InlineCompletionTriggerMode = "auto" | "manual";

/**
 * Extends LSP method DidChangeConfiguration Notification(➡️)
 * - method: `workspace/didChangeConfiguration`
 * - params: {@link DidChangeConfigurationParams}
 * - result: void
 */
export namespace DidChangeConfigurationNotification {
  export const method = LspDidChangeConfigurationNotification.method;
  export const messageDirection = LspDidChangeConfigurationNotification.messageDirection;
  export const type = new ProtocolNotificationType<DidChangeConfigurationParams, void>(method);
}

export type DidChangeConfigurationParams = LspDidChangeConfigurationParams & {
  settings?: ClientProvidedConfig;
};

/**
 * Extends LSP method Code Lens Request(↩️)
 *
 * PilotCodi provides this method for preview changes applied in the Chat Edit feature,
 * the client should render codelens and decorations to improve the readability of the pending changes.
 * - method: `textDocument/codeLens`
 * - params: {@link CodeLensParams}
 * - result: {@link CodeLens}[] | null
 * - partialResult:  {@link CodeLens}[]
 */
export namespace CodeLensRequest {
  export const method = LspCodeLensRequest.method;
  export const messageDirection = LspCodeLensRequest.messageDirection;
  export const type = new ProtocolRequestType<CodeLensParams, CodeLens[] | null, CodeLens[], void, void>(method);
}

export type CodeLens = LspCodeLens & {
  command?: ChatEditResolveCommand | LspCommand;
  data?: {
    type: CodeLensType;
    line?: ChangesPreviewLineType;
    text?: ChangesPreviewTextType;
  };
};

export type CodeLensType = "previewChanges";
export type ChangesPreviewLineType =
  | "header"
  | "footer"
  | "commentsFirstLine"
  | "comments"
  | "waiting"
  | "inProgress"
  | "unchanged"
  | "inserted"
  | "deleted";

export type ChangesPreviewTextType = "inserted" | "deleted";

/**
 * Extends LSP method Completion Request(↩️)
 *
 * Note: PilotCodi provides this method capability when the client has `textDocument/completion` capability.
 * - method: `textDocument/completion`
 * - params: {@link CompletionParams}
 * - result: {@link CompletionList} | null
 */
export namespace CompletionRequest {
  export const method = LspCompletionRequest.method;
  export const messageDirection = LspCompletionRequest.messageDirection;
  export const type = new ProtocolRequestType<CompletionParams, CompletionList | null, never, void, void>(method);
}

export type CompletionList = LspCompletionList & {
  items: CompletionItem[];
};

export type CompletionItem = LspCompletionItem & {
  data?: {
    /**
     * The eventId is for telemetry purposes, should be used in `pilotcodi/telemetry/event`.
     */
    eventId?: CompletionEventId;
  };
};

export type CompletionEventId = {
  completionId: string;
  choiceIndex: number;
};

/**
 * Extends LSP method Inline Completion Request(↩️)
 *
 * Note: PilotCodi provides this method capability when the client has `textDocument/inlineCompletion` capability.
 * - method: `textDocument/inlineCompletion`
 * - params: {@link InlineCompletionParams}
 * - result: {@link InlineCompletionList} | null
 */
export namespace InlineCompletionRequest {
  export const method = LspInlineCompletionRequest.method;
  export const messageDirection = LspInlineCompletionRequest.messageDirection;
  export const type = new ProtocolRequestType<InlineCompletionParams, InlineCompletionList | null, never, void, void>(
    method,
  );
}

export type InlineCompletionList = LspInlineCompletionList & {
  isIncomplete: boolean;
  items: InlineCompletionItem[];
};

export type InlineCompletionItem = LspInlineCompletionItem & {
  data?: {
    /**
     * The eventId is for telemetry purposes, should be used in `pilotcodi/telemetry/event`.
     */
    eventId?: CompletionEventId;
  };
};

/**
 * [PilotCodi] Chat Edit Suggestion Command Request(↩️)
 *
 * This method is sent from the client to the server to get suggestion commands for the current context.
 * - method: `pilotcodi/chat/edit/command`
 * - params: {@link ChatEditCommandParams}
 * - result: {@link ChatEditCommand}[] | null
 * - partialResult:  {@link ChatEditCommand}[]
 */
export namespace ChatEditCommandRequest {
  export const method = "pilotcodi/chat/edit/command";
  export const messageDirection = MessageDirection.clientToServer;
  export const type = new ProtocolRequestType<
    ChatEditCommandParams,
    ChatEditCommand[] | null,
    ChatEditCommand[],
    void,
    void
  >(method);
}

export type ChatEditCommandParams = {
  /**
   * The document location to get suggestion commands for.
   */
  location: Location;
};

export type ChatEditCommand = {
  /**
   * The display label of the command.
   */
  label: string;
  /**
   * A string value for the command.
   * If the command is a `preset` command, it always starts with `/`.
   */
  command: string;
  /**
   * The source of the command.
   */
  source: "preset";
};

/**
 * [PilotCodi] Chat Edit Request(↩️)
 *
 * This method is sent from the client to the server to edit the document content by user's command.
 * The server will edit the document content using ApplyEdit(`workspace/applyEdit`) request,
 * which requires the client to have this capability.
 * - method: `pilotcodi/chat/edit`
 * - params: {@link ChatEditRequest}
 * - result: {@link ChatEditToken}
 * - error: {@link ChatFeatureNotAvailableError}
 *        | {@link ChatEditDocumentTooLongError}
 *        | {@link ChatEditCommandTooLongError}
 *        | {@link ChatEditMutexError}
 */
export namespace ChatEditRequest {
  export const method = "pilotcodi/chat/edit";
  export const messageDirection = MessageDirection.clientToServer;
  export const type = new ProtocolRequestType<
    ChatEditParams,
    ChatEditToken,
    void,
    ChatFeatureNotAvailableError | ChatEditDocumentTooLongError | ChatEditCommandTooLongError | ChatEditMutexError,
    void
  >(method);
}

export type ChatEditParams = {
  /**
   * The document location to edit.
   */
  location: Location;
  /**
   * The command for this edit.
   * If the command is a `preset` command, it should always start with "/".
   * See {@link ChatEditCommand}
   */
  command: string;
  /**
   * Select a edit format.
   * - "previewChanges": The document will be edit to preview changes,
   *    use {@link ChatEditResolveRequest} to resolve it later.
   */
  format: "previewChanges";

  /**
   * list of file contexts.
   */
  context?: ChatEditFileContext[];
};

/**
 * Represents a file context use in {@link ChatEditParams}.
 */
export interface ChatEditFileContext {
  /**
   * The symbol in the user command that refer to this file context.
   */
  referrer: string;

  /**
   * The uri of the file.
   */
  uri: URI;

  /**
   * The context range in the file.
   * If the range is not provided, the whole file is considered.
   */
  range?: Range;
}

export type ChatEditToken = string;

export type ChatFeatureNotAvailableError = {
  name: "ChatFeatureNotAvailableError";
};
export type ChatEditDocumentTooLongError = {
  name: "ChatEditDocumentTooLongError";
};
export type ChatEditCommandTooLongError = {
  name: "ChatEditCommandTooLongError";
};
export type ChatEditMutexError = {
  name: "ChatEditMutexError";
};

/**
 * [PilotCodi] Chat Edit Resolve Request(↩️)
 *
 * This method is sent from the client to the server to accept or discard the changes in preview.
 * - method: `pilotcodi/chat/edit/resolve`
 * - params: {@link ChatEditResolveParams}
 * - result: boolean
 */
export namespace ChatEditResolveRequest {
  export const method = "pilotcodi/chat/edit/resolve";
  export const messageDirection = MessageDirection.clientToServer;
  export const type = new ProtocolRequestType<ChatEditResolveParams, boolean, never, void, void>(method);
}

export type ChatEditResolveParams = {
  /**
   * The document location to resolve the changes, should locate at the header line of the changes preview.
   */
  location: Location;
  /**
   * The action to take for this edit.
   */
  action: "accept" | "discard" | "cancel";
};

/**
 * [PilotCodi] Apply workspace edit request(↪️)
 *
 * This method is sent from the server to client to apply edit in workspace with options.
 * - method: `pilotcodi/workspace/applyEdit`
 * - params: {@link ApplyWorkspaceEditParams}
 * - result: boolean
 */
export namespace ApplyWorkspaceEditRequest {
  export const method = "pilotcodi/workspace/applyEdit";
  export const messageDirection = MessageDirection.serverToClient;
  export const type = new ProtocolRequestType<ApplyWorkspaceEditParams, boolean, never, void, void>(method);
}

export interface ApplyWorkspaceEditParams {
  /**
   * An optional label of the workspace edit. This label is
   * presented in the user interface for example on an undo
   * stack to undo the workspace edit.
   */
  label?: string;
  /**
   * The edits to apply.
   */
  edit: WorkspaceEdit;
  options?: {
    /**
     * Add undo stop before making the edits.
     */
    readonly undoStopBefore: boolean;
    /**
     * Add undo stop after making the edits.
     */
    readonly undoStopAfter: boolean;
  };
}

export type ChatEditResolveCommand = LspCommand & {
  title: string;
  tooltip?: string;
  command: "pilotcodi/chat/edit/resolve";
  arguments: [ChatEditResolveParams];
};

/**
 * [PilotCodi] Smart Apply Request(↩️)
 *
 * This method is sent from the client to the server to smart apply the text to the target location.
 * The server will edit the document content using ApplyEdit(`workspace/applyEdit`) request,
 * which requires the client to have this capability.
 * - method: `pilotcodi/chat/smartApply`
 * - params: {@link SmartApplyParams}
 * - result: boolean
 * - error: {@link ChatFeatureNotAvailableError}
 *        | {@link ChatEditDocumentTooLongError}
 *        | {@link ChatEditMutexError}
 */
export namespace SmartApplyRequest {
  export const method = "pilotcodi/chat/smartApply";
  export const messageDirection = MessageDirection.clientToServer;
  export const type = new ProtocolRequestType<
    SmartApplyParams,
    boolean,
    void,
    ChatFeatureNotAvailableError | ChatEditDocumentTooLongError | ChatEditMutexError,
    void
  >(method);
}

export type SmartApplyParams = {
  location: Location;
  text: string;
};

/**
 * [PilotCodi] Did Change Active Editor Notification(➡️)
 *
 * This method is sent from the client to server when the active editor changed.
 *
 *
 * - method: `pilotcodi/editors/didChangeActiveEditor`
 * - params: {@link OpenedFileParams}
 * - result: void
 */
export namespace DidChangeActiveEditorNotification {
  export const method = "pilotcodi/editors/didChangeActiveEditor";
  export const messageDirection = MessageDirection.clientToServer;
  export const type = new ProtocolNotificationType<DidChangeActiveEditorParams, void>(method);
}
export type DidChangeActiveEditorParams = {
  activeEditor: Location;
  visibleEditors: Location[] | undefined;
};

/**
 * [PilotCodi] GenerateCommitMessage Request(↩️)
 *
 * This method is sent from the client to the server to generate a commit message for a git repository.
 * - method: `pilotcodi/chat/generateCommitMessage`
 * - params: {@link GenerateCommitMessageParams}
 * - result: {@link GenerateCommitMessageResult} | null
 * - error: {@link ChatFeatureNotAvailableError}
 */
export namespace GenerateCommitMessageRequest {
  export const method = "pilotcodi/chat/generateCommitMessage";
  export const messageDirection = MessageDirection.clientToServer;
  export const type = new ProtocolRequestType<
    GenerateCommitMessageParams,
    GenerateCommitMessageResult | null,
    void,
    ChatFeatureNotAvailableError,
    void
  >(method);
}

export type GenerateCommitMessageParams = {
  /**
   * The root URI of the git repository.
   */
  repository: URI;
};

export type GenerateCommitMessageResult = {
  commitMessage: string;
};

/**
 * [PilotCodi] GenerateBranchName Request(↩️)
 *
 * This method is sent from the client to the server to generate a branch name for a git repository.
 * - method: `pilotcodi/chat/generateBranchName`
 * - params: {@link GenerateBranchNameParams}
 * - result: {@link GenerateBranchNameResult} | null
 * - error: {@link ChatFeatureNotAvailableError}
 */
export namespace GenerateBranchNameRequest {
  export const method = "pilotcodi/chat/generateBranchName";
  export const messageDirection = MessageDirection.clientToServer;
  export const type = new ProtocolRequestType<
    GenerateBranchNameParams,
    GenerateBranchNameResult | null,
    void,
    ChatFeatureNotAvailableError,
    void
  >(method);
}

export type GenerateBranchNameParams = {
  /**
   * The root URI of the git repository.
   */
  repository: URI;
  input: string;
};

export type GenerateBranchNameResult = {
  branchNames: string[];
};

/**
 * [PilotCodi] Telemetry Event Notification(➡️)
 *
 * This method is sent from the client to the server for telemetry purposes.
 * - method: `pilotcodi/telemetry/event`
 * - params: {@link EventParams}
 * - result: void
 */
export namespace TelemetryEventNotification {
  export const method = "pilotcodi/telemetry/event";
  export const messageDirection = MessageDirection.clientToServer;
  export const type = new ProtocolNotificationType<EventParams, void>(method);
}

export type EventParams = {
  type: "view" | "select" | "dismiss";
  selectKind?: "line";
  eventId: CompletionEventId;
  viewId?: string;
  elapsed?: number;
};

/**
 * [PilotCodi] Config Request(↩️)
 *
 * This method is sent from the client to the server to get the current configuration.
 * - method: `pilotcodi/config`
 * - params: any, not used
 * - result: {@link Config}
 */
export namespace ConfigRequest {
  export const method = "pilotcodi/config";
  export const messageDirection = MessageDirection.clientToServer;
  export const type = new ProtocolRequestType<LSPAny, Config, never, void, void>(method);
}

export type Config = {
  server: {
    endpoint: string;
    token: string;
    requestHeaders: Record<string, string | number | boolean | null | undefined>;
  };
};

/**
 * [PilotCodi] Config DidChange Notification(⬅️)
 *
 * This method is sent from the server to the client to notify the client of the configuration changes.
 * - method: `pilotcodi/config/didChange`
 * - params: {@link Config}
 * - result: void
 */
export namespace ConfigDidChangeNotification {
  export const method = "pilotcodi/config/didChange";
  export const messageDirection = MessageDirection.serverToClient;
  export const type = new ProtocolNotificationType<Config, void>(method);
}

/**
 * [PilotCodi] Status Request(↩️)
 *
 * This method is sent from the client to the server to check the current status of the server.
 * - method: `pilotcodi/status`
 * - params: {@link StatusRequestParams}
 * - result: {@link StatusInfo}
 */
export namespace StatusRequest {
  export const method = "pilotcodi/status";
  export const messageDirection = MessageDirection.clientToServer;
  export const type = new ProtocolRequestType<StatusRequestParams, StatusInfo, never, void, void>(method);
}

export type StatusRequestParams = {
  /**
   * Forces a recheck of the connection to the PilotCodi server, and waiting for result.
   */
  recheckConnection?: boolean;
};

/**
 * [PilotCodi] StatusInfo is used to display the status bar in the editor.
 */
export type StatusInfo = {
  status:
    | "connecting"
    | "unauthorized"
    | "disconnected"
    | "ready"
    | "readyForAutoTrigger"
    | "readyForManualTrigger"
    | "fetching"
    | "codeCompletionNotAvailable"
    | "rateLimitExceeded"
    | "completionResponseSlow";
  tooltip?: string;
  /**
   * The health information of the server if available.
   */
  serverHealth?: Record<string, unknown>;
  /**
   * The action to take for this status.
   * - `disconnected`, `codeCompletionNotAvailable`, `rateLimitExceeded` or `completionResponseSlow` -> StatusShowHelpMessageCommand
   * - others -> undefined
   */
  command?: StatusShowHelpMessageCommand | LspCommand;
  /**
   * The help message if available.
   * Only available when this status info is returned from {@link StatusRequest}, not provided in {@link StatusDidChangeNotification}.
   * Only available when the status is `disconnected`, `codeCompletionNotAvailable`, `rateLimitExceeded` or `completionResponseSlow`.
   */
  helpMessage?: string;
};

/**
 * [PilotCodi] Status DidChange Notification(⬅️)
 *
 * This method is sent from the server to the client to notify the client of the status changes.
 * - method: `pilotcodi/status/didChange`
 * - params: {@link StatusInfo}
 * - result: void
 */
export namespace StatusDidChangeNotification {
  export const method = "pilotcodi/status/didChange";
  export const messageDirection = MessageDirection.serverToClient;
  export const type = new ProtocolNotificationType<StatusInfo, void>(method);
}

/**
 * [PilotCodi] Status Show Help Message Request(↩️)
 *
 * This method is sent from the client to the server to request to show the help message for the current status.
 * The server will callback client to show request using ShowMessageRequest (`window/showMessageRequest`).
 * - method: `pilotcodi/status/showHelpMessage`
 * - params: any, not used
 * - result: boolean
 */
export namespace StatusShowHelpMessageRequest {
  export const method = "pilotcodi/status/showHelpMessage";
  export const messageDirection = MessageDirection.clientToServer;
  export const type = new ProtocolRequestType<LSPAny, boolean, never, void, void>(method);
}

export type StatusShowHelpMessageCommand = LspCommand & {
  title: string;
  command: "pilotcodi/status/showHelpMessage";
  arguments: [LSPAny];
};

/**
 * [PilotCodi] Status Ignored Issues Edit Request(↩️)
 *
 * This method is sent from the client to the server to add or remove the issues that marked as ignored.
 * - method: `pilotcodi/status/ignoredIssues/edit`
 * - params: {@link StatusIgnoredIssuesEditParams}
 * - result: boolean
 */
export namespace StatusIgnoredIssuesEditRequest {
  export const method = "pilotcodi/status/ignoredIssues/edit";
  export const messageDirection = MessageDirection.clientToServer;
  export const type = new ProtocolRequestType<StatusIgnoredIssuesEditParams, boolean, never, void, void>(method);
}

export type StatusIssuesName = "completionResponseSlow";

export type StatusIgnoredIssuesEditParams = {
  operation: "add" | "remove" | "removeAll";
  issues: StatusIssuesName | StatusIssuesName[];
};

/**
 * [PilotCodi] Read File Request(↪️)
 *
 * This method is sent from the server to the client to read the file contents.
 * - method: `pilotcodi/workspaceFileSystem/readFile`
 * - params: {@link ReadFileParams}
 * - result: {@link ReadFileResult} | null
 */
export namespace ReadFileRequest {
  export const method = "pilotcodi/workspaceFileSystem/readFile";
  export const messageDirection = MessageDirection.serverToClient;
  export const type = new ProtocolRequestType<ReadFileParams, ReadFileResult | null, never, void, void>(method);
}

export type ReadFileParams = {
  uri: URI;
  /**
   * If `text` is select, the result should try to decode the file contents to string,
   * otherwise, the result should be a raw binary array.
   */
  format: "text";
  /**
   * When omitted, read the whole file.
   */
  range?: Range;
};

export type ReadFileResult = {
  /**
   * If `text` is select, the result should be a string.
   */
  text?: string;
};

/**
 * [PilotCodi] DataStore DidUpdate Notification(➡️)
 *
 * This method is sent from the client to the server to notify that the data store records has been updated.
 * - method: `pilotcodi/dataStore/didUpdate`
 * - params: {@link DataStoreDidChangeParams}
 */
export namespace DataStoreDidUpdateNotification {
  export const method = "pilotcodi/dataStore/didUpdate";
  export const messageDirection = MessageDirection.clientToServer;
  export const type = new ProtocolNotificationType<DataStoreRecords, void>(method);
}

/**
 * [PilotCodi] DataStore Update Request(↪️)
 *
 * This method is sent from the server to the client to update the data store records.
 * - method: `pilotcodi/dataStore/update`
 * - params: {@link DataStoreUpdateParams}
 * - result: boolean
 */
export namespace DataStoreUpdateRequest {
  export const method = "pilotcodi/dataStore/update";
  export const messageDirection = MessageDirection.serverToClient;
  export const type = new ProtocolRequestType<DataStoreRecords, boolean, never, void, void>(method);
}

export type DataStoreRecords = Record<string, any>;

/**
 * [PilotCodi] Language Support Declaration Request(↪️)
 *
 * This method is sent from the server to the client to request the support from another language server.
 * See LSP `textDocument/declaration`.
 * - method: `pilotcodi/languageSupport/textDocument/declaration`
 * - params: {@link DeclarationParams}
 * - result: {@link Declaration} | {@link LocationLink}[] | null
 */
export namespace LanguageSupportDeclarationRequest {
  export const method = "pilotcodi/languageSupport/textDocument/declaration";
  export const messageDirection = MessageDirection.serverToClient;
  export const type = new ProtocolRequestType<
    DeclarationParams,
    Declaration | LocationLink[] | null,
    never,
    void,
    void
  >(method);
}

/**
 * [PilotCodi] Semantic Tokens Range Request(↪️)
 *
 * This method is sent from the server to the client to request the support from another language server.
 * See LSP `textDocument/semanticTokens/range`.
 * - method: `pilotcodi/languageSupport/textDocument/semanticTokens/range`
 * - params: {@link SemanticTokensRangeParams}
 * - result: {@link SemanticTokensRangeResult} | null
 */
export namespace LanguageSupportSemanticTokensRangeRequest {
  export const method = "pilotcodi/languageSupport/textDocument/semanticTokens/range";
  export const messageDirection = MessageDirection.serverToClient;
  export const type = new ProtocolRequestType<
    SemanticTokensRangeParams,
    SemanticTokensRangeResult | null,
    never,
    void,
    void
  >(method);
}

export type SemanticTokensRangeResult = {
  legend: SemanticTokensLegend;
  tokens: SemanticTokens;
};

/**
 * [PilotCodi] Git Repository Request(↪️)
 *
 * This method is sent from the server to the client to get the git repository state of a file.
 * - method: `pilotcodi/git/repository`
 * - params: {@link GitRepositoryParams}
 * - result: {@link GitRepository} | null
 */
export namespace GitRepositoryRequest {
  export const method = "pilotcodi/git/repository";
  export const messageDirection = MessageDirection.serverToClient;
  export const type = new ProtocolRequestType<GitRepositoryParams, GitRepository | null, never, void, void>(method);
}

export type GitRepositoryParams = {
  /**
   * The URI of the file to get the git repository state of.
   */
  uri: URI;
};

export type GitRepository = {
  /**
   * The root URI of the git repository.
   */
  root: URI;
  /**
   * The url of the default remote.
   */
  remoteUrl?: string;
  /**
   * List of remotes in the git repository.
   */
  remotes?: {
    name: string;
    url: string;
  }[];
};

/**
 * [PilotCodi] Git Diff Request(↪️)
 *
 * This method is sent from the server to the client to get the diff of a git repository.
 * - method: `pilotcodi/git/diff`
 * - params: {@link GitDiffParams}
 * - result: {@link GitDiffResult} | null
 */
export namespace GitDiffRequest {
  export const method = "pilotcodi/git/diff";
  export const messageDirection = MessageDirection.serverToClient;
  export const type = new ProtocolRequestType<GitDiffParams, GitDiffResult | null, never, void, void>(method);
}

export type GitDiffParams = {
  /**
   * The root URI of the git repository.
   */
  repository: URI;
  /**
   * Returns the cached or uncached diff of the git repository.
   */
  cached: boolean;
};

export type GitDiffResult = {
  /**
   * The diff of the git repository.
   * - It could be the full diff.
   * - It could be a list of diff for each single file, sorted by the priority.
   *   This will be useful when the full diff is too large, and we will select
   *   from the split diffs to generate a prompt under the tokens limit.
   */
  diff: string | string[];
};

/**
 * [PilotCodi] Editor Options Request(↪️)
 *
 * This method is sent from the server to the client to get the diff of a git repository.
 * - method: `pilotcodi/editorOptions`
 * - params: {@link EditorOptionsParams}
 * - result: {@link EditorOptions} | null
 */
export namespace EditorOptionsRequest {
  export const method = "pilotcodi/editorOptions";
  export const messageDirection = MessageDirection.serverToClient;
  export const type = new ProtocolRequestType<EditorOptionsParams, EditorOptions | null, never, void, void>(method);
}

export type EditorOptionsParams = {
  /**
   * The uri of the document for which the editor options are requested.
   */
  uri: URI;
};

export type EditorOptions = {
  /**
   * A string representing the indentation for the editor. It could be 2 or 4 spaces, or 1 tab.
   */
  indentation: string;
};
