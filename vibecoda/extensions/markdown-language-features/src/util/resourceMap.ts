/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as vibecoda from 'vibecoda';

type ResourceToKey = (uri: vibecoda.Uri) => string;

const defaultResourceToKey = (resource: vibecoda.Uri): string => resource.toString();

export class ResourceMap<T> {

	private readonly _map = new Map<string, { readonly uri: vibecoda.Uri; readonly value: T }>();

	private readonly _toKey: ResourceToKey;

	constructor(toKey: ResourceToKey = defaultResourceToKey) {
		this._toKey = toKey;
	}

	public set(uri: vibecoda.Uri, value: T): this {
		this._map.set(this._toKey(uri), { uri, value });
		return this;
	}

	public get(resource: vibecoda.Uri): T | undefined {
		return this._map.get(this._toKey(resource))?.value;
	}

	public has(resource: vibecoda.Uri): boolean {
		return this._map.has(this._toKey(resource));
	}

	public get size(): number {
		return this._map.size;
	}

	public clear(): void {
		this._map.clear();
	}

	public delete(resource: vibecoda.Uri): boolean {
		return this._map.delete(this._toKey(resource));
	}

	public *values(): IterableIterator<T> {
		for (const entry of this._map.values()) {
			yield entry.value;
		}
	}

	public *keys(): IterableIterator<vibecoda.Uri> {
		for (const entry of this._map.values()) {
			yield entry.uri;
		}
	}

	public *entries(): IterableIterator<[vibecoda.Uri, T]> {
		for (const entry of this._map.values()) {
			yield [entry.uri, entry.value];
		}
	}

	public [Symbol.iterator](): IterableIterator<[vibecoda.Uri, T]> {
		return this.entries();
	}
}
