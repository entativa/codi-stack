/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import { IstanbulCoverageContext } from 'istanbul-to-vibecoda';
import * as vibecoda from 'vibecoda';
import { SearchStrategy, SourceLocationMapper, SourceMapStore } from './testOutputScanner';
import { IScriptCoverage, OffsetToPosition, RangeCoverageTracker } from './v8CoverageWrangling';

export const istanbulCoverageContext = new IstanbulCoverageContext();

/**
 * Tracks coverage in per-script coverage mode. There are two modes of coverage
 * in this extension: generic istanbul reports, and reports from the runtime
 * sent before and after each test case executes. This handles the latter.
 */
export class PerTestCoverageTracker {
	private readonly scripts = new Map</* script ID */ string, Script>();

	constructor(private readonly maps: SourceMapStore) { }

	public add(coverage: IScriptCoverage, test?: vibecoda.TestItem) {
		const script = this.scripts.get(coverage.scriptId);
		if (script) {
			return script.add(coverage, test);
		}
		// ignore internals and node_modules
		if (!coverage.url.startsWith('file://') || coverage.url.includes('node_modules')) {
			return;
		}
		if (!coverage.source) {
			throw new Error('expected to have source the first time a script is seen');
		}

		const src = new Script(vibecoda.Uri.parse(coverage.url), coverage.source, this.maps);
		this.scripts.set(coverage.scriptId, src);
		src.add(coverage, test);
	}

	public async report(run: vibecoda.TestRun) {
		await Promise.all(Array.from(this.scripts.values()).map(s => s.report(run)));
	}
}

class Script {
	private converter: OffsetToPosition;

	/** Tracking the overall coverage for the file */
	private overall = new ScriptCoverageTracker();
	/** Range tracking per-test item */
	private readonly perItem = new Map<vibecoda.TestItem, ScriptCoverageTracker>();

	constructor(
		public readonly uri: vibecoda.Uri,
		source: string,
		private readonly maps: SourceMapStore
	) {
		this.converter = new OffsetToPosition(source);
	}

	public add(coverage: IScriptCoverage, test?: vibecoda.TestItem) {
		this.overall.add(coverage);
		if (test) {
			const p = new ScriptCoverageTracker();
			p.add(coverage);
			this.perItem.set(test, p);
		}
	}

	public async report(run: vibecoda.TestRun) {
		const mapper = await this.maps.getSourceLocationMapper(this.uri.toString());
		const originalUri = (await this.maps.getSourceFile(this.uri.toString())) || this.uri;
		run.addCoverage(this.overall.report(originalUri, this.converter, mapper, this.perItem));
	}
}

class ScriptCoverageTracker {
	private coverage = new RangeCoverageTracker();

	public add(coverage: IScriptCoverage) {
		for (const range of RangeCoverageTracker.initializeBlocks(coverage.functions)) {
			this.coverage.setCovered(range.start, range.end, range.covered);
		}
	}

	public *toDetails(
		uri: vibecoda.Uri,
		convert: OffsetToPosition,
		mapper: SourceLocationMapper | undefined,
	) {
		for (const range of this.coverage) {
			if (range.start === range.end) {
				continue;
			}

			const startCov = convert.toLineColumn(range.start);
			let start = new vibecoda.Position(startCov.line, startCov.column);

			const endCov = convert.toLineColumn(range.end);
			let end = new vibecoda.Position(endCov.line, endCov.column);
			if (mapper) {
				const startMap = mapper(start.line, start.character, SearchStrategy.FirstAfter);
				const endMap = startMap && mapper(end.line, end.character, SearchStrategy.FirstBefore);
				if (!endMap || uri.toString().toLowerCase() !== endMap.uri.toString().toLowerCase()) {
					continue;
				}
				start = startMap.range.start;
				end = endMap.range.end;
			}

			for (let i = start.line; i <= end.line; i++) {
				yield new vibecoda.StatementCoverage(
					range.covered,
					new vibecoda.Range(
						new vibecoda.Position(i, i === start.line ? start.character : 0),
						new vibecoda.Position(i, i === end.line ? end.character : Number.MAX_SAFE_INTEGER)
					)
				);
			}
		}
	}

	/**
	 * Generates the script's coverage for the test run.
	 *
	 * If a source location mapper is given, it assumes the `uri` is the mapped
	 * URI, and that any unmapped locations/outside the URI should be ignored.
	 */
	public report(
		uri: vibecoda.Uri,
		convert: OffsetToPosition,
		mapper: SourceLocationMapper | undefined,
		items: Map<vibecoda.TestItem, ScriptCoverageTracker>,
	): V8CoverageFile {
		const file = new V8CoverageFile(uri, items, convert, mapper);
		for (const detail of this.toDetails(uri, convert, mapper)) {
			file.add(detail);
		}

		return file;
	}
}

export class V8CoverageFile extends vibecoda.FileCoverage {
	public details: vibecoda.StatementCoverage[] = [];

	constructor(
		uri: vibecoda.Uri,
		private readonly perTest: Map<vibecoda.TestItem, ScriptCoverageTracker>,
		private readonly convert: OffsetToPosition,
		private readonly mapper: SourceLocationMapper | undefined,
	) {
		super(uri, { covered: 0, total: 0 }, undefined, undefined, [...perTest.keys()]);
	}

	public add(detail: vibecoda.StatementCoverage) {
		this.details.push(detail);
		this.statementCoverage.total++;
		if (detail.executed) {
			this.statementCoverage.covered++;
		}
	}

	public testDetails(test: vibecoda.TestItem): vibecoda.FileCoverageDetail[] {
		const t = this.perTest.get(test);
		return t ? [...t.toDetails(this.uri, this.convert, this.mapper)] : [];
	}
}
