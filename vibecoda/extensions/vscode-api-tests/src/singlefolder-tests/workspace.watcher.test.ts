/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as assert from 'assert';
import * as vibecoda from 'vibecoda';
import { TestFS } from '../memfs';
import { assertNoRpc } from '../utils';

suite('vibecoda API - workspace-watcher', () => {

	interface IWatchRequest {
		uri: vibecoda.Uri;
		options: { recursive: boolean; excludes: string[] };
	}

	class WatcherTestFs extends TestFS {

		private _onDidWatch = new vibecoda.EventEmitter<IWatchRequest>();
		readonly onDidWatch = this._onDidWatch.event;

		override watch(uri: vibecoda.Uri, options: { recursive: boolean; excludes: string[] }): vibecoda.Disposable {
			this._onDidWatch.fire({ uri, options });

			return super.watch(uri, options);
		}
	}

	let fs: WatcherTestFs;
	let disposable: vibecoda.Disposable;

	function onDidWatchPromise() {
		const onDidWatchPromise = new Promise<IWatchRequest>(resolve => {
			fs.onDidWatch(request => resolve(request));
		});

		return onDidWatchPromise;
	}

	setup(() => {
		fs = new WatcherTestFs('watcherTest', false);
		disposable = vibecoda.workspace.registerFileSystemProvider('watcherTest', fs);
	});

	teardown(() => {
		disposable.dispose();
		assertNoRpc();
	});

	test('createFileSystemWatcher', async function () {

		// Non-recursive
		let watchUri = vibecoda.Uri.from({ scheme: 'watcherTest', path: '/somePath/folder' });
		const watcher = vibecoda.workspace.createFileSystemWatcher(new vibecoda.RelativePattern(watchUri, '*.txt'));
		let request = await onDidWatchPromise();

		assert.strictEqual(request.uri.toString(), watchUri.toString());
		assert.strictEqual(request.options.recursive, false);

		watcher.dispose();

		// Recursive
		watchUri = vibecoda.Uri.from({ scheme: 'watcherTest', path: '/somePath/folder' });
		vibecoda.workspace.createFileSystemWatcher(new vibecoda.RelativePattern(watchUri, '**/*.txt'));
		request = await onDidWatchPromise();

		assert.strictEqual(request.uri.toString(), watchUri.toString());
		assert.strictEqual(request.options.recursive, true);
	});
});
