/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as vibecoda from 'vibecoda';
import { DocumentSelector } from '../configuration/documentSelector';
import { ClientCapability, ITypeScriptServiceClient } from '../typescriptService';
import DefinitionProviderBase from './definitionProviderBase';
import { conditionalRegistration, requireSomeCapability } from './util/dependentRegistration';

class TypeScriptImplementationProvider extends DefinitionProviderBase implements vibecoda.ImplementationProvider {
	public provideImplementation(document: vibecoda.TextDocument, position: vibecoda.Position, token: vibecoda.CancellationToken): Promise<vibecoda.Definition | undefined> {
		return this.getSymbolLocations('implementation', document, position, token);
	}
}

export function register(
	selector: DocumentSelector,
	client: ITypeScriptServiceClient,
) {
	return conditionalRegistration([
		requireSomeCapability(client, ClientCapability.Semantic),
	], () => {
		return vibecoda.languages.registerImplementationProvider(selector.semantic,
			new TypeScriptImplementationProvider(client));
	});
}
