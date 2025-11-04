/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as vibecoda from 'vibecoda';
import { Command } from './commandManager';

export interface OpenJsDocLinkCommand_Args {
	readonly file: {
		readonly scheme: string;
		readonly authority?: string;
		readonly path?: string;
		readonly query?: string;
		readonly fragment?: string;
	};
	readonly position: {
		readonly line: number;
		readonly character: number;
	};
}

/**
 * Proxy command for opening links in jsdoc comments.
 *
 * This is needed to avoid incorrectly rewriting uris.
 */
export class OpenJsDocLinkCommand implements Command {
	public static readonly id = '_typescript.openJsDocLink';
	public readonly id = OpenJsDocLinkCommand.id;

	public async execute(args: OpenJsDocLinkCommand_Args): Promise<void> {
		const { line, character } = args.position;
		const position = new vibecoda.Position(line, character);
		await vibecoda.commands.executeCommand('vibecoda.open', vibecoda.Uri.from(args.file), {
			selection: new vibecoda.Range(position, position),
		} satisfies vibecoda.TextDocumentShowOptions);
	}
}
