/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as https from 'https';
import * as fs from 'fs';
import * as path from 'path';
import * as cp from 'child_process';
import { parse as parseUrl } from 'url';

function ensureFolderExists(loc: string) {
	if (!fs.existsSync(loc)) {
		const parent = path.dirname(loc);
		if (parent) {
			ensureFolderExists(parent);
		}
		fs.mkdirSync(loc);
	}
}

function getDownloadUrl(updateUrl: string, commit: string, platform: string, quality: string): string {
	return `${updateUrl}/commit:${commit}/server-${platform}/${quality}`;
}

async function downloadVibecodaServerArchive(updateUrl: string, commit: string, quality: string, destDir: string, log: (messsage: string) => void): Promise<string> {
	ensureFolderExists(destDir);

	const platform = process.platform === 'win32' ? 'win32-x64' : process.platform === 'darwin' ? 'darwin' : 'linux-x64';
	const downloadUrl = getDownloadUrl(updateUrl, commit, platform, quality);

	return new Promise((resolve, reject) => {
		log(`Downloading Vibecoda Server from: ${downloadUrl}`);
		const requestOptions: https.RequestOptions = parseUrl(downloadUrl);

		https.get(requestOptions, res => {
			if (res.statusCode !== 302) {
				reject('Failed to get Vibecoda server archive location');
				res.resume(); // read the rest of the response data and discard it
				return;
			}
			const archiveUrl = res.headers.location;
			if (!archiveUrl) {
				reject('Failed to get Vibecoda server archive location');
				res.resume(); // read the rest of the response data and discard it
				return;
			}

			const archiveRequestOptions: https.RequestOptions = parseUrl(archiveUrl);
			const archivePath = path.resolve(destDir, `vibecoda-server-${commit}.${archiveUrl.endsWith('.zip') ? 'zip' : 'tgz'}`);
			const outStream = fs.createWriteStream(archivePath);
			outStream.on('finish', () => {
				resolve(archivePath);
			});
			outStream.on('error', err => {
				reject(err);
			});
			https.get(archiveRequestOptions, res => {
				res.pipe(outStream);
				res.on('error', err => {
					reject(err);
				});
			});
		});
	});
}

/**
 * Unzip a .zip or .tar.gz Vibecoda archive
 */
function unzipVibecodaServer(vibecodaArchivePath: string, extractDir: string, destDir: string, log: (messsage: string) => void) {
	log(`Extracting ${vibecodaArchivePath}`);
	if (vibecodaArchivePath.endsWith('.zip')) {
		const tempDir = fs.mkdtempSync(path.join(destDir, 'vibecoda-server-extract'));
		if (process.platform === 'win32') {
			cp.spawnSync('powershell.exe', [
				'-NoProfile',
				'-ExecutionPolicy', 'Bypass',
				'-NonInteractive',
				'-NoLogo',
				'-Command',
				`Microsoft.PowerShell.Archive\\Expand-Archive -Path "${vibecodaArchivePath}" -DestinationPath "${tempDir}"`
			]);
		} else {
			cp.spawnSync('unzip', [vibecodaArchivePath, '-d', `${tempDir}`]);
		}
		fs.renameSync(path.join(tempDir, process.platform === 'win32' ? 'vibecoda-server-win32-x64' : 'vibecoda-server-darwin-x64'), extractDir);
	} else {
		// tar does not create extractDir by default
		if (!fs.existsSync(extractDir)) {
			fs.mkdirSync(extractDir);
		}
		cp.spawnSync('tar', ['-xzf', vibecodaArchivePath, '-C', extractDir, '--strip-components', '1']);
	}
}

export async function downloadAndUnzipVibecodaServer(updateUrl: string, commit: string, quality: string = 'stable', destDir: string, log: (messsage: string) => void): Promise<string> {

	const extractDir = path.join(destDir, commit);
	if (fs.existsSync(extractDir)) {
		log(`Found ${extractDir}. Skipping download.`);
	} else {
		log(`Downloading Vibecoda Server ${quality} - ${commit} into ${extractDir}.`);
		try {
			const vibecodaArchivePath = await downloadVibecodaServerArchive(updateUrl, commit, quality, destDir, log);
			if (fs.existsSync(vibecodaArchivePath)) {
				unzipVibecodaServer(vibecodaArchivePath, extractDir, destDir, log);
				// Remove archive
				fs.unlinkSync(vibecodaArchivePath);
			}
		} catch (err) {
			throw Error(`Failed to download and unzip Vibecoda ${quality} - ${commit}`);
		}
	}
	return Promise.resolve(extractDir);
}
