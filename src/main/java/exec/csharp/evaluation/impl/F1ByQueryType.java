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

import java.util.Map;
import java.util.TreeSet;

import com.google.common.collect.Maps;
import com.google.inject.Inject;

import cc.kave.commons.evaluation.Boxplot;
import cc.kave.commons.evaluation.BoxplotData;
import cc.kave.commons.utils.io.Logger;
import cc.kave.rsse.calls.usages.Usage;
import exec.csharp.evaluation.AbstractEvaluationConsumer;
import exec.csharp.evaluation.IEvaluation;
import exec.csharp.queries.QueryMode;
import exec.csharp.utils.QueryUtils;

public class F1ByQueryType extends AbstractEvaluationConsumer {

	private final IEvaluation eval;

	private Map<String, Map<QueryMode, Map<Integer, BoxplotData>>> results;

	@Inject
	public F1ByQueryType(IEvaluation eval) {
		this.eval = eval;
	}

	@Override
	public void run() {
		results = Maps.newHashMap();
		eval.run(this);
	}

	@Override
	public void addResult(Usage start, Usage end, QueryMode queryMode, double f1) {
		Logger.append(".");
		int numEnd = end.getReceiverCallsites().size();
		int numAdd = QueryUtils.countAdditions(start, end);
		int numStart = numEnd - numAdd;
		store("avg", queryMode, -1, f1);
		store("before", queryMode, numStart, f1);
		store("add", queryMode, numAdd, f1);
		store("after", queryMode, numEnd, f1);
	}

	private void store(String kind, QueryMode queryMode, int num, double f1) {
		Map<QueryMode, Map<Integer, BoxplotData>> modes = getOrCreate(results, kind, Maps.newLinkedHashMap());
		Map<Integer, BoxplotData> nums = getOrCreate(modes, queryMode, Maps.newLinkedHashMap());
		BoxplotData data = getOrCreate(nums, num, new BoxplotData());
		data.add(f1);
	}

	private <K, V> V getOrCreate(Map<K, V> map, K key, V defaultValue) {
		if (map.containsKey(key)) {
			return map.get(key);
		} else {
			map.put(key, defaultValue);
			return defaultValue;
		}
	}

	@Override
	public void finish() {
		Logger.append("\n\nresults:");
		for (String desc : results.keySet()) {
			Logger.append("\n\n## %s ######################\n", desc);
			Map<QueryMode, Map<Integer, BoxplotData>> modes = results.get(desc);

			for (QueryMode mode : modes.keySet()) {
				Logger.append("\n    %s", mode);

				Map<Integer, BoxplotData> nums = modes.get(mode);
				TreeSet<Integer> sortedNums = new TreeSet<Integer>(nums.keySet());

				int total = 0;
				for (int num : sortedNums) {
					BoxplotData data = nums.get(num);
					Boxplot boxplot = data.getBoxplot();
					total += boxplot.getNumValues();
					Logger.append("\n        %3d: %s", num, boxplot);
				}
				Logger.append("\n        ---");
				Logger.append("\n        %d values", total);
			}
		}
	}
}