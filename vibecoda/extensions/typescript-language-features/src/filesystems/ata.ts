/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as vibecoda from 'vibecoda';
import { conditionalRegistration, requireGlobalConfiguration } from '../languageFeatures/util/dependentRegistration';
import { supportsReadableByteStreams } from '../utils/platform';
import { AutoInstallerFs } from './autoInstallerFs';
import { MemFs } from './memFs';
import { Logger } from '../logging/logger';

export function registerAtaSupport(logger: Logger): vibecoda.Disposable {
	if (!supportsReadableByteStreams()) {
		return vibecoda.Disposable.from();
	}

	return conditionalRegistration([
		requireGlobalConfiguration('typescript', 'tsserver.web.typeAcquisition.enabled'),
	], () => {
		return vibecoda.Disposable.from(
			// Ata
			vibecoda.workspace.registerFileSystemProvider('vibecoda-global-typings', new MemFs('global-typings', logger), {
				isCaseSensitive: true,
				isReadonly: false,
			}),

			// Read accesses to node_modules
			vibecoda.workspace.registerFileSystemProvider('vibecoda-node-modules', new AutoInstallerFs(logger), {
				isCaseSensitive: true,
				isReadonly: false
			}));
	});
}
