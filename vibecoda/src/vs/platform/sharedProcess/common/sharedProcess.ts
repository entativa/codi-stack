/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

export const SharedProcessLifecycle = {
	exit: 'vibecoda:electron-main->shared-process=exit',
	ipcReady: 'vibecoda:shared-process->electron-main=ipc-ready',
	initDone: 'vibecoda:shared-process->electron-main=init-done'
};

export const SharedProcessChannelConnection = {
	request: 'vibecoda:createSharedProcessChannelConnection',
	response: 'vibecoda:createSharedProcessChannelConnectionResult'
};

export const SharedProcessRawConnection = {
	request: 'vibecoda:createSharedProcessRawConnection',
	response: 'vibecoda:createSharedProcessRawConnectionResult'
};
