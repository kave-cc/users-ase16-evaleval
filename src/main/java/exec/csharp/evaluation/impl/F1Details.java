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

import com.google.common.collect.Maps;
import com.google.inject.Inject;

import cc.kave.commons.assertions.Asserts;
import cc.kave.commons.evaluation.BoxplotData;
import cc.kave.rsse.calls.usages.Usage;
import exec.csharp.evaluation.AbstractEvaluationConsumer;
import exec.csharp.evaluation.IEvaluation;
import exec.csharp.queries.QueryMode;
import exec.csharp.utils.QueryUtils;

public class F1Details extends AbstractEvaluationConsumer {

	private final IEvaluation eval;

	private Map<QueryMode, Map<String, BoxplotData>> res;
	private Map<String, Integer> counts;

	@Inject
	public F1Details(IEvaluation eval) {
		this.eval = eval;
	}

	@Override
	public void run() {
		res = Maps.newLinkedHashMap();
		counts = Maps.newLinkedHashMap();
		eval.run(this);
	}

	@Override
	public void addResult(Usage start, Usage end, QueryMode queryMode, double f1) {
		System.out.printf(".");
		String key = getKey(start, end);
		storeResult(queryMode, key, f1);
		if (QueryMode.LINEAR == queryMode) {
			count(key);
		}
	}

	private String getKey(Usage start, Usage end) {
		int numStart = start.getReceiverCallsites().size();
		int numAdded = QueryUtils.countAdditions(start, end);
		int numRemoved = QueryUtils.countRemovals(start, end);
		int numStartWithoutNoise = numStart - numRemoved;
		Asserts.assertGreaterOrEqual(numStartWithoutNoise, 0);
		int numEnd = end.getReceiverCallsites().size();
		Asserts.assertEquals(numStartWithoutNoise + numAdded, numEnd);

		return getStringKey(numStartWithoutNoise, numEnd);
	}

	private String getStringKey(int numStartWithoutNoise, int numEnd) {
		String first = numStartWithoutNoise < 4 ? Integer.toString(numStartWithoutNoise) : "4+";
		String second = numEnd < 5 ? Integer.toString(numEnd) : "5+";
		return String.format("%s|%s", first, second);
	}

	private void storeResult(QueryMode queryMode, String key, double f1) {
		Map<String, BoxplotData> keys = res.get(queryMode);
		if (keys == null) {
			keys = Maps.newLinkedHashMap();
			res.put(queryMode, keys);
		}

		BoxplotData f1s = keys.get(key);
		if (f1s == null) {
			f1s = new BoxplotData();
			keys.put(key, f1s);
		}

		f1s.add(f1);
	}

	private void count(String key) {
		Integer count = counts.get(key);
		if (count == null) {
			counts.put(key, 1);
		} else {
			counts.put(key, count + 1);
		}
	}

	@Override
	public void finish() {

		System.out.printf("\n\n## counts ############\n\n");
		for (int second = 1; second <= 5; second++) {
			System.out.printf("\t%s", nMax(second, 5));
		}
		System.out.println();

		for (int first = 0; first <= 4; first++) {
			System.out.printf("%s", nMax(first, 4));

			for (int second = 1; second <= 5; second++) {
				String key = getStringKey(first, second);

				if (counts.containsKey(key)) {
					int count = counts.get(key);
					System.out.printf("\t%d", count);
				} else {
					System.out.print("\t-");
				}
			}
			System.out.println();
		}
		System.out.println();

		for (QueryMode qm : QueryMode.values()) {
			System.out.printf("## %s ############\n\n", qm);
			Map<String, BoxplotData> keys = res.get(qm);
			if (keys == null) {
				System.out.printf("no data\n\n");
				continue;
			}

			for (int second = 1; second <= 5; second++) {
				System.out.printf("\t%s", nMax(second, 5));
			}
			System.out.println();

			for (int first = 0; first <= 4; first++) {
				System.out.printf("%s", nMax(first, 4));
				for (int second = 1; second <= 5; second++) {
					String key = getStringKey(first, second);
					if (keys.containsKey(key)) {
						BoxplotData bp = keys.get(key);
						double f1 = bp.getBoxplot().getMean();
						System.out.printf("\t%.4f", f1);
					} else {
						System.out.printf("\t-");
					}
				}
				System.out.println();
			}
			System.out.println();
		}
	}

	private static String nMax(int num, int max) {
		if (num < max) {
			return Integer.toString(num);
		}
		return Integer.toString(max) + "+";
	}
}