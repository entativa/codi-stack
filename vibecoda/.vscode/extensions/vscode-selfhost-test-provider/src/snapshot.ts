/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import { promises as fs } from 'fs';
import * as vibecoda from 'vibecoda';

export const snapshotComment = '\n\n// Snapshot file: ';

export const registerSnapshotUpdate = (ctrl: vibecoda.TestController) =>
	vibecoda.commands.registerCommand('selfhost-test-provider.updateSnapshot', async args => {
		const message: vibecoda.TestMessage = args.message;
		const index = message.expectedOutput?.indexOf(snapshotComment);
		if (!message.expectedOutput || !message.actualOutput || !index || index === -1) {
			vibecoda.window.showErrorMessage('Could not find snapshot comment in message');
			return;
		}

		const file = message.expectedOutput.slice(index + snapshotComment.length);
		await fs.writeFile(file, message.actualOutput);
		ctrl.invalidateTestResults(args.test);
	});
