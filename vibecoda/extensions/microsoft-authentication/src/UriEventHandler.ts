/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as vibecoda from 'vibecoda';

export class UriEventHandler extends vibecoda.EventEmitter<vibecoda.Uri> implements vibecoda.UriHandler {
	private _disposable = vibecoda.window.registerUriHandler(this);

	handleUri(uri: vibecoda.Uri) {
		this.fire(uri);
	}

	override dispose(): void {
		super.dispose();
		this._disposable.dispose();
	}
}
