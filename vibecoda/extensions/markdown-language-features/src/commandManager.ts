/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as vibecoda from 'vibecoda';

export interface Command {
	readonly id: string;

	execute(...args: any[]): void;
}

export class CommandManager {
	private readonly _commands = new Map<string, vibecoda.Disposable>();

	public dispose() {
		for (const registration of this._commands.values()) {
			registration.dispose();
		}
		this._commands.clear();
	}

	public register<T extends Command>(command: T): vibecoda.Disposable {
		this._registerCommand(command.id, command.execute, command);
		return new vibecoda.Disposable(() => {
			this._commands.delete(command.id);
		});
	}

	private _registerCommand(id: string, impl: (...args: any[]) => void, thisArg?: any) {
		if (this._commands.has(id)) {
			return;
		}

		this._commands.set(id, vibecoda.commands.registerCommand(id, impl, thisArg));
	}
}
