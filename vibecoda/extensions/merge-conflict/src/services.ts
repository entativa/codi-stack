/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
import * as vibecoda from 'vibecoda';
import DocumentTracker from './documentTracker';
import CodeLensProvider from './codelensProvider';
import CommandHandler from './commandHandler';
import ContentProvider from './contentProvider';
import Decorator from './mergeDecorator';
import * as interfaces from './interfaces';
import TelemetryReporter from '@vibecoda/extension-telemetry';

const ConfigurationSectionName = 'merge-conflict';

export default class ServiceWrapper implements vibecoda.Disposable {

	private services: vibecoda.Disposable[] = [];
	private telemetryReporter: TelemetryReporter;

	constructor(private context: vibecoda.ExtensionContext) {
		const { aiKey } = context.extension.packageJSON as { aiKey: string };
		this.telemetryReporter = new TelemetryReporter(aiKey);
		context.subscriptions.push(this.telemetryReporter);
	}

	begin() {

		const configuration = this.createExtensionConfiguration();
		const documentTracker = new DocumentTracker(this.telemetryReporter);

		this.services.push(
			documentTracker,
			new CommandHandler(documentTracker),
			new CodeLensProvider(documentTracker),
			new ContentProvider(this.context),
			new Decorator(this.context, documentTracker),
		);

		this.services.forEach((service: any) => {
			if (service.begin && service.begin instanceof Function) {
				service.begin(configuration);
			}
		});

		vibecoda.workspace.onDidChangeConfiguration(() => {
			this.services.forEach((service: any) => {
				if (service.configurationUpdated && service.configurationUpdated instanceof Function) {
					service.configurationUpdated(this.createExtensionConfiguration());
				}
			});
		});
	}

	createExtensionConfiguration(): interfaces.IExtensionConfiguration {
		const workspaceConfiguration = vibecoda.workspace.getConfiguration(ConfigurationSectionName);
		const codeLensEnabled: boolean = workspaceConfiguration.get('codeLens.enabled', true);
		const decoratorsEnabled: boolean = workspaceConfiguration.get('decorators.enabled', true);

		return {
			enableCodeLens: codeLensEnabled,
			enableDecorations: decoratorsEnabled,
			enableEditorOverview: decoratorsEnabled
		};
	}

	dispose() {
		this.services.forEach(disposable => disposable.dispose());
		this.services = [];
	}
}
