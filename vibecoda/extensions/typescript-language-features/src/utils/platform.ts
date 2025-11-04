/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as vibecoda from 'vibecoda';

export function isWeb(): boolean {
	return !(typeof process === 'object' && !!process.versions.node) && vibecoda.env.uiKind === vibecoda.UIKind.Web;
}

export function isWebAndHasSharedArrayBuffers(): boolean {
	return isWeb() && !!(globalThis as Record<string, unknown>)['crossOriginIsolated'];
}

export function supportsReadableByteStreams(): boolean {
	return isWeb() && 'ReadableByteStreamController' in globalThis;
}
