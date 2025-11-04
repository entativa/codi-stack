/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import { UriParts, IRawURITransformer, URITransformer, IURITransformer } from './uriIpc.js';

/**
 * ```
 * --------------------------------
 * |    UI SIDE    |  AGENT SIDE  |
 * |---------------|--------------|
 * | vibecoda-remote | file         |
 * | file          | vibecoda-local |
 * --------------------------------
 * ```
 */
function createRawURITransformer(remoteAuthority: string): IRawURITransformer {
	return {
		transformIncoming: (uri: UriParts): UriParts => {
			if (uri.scheme === 'vibecoda-remote') {
				return { scheme: 'file', path: uri.path, query: uri.query, fragment: uri.fragment };
			}
			if (uri.scheme === 'file') {
				return { scheme: 'vibecoda-local', path: uri.path, query: uri.query, fragment: uri.fragment };
			}
			return uri;
		},
		transformOutgoing: (uri: UriParts): UriParts => {
			if (uri.scheme === 'file') {
				return { scheme: 'vibecoda-remote', authority: remoteAuthority, path: uri.path, query: uri.query, fragment: uri.fragment };
			}
			if (uri.scheme === 'vibecoda-local') {
				return { scheme: 'file', path: uri.path, query: uri.query, fragment: uri.fragment };
			}
			return uri;
		},
		transformOutgoingScheme: (scheme: string): string => {
			if (scheme === 'file') {
				return 'vibecoda-remote';
			} else if (scheme === 'vibecoda-local') {
				return 'file';
			}
			return scheme;
		}
	};
}

export function createURITransformer(remoteAuthority: string): IURITransformer {
	return new URITransformer(createRawURITransformer(remoteAuthority));
}
