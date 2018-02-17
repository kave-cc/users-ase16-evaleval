/*
 * Copyright 2014 Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package exec.csharp.evaluation.impl;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.google.inject.Inject;

import cc.kave.commons.evaluation.BoxplotData;
import cc.kave.commons.model.naming.types.ITypeName;
import cc.kave.commons.utils.io.Logger;
import cc.kave.rsse.calls.usages.DefinitionSiteKind;
import cc.kave.rsse.calls.usages.Usage;
import exec.csharp.evaluation.AbstractEvaluationConsumer;
import exec.csharp.evaluation.IEvaluation;
import exec.csharp.queries.QueryMode;
import exec.csharp.utils.MapSorter;
import exec.csharp.utils.QueryUtils;
import exec.validate_evaluation.microcommits.MicroCommit;

public class F1ByQueryMode extends AbstractEvaluationConsumer {

	private final IEvaluation eval;

	private Map<QueryMode, BoxplotData> results;
	private int queriesTotal;
	private int numFilteredNoUsages;
	private int numFilteredNoChange;
	private int numFilteredNoAddition;
	private int numFiltered01;
	private Map<String, Integer> queryTypeCounts;
	private Map<ITypeName, Integer> typeCounts;
	private Map<DefinitionSiteKind, Integer> defCounts;
	private Map<Integer, Integer> beforeCounts;
	private Map<Integer, Integer> addCounts;
	private Map<Integer, Integer> afterCounts;
	private int numResultsInCurrentLine;

	@Inject
	public F1ByQueryMode(IEvaluation eval) {
		this.eval = eval;
	}

	@Override
	public void run() {
		results = Maps.newHashMap();
		for (QueryMode mode : QueryMode.values()) {
			results.put(mode, new BoxplotData());
		}

		queriesTotal = 0;
		numFilteredNoUsages = 0;
		numFilteredNoChange = 0;
		numFilteredNoAddition = 0;
		numFiltered01 = 0;

		queryTypeCounts = Maps.newHashMap();
		typeCounts = Maps.newHashMap();
		defCounts = Maps.newHashMap();
		beforeCounts = Maps.newHashMap();
		addCounts = Maps.newHashMap();
		afterCounts = Maps.newHashMap();

		eval.run(this);
	}

	@Override
	public void skippingType(ITypeName type, List<Usage> us, List<MicroCommit> histories) {
		super.skippingType(type, us, histories);
		queriesTotal += histories.size();
		numFilteredNoUsages += histories.size();
	}

	@Override
	public void startingType(ITypeName type, List<Usage> usages, List<MicroCommit> histories) {
		super.startingType(type, usages, histories);
		queriesTotal += histories.size();
	}

	@Override
	public void startingQueryMode(QueryMode mode) {
		super.startingQueryMode(mode);
		numResultsInCurrentLine = 100;
	}

	@Override
	public void skipCommit_NoChange(QueryMode mode) {
		numFilteredNoChange++;
	}

	@Override
	public void skipCommit_NoAddition(QueryMode mode) {
		numFilteredNoAddition++;
	}

	@Override
	public void registerQuery(DefinitionSiteKind def, int before, int add, int after) {
		count(defCounts, def);
		count(beforeCounts, before);
		count(addCounts, add);
		count(afterCounts, after);
	}

	@Override
	public void addResult(Usage start, Usage end, QueryMode queryMode, double f1) {
		results.get(queryMode).add(f1);

		String diffString = QueryUtils.toDiffString(start, end);
		logNewLineIfNecessary();
		Logger.append("%.3f (%s), ", f1, diffString);

		if (queryMode.equals(QueryMode.LINEAR)) {
			count(queryTypeCounts, diffString);
			count(typeCounts, start.getType());
		}
	}

	private void logNewLineIfNecessary() {
		if (++numResultsInCurrentLine > 12) {
			numResultsInCurrentLine = 0;
			Logger.log("\t\t");
		}
	}

	@Override
	public void finish() {

		int numRemaining = queriesTotal - numFilteredNoUsages - numFilteredNoChange - numFilteredNoAddition
				- numFiltered01;
		Logger.append("\n\n%d queries in total", queriesTotal);
		Logger.append("\nfiltered %d queries because no usages existed", numFilteredNoUsages);
		Logger.append("\nfiltered %d queries because no change", numFilteredNoChange);
		Logger.append("\nfiltered %d queries because nothing was added", numFilteredNoAddition);
		Logger.append("\nfiltered %d queries because there is no diff for 0|N", numFiltered01);
		Logger.append("\n--> %d queries remaining", numRemaining);

		Logger.append("\n\nresults:\n");
		for (QueryMode mode : QueryMode.values()) {
			Logger.append("%6s: %s\n", mode, results.get(mode).getBoxplot());
		}

		printMap(defCounts, "queries for definitions");
		printMap(beforeCounts, "queries for numBefore");
		printMap(addCounts, "queries for numAdditions");
		printMap(afterCounts, "queries for numAfter");

		Map<String, Integer> sortedQueryTypeCounts = MapSorter.sort(queryTypeCounts);
		printMap(sortedQueryTypeCounts, "kind of queries");

		Map<ITypeName, Integer> sortedTypeCounts = MapSorter.sort(typeCounts);
		printMap(sortedTypeCounts, "usages per type");
	}

	private <T> void count(Map<T, Integer> counts, T key) {
		if (counts.containsKey(key)) {
			int c = counts.get(key);
			counts.put(key, c + 1);
		} else {
			counts.put(key, 1);
		}
	}

	private <T> void printMap(Map<T, Integer> counts, String title) {
		int total = 0;
		Logger.append("\n\n%s:\n", title);
		for (T kind : counts.keySet()) {
			int count = counts.get(kind);
			total += count;
			Logger.append("%3dx %s\n", count, kind);
		}
		Logger.append("---\n%3d total\n", total);
	}
}
