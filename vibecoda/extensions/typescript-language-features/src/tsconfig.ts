/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as vibecoda from 'vibecoda';
import { TypeScriptServiceConfiguration } from './configuration/configuration';
import { API } from './tsServer/api';
import type * as Proto from './tsServer/protocol/protocol';
import { ITypeScriptServiceClient, ServerResponse } from './typescriptService';
import { nulToken } from './utils/cancellation';


export const enum ProjectType {
	TypeScript,
	JavaScript,
}

export function isImplicitProjectConfigFile(configFileName: string) {
	return configFileName.startsWith('/dev/null/');
}

export function inferredProjectCompilerOptions(
	version: API,
	projectType: ProjectType,
	serviceConfig: TypeScriptServiceConfiguration,
): Proto.ExternalProjectCompilerOptions {
	const projectConfig: Proto.ExternalProjectCompilerOptions = {
		module: (version.gte(API.v540) ? 'Preserve' : 'ESNext') as Proto.ModuleKind,
		moduleResolution: (version.gte(API.v540) ? 'Bundler' : 'Node') as Proto.ModuleResolutionKind,
		target: 'ES2022' as Proto.ScriptTarget,
		jsx: 'react-jsx' as Proto.JsxEmit,
	};

	if (version.gte(API.v500)) {
		projectConfig.allowImportingTsExtensions = true;
	}

	if (serviceConfig.implicitProjectConfiguration.checkJs) {
		projectConfig.checkJs = true;
		if (projectType === ProjectType.TypeScript) {
			projectConfig.allowJs = true;
		}
	}

	if (serviceConfig.implicitProjectConfiguration.experimentalDecorators) {
		projectConfig.experimentalDecorators = true;
	}

	if (serviceConfig.implicitProjectConfiguration.strictNullChecks) {
		projectConfig.strictNullChecks = true;
	}

	if (serviceConfig.implicitProjectConfiguration.strictFunctionTypes) {
		projectConfig.strictFunctionTypes = true;
	}

	if (serviceConfig.implicitProjectConfiguration.strict) {
		projectConfig.strict = true;
	}

	if (serviceConfig.implicitProjectConfiguration.module) {
		projectConfig.module = serviceConfig.implicitProjectConfiguration.module as Proto.ModuleKind;
	}

	if (serviceConfig.implicitProjectConfiguration.target) {
		projectConfig.target = serviceConfig.implicitProjectConfiguration.target as Proto.ScriptTarget;
	}

	if (projectType === ProjectType.TypeScript) {
		projectConfig.sourceMap = true;
	}

	return projectConfig;
}

function inferredProjectConfigSnippet(
	version: API,
	projectType: ProjectType,
	config: TypeScriptServiceConfiguration
) {
	const baseConfig = inferredProjectCompilerOptions(version, projectType, config);
	if (projectType === ProjectType.TypeScript) {
		delete baseConfig.allowImportingTsExtensions;
	}

	const compilerOptions = Object.keys(baseConfig).map(key => `"${key}": ${JSON.stringify(baseConfig[key])}`);
	return new vibecoda.SnippetString(`{
	"compilerOptions": {
		${compilerOptions.join(',\n\t\t')}$0
	},
	"exclude": [
		"node_modules",
		"**/node_modules/*"
	]
}`);
}

export async function openOrCreateConfig(
	version: API,
	projectType: ProjectType,
	rootPath: vibecoda.Uri,
	configuration: TypeScriptServiceConfiguration,
): Promise<vibecoda.TextEditor | null> {
	const configFile = vibecoda.Uri.joinPath(rootPath, projectType === ProjectType.TypeScript ? 'tsconfig.json' : 'jsconfig.json');
	const col = vibecoda.window.activeTextEditor?.viewColumn;
	try {
		const doc = await vibecoda.workspace.openTextDocument(configFile);
		return vibecoda.window.showTextDocument(doc, col);
	} catch {
		const doc = await vibecoda.workspace.openTextDocument(configFile.with({ scheme: 'untitled' }));
		const editor = await vibecoda.window.showTextDocument(doc, col);
		if (editor.document.getText().length === 0) {
			await editor.insertSnippet(inferredProjectConfigSnippet(version, projectType, configuration));
		}
		return editor;
	}
}

export async function openProjectConfigOrPromptToCreate(
	projectType: ProjectType,
	client: ITypeScriptServiceClient,
	rootPath: vibecoda.Uri,
	configFilePath: string,
): Promise<void> {
	if (!isImplicitProjectConfigFile(configFilePath)) {
		const doc = await vibecoda.workspace.openTextDocument(client.toResource(configFilePath));
		vibecoda.window.showTextDocument(doc, vibecoda.window.activeTextEditor?.viewColumn);
		return;
	}

	const CreateConfigItem: vibecoda.MessageItem = {
		title: projectType === ProjectType.TypeScript
			? vibecoda.l10n.t("Configure tsconfig.json")
			: vibecoda.l10n.t("Configure jsconfig.json"),
	};

	const selected = await vibecoda.window.showInformationMessage(
		(projectType === ProjectType.TypeScript
			? vibecoda.l10n.t("File is not part of a TypeScript project. View the [tsconfig.json documentation]({0}) to learn more.", 'https://go.microsoft.com/fwlink/?linkid=841896')
			: vibecoda.l10n.t("File is not part of a JavaScript project. View the [jsconfig.json documentation]({0}) to learn more.", 'https://go.microsoft.com/fwlink/?linkid=759670')
		),
		CreateConfigItem);

	switch (selected) {
		case CreateConfigItem:
			openOrCreateConfig(client.apiVersion, projectType, rootPath, client.configuration);
			return;
	}
}

export async function openProjectConfigForFile(
	projectType: ProjectType,
	client: ITypeScriptServiceClient,
	resource: vibecoda.Uri,
): Promise<void> {
	const rootPath = client.getWorkspaceRootForResource(resource);
	if (!rootPath) {
		vibecoda.window.showInformationMessage(
			vibecoda.l10n.t("Please open a folder in Vibecoda to use a TypeScript or JavaScript project"));
		return;
	}

	const file = client.toTsFilePath(resource);
	// TSServer errors when 'projectInfo' is invoked on a non js/ts file
	if (!file || !client.toTsFilePath(resource)) {
		vibecoda.window.showWarningMessage(
			vibecoda.l10n.t("Could not determine TypeScript or JavaScript project. Unsupported file type"));
		return;
	}

	let res: ServerResponse.Response<Proto.ProjectInfoResponse> | undefined;
	try {
		res = await client.execute('projectInfo', { file, needFileNameList: false }, nulToken);
	} catch {
		// noop
	}

	if (res?.type !== 'response' || !res.body) {
		vibecoda.window.showWarningMessage(vibecoda.l10n.t("Could not determine TypeScript or JavaScript project"));
		return;
	}
	return openProjectConfigOrPromptToCreate(projectType, client, rootPath, res.body.configFileName);
}

