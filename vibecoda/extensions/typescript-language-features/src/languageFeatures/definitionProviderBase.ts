/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as vibecoda from 'vibecoda';
import * as typeConverters from '../typeConverters';
import { ITypeScriptServiceClient } from '../typescriptService';


export default class TypeScriptDefinitionProviderBase {
	constructor(
		protected readonly client: ITypeScriptServiceClient
	) { }

	protected async getSymbolLocations(
		definitionType: 'definition' | 'implementation' | 'typeDefinition',
		document: vibecoda.TextDocument,
		position: vibecoda.Position,
		token: vibecoda.CancellationToken
	): Promise<vibecoda.Location[] | undefined> {
		const file = this.client.toOpenTsFilePath(document);
		if (!file) {
			return undefined;
		}

		const args = typeConverters.Position.toFileLocationRequestArgs(file, position);
		const response = await this.client.execute(definitionType, args, token);
		if (response.type !== 'response' || !response.body) {
			return undefined;
		}

		return response.body.map(location =>
			typeConverters.Location.fromTextSpan(this.client.toResource(location.file), location));
	}
}
