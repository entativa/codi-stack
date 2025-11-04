/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as vibecoda from 'vibecoda';

const Logger = vibecoda.window.createOutputChannel(vibecoda.l10n.t('Microsoft Authentication'), { log: true });
export default Logger;
