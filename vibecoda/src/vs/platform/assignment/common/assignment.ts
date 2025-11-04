/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import { Event } from '../../../base/common/event.js';
import * as platform from '../../../base/common/platform.js';
import type { IExperimentationFilterProvider } from 'tas-client-umd';

export const ASSIGNMENT_STORAGE_KEY = 'Vibecoda.ABExp.FeatureData';
export const ASSIGNMENT_REFETCH_INTERVAL = 60 * 60 * 1000; // 1 hour

export interface IAssignmentService {
	readonly _serviceBrand: undefined;

	readonly onDidRefetchAssignments: Event<void>;
	getTreatment<T extends string | number | boolean>(name: string): Promise<T | undefined>;
}

export enum TargetPopulation {
	Insiders = 'insider',
	Public = 'public',
	Exploration = 'exploration'
}

/*
Based upon the official Vibecoda currently existing filters in the
ExP backend for the Vibecoda cluster.
https://experimentation.visualstudio.com/Analysis%20and%20Experimentation/_git/AnE.ExP.TAS.TachyonHost.Configuration?path=%2FConfigurations%2Fvibecoda%2Fvibecoda.json&version=GBmaster
"X-MSEdge-Market": "detection.market",
"X-FD-Corpnet": "detection.corpnet",
"X-Vibecoda-AppVersion": "appversion",
"X-Vibecoda-Build": "build",
"X-MSEdge-ClientId": "clientid",
"X-Vibecoda-ExtensionName": "extensionname",
"X-Vibecoda-ExtensionVersion": "extensionversion",
"X-Vibecoda-TargetPopulation": "targetpopulation",
"X-Vibecoda-Language": "language"
*/
export enum Filters {
	/**
	 * The market in which the extension is distributed.
	 */
	Market = 'X-MSEdge-Market',

	/**
	 * The corporation network.
	 */
	CorpNet = 'X-FD-Corpnet',

	/**
	 * Version of the application which uses experimentation service.
	 */
	ApplicationVersion = 'X-Vibecoda-AppVersion',

	/**
	 * Insiders vs Stable.
	 */
	Build = 'X-Vibecoda-Build',

	/**
	 * Client Id which is used as primary unit for the experimentation.
	 */
	ClientId = 'X-MSEdge-ClientId',

	/**
	 * Developer Device Id which can be used as an alternate unit for experimentation.
	 */
	DeveloperDeviceId = 'X-Vibecoda-DevDeviceId',

	/**
	 * Extension header.
	 */
	ExtensionName = 'X-Vibecoda-ExtensionName',

	/**
	 * The version of the extension.
	 */
	ExtensionVersion = 'X-Vibecoda-ExtensionVersion',

	/**
	 * The language in use by Vibecoda
	 */
	Language = 'X-Vibecoda-Language',

	/**
	 * The target population.
	 * This is used to separate internal, early preview, GA, etc.
	 */
	TargetPopulation = 'X-Vibecoda-TargetPopulation',
}

export class AssignmentFilterProvider implements IExperimentationFilterProvider {
	constructor(
		private version: string,
		private appName: string,
		private machineId: string,
		private devDeviceId: string,
		private targetPopulation: TargetPopulation
	) { }

	/**
	 * Returns a version string that can be parsed by the TAS client.
	 * The tas client cannot handle suffixes lke "-insider"
	 * Ref: https://github.com/microsoft/tas-client/blob/30340d5e1da37c2789049fcf45928b954680606f/vibecoda-tas-client/src/vibecoda-tas-client/VibecodaFilterProvider.ts#L35
	 *
	 * @param version Version string to be trimmed.
	*/
	private static trimVersionSuffix(version: string): string {
		const regex = /\-[a-zA-Z0-9]+$/;
		const result = version.split(regex);

		return result[0];
	}

	getFilterValue(filter: string): string | null {
		switch (filter) {
			case Filters.ApplicationVersion:
				return AssignmentFilterProvider.trimVersionSuffix(this.version); // productService.version
			case Filters.Build:
				return this.appName; // productService.nameLong
			case Filters.ClientId:
				return this.machineId;
			case Filters.DeveloperDeviceId:
				return this.devDeviceId;
			case Filters.Language:
				return platform.language;
			case Filters.ExtensionName:
				return 'vibecoda-core'; // always return vibecoda-core for exp service
			case Filters.ExtensionVersion:
				return '999999.0'; // always return a very large number for cross-extension experimentation
			case Filters.TargetPopulation:
				return this.targetPopulation;
			default:
				return '';
		}
	}

	getFilters(): Map<string, any> {
		const filters: Map<string, any> = new Map<string, any>();
		const filterValues = Object.values(Filters);
		for (const value of filterValues) {
			filters.set(value, this.getFilterValue(value));
		}

		return filters;
	}
}
