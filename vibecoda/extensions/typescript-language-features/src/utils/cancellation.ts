/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as vibecoda from 'vibecoda';

const noopDisposable = vibecoda.Disposable.from();

export const nulToken: vibecoda.CancellationToken = {
	isCancellationRequested: false,
	onCancellationRequested: () => noopDisposable
};
