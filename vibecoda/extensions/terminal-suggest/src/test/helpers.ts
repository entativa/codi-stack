/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import 'mocha';
import * as vibecoda from 'vibecoda';
import type { ICompletionResource } from '../types';
import type { Uri } from 'vibecoda';

export interface ISuiteSpec {
	name: string;
	completionSpecs: Fig.Spec | Fig.Spec[];
	// TODO: This seems unnecessary, ideally getCompletionItemsFromSpecs would only consider the
	//       spec's completions
	availableCommands: string | string[];
	testSpecs: ITestSpec[];
}

export interface ITestSpec {
	input: string;
	expectedResourceRequests?: {
		type: 'files' | 'folders' | 'both';
		cwd: Uri;
	};
	expectedCompletions?: (string | ICompletionResource)[];
}

const fixtureDir = vibecoda.Uri.joinPath(vibecoda.Uri.file(__dirname), '../../testWorkspace');

/**
 * A default set of paths shared across tests.
 */
export const testPaths = {
	fixtureDir,
	cwdParent: vibecoda.Uri.joinPath(fixtureDir, 'parent'),
	cwd: vibecoda.Uri.joinPath(fixtureDir, 'parent/home'),
	cwdChild: vibecoda.Uri.joinPath(fixtureDir, 'parent/home/child'),
};

export function removeArrayEntries<T>(array: T[], ...elements: T[]): T[] {
	for (const element of elements) {
		const index = array.indexOf(element);
		if (index > -1) {
			array.splice(index, 1);
		}
	}
	return array;
}
