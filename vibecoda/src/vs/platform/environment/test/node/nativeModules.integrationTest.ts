/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import assert from 'assert';
import { isMacintosh, isWindows } from '../../../../base/common/platform.js';
import { flakySuite } from '../../../../base/test/common/testUtils.js';

function testErrorMessage(module: string): string {
	return `Unable to load "${module}" dependency. It was probably not compiled for the right operating system architecture or had missing build tools.`;
}

flakySuite('Native Modules (all platforms)', () => {

	(isMacintosh ? test.skip : test)('kerberos', async () => { // Somehow fails on macOS ARM?
		const { default: kerberos } = await import('kerberos');
		assert.ok(typeof kerberos.initializeClient === 'function', testErrorMessage('kerberos'));
	});

	test('yauzl', async () => {
		const { default: yauzl } = await import('yauzl');
		assert.ok(typeof yauzl.ZipFile === 'function', testErrorMessage('yauzl'));
	});

	test('yazl', async () => {
		const { default: yazl } = await import('yazl');
		assert.ok(typeof yazl.ZipFile === 'function', testErrorMessage('yazl'));
	});

	test('v8-inspect-profiler', async () => {
		const { default: profiler } = await import('v8-inspect-profiler');
		assert.ok(typeof profiler.startProfiling === 'function', testErrorMessage('v8-inspect-profiler'));
	});

	test('native-is-elevated', async () => {
		const { default: isElevated } = await import('native-is-elevated');
		assert.ok(typeof isElevated === 'function', testErrorMessage('native-is-elevated '));

		const result = isElevated();
		assert.ok(typeof result === 'boolean', testErrorMessage('native-is-elevated'));
	});

	test('native-keymap', async () => {
		const keyMap = await import('native-keymap');
		assert.ok(typeof keyMap.onDidChangeKeyboardLayout === 'function', testErrorMessage('native-keymap'));
		assert.ok(typeof keyMap.getCurrentKeyboardLayout === 'function', testErrorMessage('native-keymap'));

		const result = keyMap.getCurrentKeyboardLayout();
		assert.ok(result, testErrorMessage('native-keymap'));
	});

	test('native-watchdog', async () => {
		const watchDog = await import('native-watchdog');
		assert.ok(typeof watchDog.start === 'function', testErrorMessage('native-watchdog'));
	});

	test('@vibecoda/sudo-prompt', async () => {
		const prompt = await import('@vibecoda/sudo-prompt');
		assert.ok(typeof prompt.exec === 'function', testErrorMessage('@vibecoda/sudo-prompt'));
	});

	test('@vibecoda/policy-watcher', async () => {
		const watcher = await import('@vibecoda/policy-watcher');
		assert.ok(typeof watcher.createWatcher === 'function', testErrorMessage('@vibecoda/policy-watcher'));
	});

	test('node-pty', async () => {
		const nodePty = await import('node-pty');
		assert.ok(typeof nodePty.spawn === 'function', testErrorMessage('node-pty'));
	});

	test('@vibecoda/spdlog', async () => {
		const spdlog = await import('@vibecoda/spdlog');
		assert.ok(typeof spdlog.createRotatingLogger === 'function', testErrorMessage('@vibecoda/spdlog'));
		assert.ok(typeof spdlog.version === 'number', testErrorMessage('@vibecoda/spdlog'));
	});

	test('@parcel/watcher', async () => {
		const parcelWatcher = await import('@parcel/watcher');
		assert.ok(typeof parcelWatcher.subscribe === 'function', testErrorMessage('@parcel/watcher'));
	});

	test('@vibecoda/deviceid', async () => {
		const deviceIdPackage = await import('@vibecoda/deviceid');
		assert.ok(typeof deviceIdPackage.getDeviceId === 'function', testErrorMessage('@vibecoda/deviceid'));
	});

	test('@vibecoda/ripgrep', async () => {
		const ripgrep = await import('@vibecoda/ripgrep');
		assert.ok(typeof ripgrep.rgPath === 'string', testErrorMessage('@vibecoda/ripgrep'));
	});

	test('vibecoda-regexpp', async () => {
		const regexpp = await import('vibecoda-regexpp');
		assert.ok(typeof regexpp.RegExpParser === 'function', testErrorMessage('vibecoda-regexpp'));
	});

	test('@vibecoda/sqlite3', async () => {
		const { default: sqlite3 } = await import('@vibecoda/sqlite3');
		assert.ok(typeof sqlite3.Database === 'function', testErrorMessage('@vibecoda/sqlite3'));
	});

	test('http-proxy-agent', async () => {
		const { default: mod } = await import('http-proxy-agent');
		assert.ok(typeof mod.HttpProxyAgent === 'function', testErrorMessage('http-proxy-agent'));
	});

	test('https-proxy-agent', async () => {
		const { default: mod } = await import('https-proxy-agent');
		assert.ok(typeof mod.HttpsProxyAgent === 'function', testErrorMessage('https-proxy-agent'));
	});

	test('@vibecoda/proxy-agent', async () => {
		const proxyAgent = await import('@vibecoda/proxy-agent');
		// This call will load `@vibecoda/proxy-agent` which is a native module that we want to test on Windows
		const windowsCerts = await proxyAgent.loadSystemCertificates({
			loadSystemCertificatesFromNode: () => undefined,
			log: {
				trace: () => { },
				debug: () => { },
				info: () => { },
				warn: () => { },
				error: () => { }
			}
		});
		assert.ok(windowsCerts.length > 0, testErrorMessage('@vibecoda/proxy-agent'));
	});
});

(!isWindows ? suite.skip : suite)('Native Modules (Windows)', () => {

	test('@vibecoda/windows-mutex', async () => {
		const mutex = await import('@vibecoda/windows-mutex');
		assert.ok(mutex && typeof mutex.isActive === 'function', testErrorMessage('@vibecoda/windows-mutex'));
		assert.ok(typeof mutex.isActive === 'function', testErrorMessage('@vibecoda/windows-mutex'));
		assert.ok(typeof mutex.Mutex === 'function', testErrorMessage('@vibecoda/windows-mutex'));
	});

	test('windows-foreground-love', async () => {
		const foregroundLove = await import('windows-foreground-love');
		assert.ok(typeof foregroundLove.allowSetForegroundWindow === 'function', testErrorMessage('windows-foreground-love'));

		const result = foregroundLove.allowSetForegroundWindow(process.pid);
		assert.ok(typeof result === 'boolean', testErrorMessage('windows-foreground-love'));
	});

	test('@vibecoda/windows-process-tree', async () => {
		const processTree = await import('@vibecoda/windows-process-tree');
		assert.ok(typeof processTree.getProcessTree === 'function', testErrorMessage('@vibecoda/windows-process-tree'));

		return new Promise((resolve, reject) => {
			processTree.getProcessTree(process.pid, tree => {
				if (tree) {
					resolve();
				} else {
					reject(new Error(testErrorMessage('@vibecoda/windows-process-tree')));
				}
			});
		});
	});

	test('@vibecoda/windows-registry', async () => {
		const windowsRegistry = await import('@vibecoda/windows-registry');
		assert.ok(typeof windowsRegistry.GetStringRegKey === 'function', testErrorMessage('@vibecoda/windows-registry'));

		const result = windowsRegistry.GetStringRegKey('HKEY_LOCAL_MACHINE', 'SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion', 'EditionID');
		assert.ok(typeof result === 'string' || typeof result === 'undefined', testErrorMessage('@vibecoda/windows-registry'));
	});
});
