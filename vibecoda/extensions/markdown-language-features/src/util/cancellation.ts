/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as vibecoda from 'vibecoda';

export const noopToken: vibecoda.CancellationToken = new class implements vibecoda.CancellationToken {
	private readonly _onCancellationRequestedEmitter = new vibecoda.EventEmitter<void>();
	onCancellationRequested = this._onCancellationRequestedEmitter.event;

	get isCancellationRequested() { return false; }
};
