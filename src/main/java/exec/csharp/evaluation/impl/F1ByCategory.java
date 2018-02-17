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

import cc.kave.commons.evaluation.BoxplotData;
import cc.kave.commons.utils.io.Logger;
import cc.kave.rsse.calls.usages.Usage;
import exec.csharp.evaluation.AbstractEvaluationConsumer;
import exec.csharp.evaluation.IEvaluation;
import exec.csharp.queries.QueryMode;
import exec.csharp.utils.QueryJudge;

public class F1ByCategory extends AbstractEvaluationConsumer {

	private final IEvaluation eval;

	private Map<QueryMode, Map<QueryContent, BoxplotData>> res;
	private Map<QueryContent, Integer> counts;

	@Inject
	public F1ByCategory(IEvaluation eval) {
		this.eval = eval;

	}

	@Override
	public void run() {
		res = Maps.newLinkedHashMap();
		for (QueryMode qm : QueryMode.values()) {
			Map<QueryContent, BoxplotData> qms = Maps.newLinkedHashMap();
			res.put(qm, qms);

			for (QueryContent qc : QueryContent.values()) {
				qms.put(qc, new BoxplotData());
			}
		}

		counts = Maps.newLinkedHashMap();
		for (QueryContent qc : QueryContent.values()) {
			counts.put(qc, 0);
		}

		eval.run(this);
	}

	@Override
	public void addResult(Usage start, Usage end, QueryMode queryMode, double f1) {
		Logger.append(".");
		QueryContent qc = categorize(start, end);

		res.get(queryMode).get(qc).add(f1);
		if (queryMode == QueryMode.LINEAR) {
			counts.put(qc, counts.get(qc) + 1);
		}
	}

	private QueryContent categorize(Usage start, Usage end) {
		return new QueryJudge(start, end).getQueryContentCategorization();
	}

	@Override
	public void finish() {
		System.out.println();
		System.out.println();
		for (QueryContent qc : QueryContent.values()) {
			System.out.print(" & " + qc);
		}
		System.out.println();

		for (QueryMode qm : QueryMode.values()) {
			System.out.print(qm);
			for (QueryContent qc : QueryContent.values()) {
				double meanF1 = res.get(qm).get(qc).getBoxplot().getMean();
				System.out.printf(" & %.4f", meanF1);
			}
			System.out.println();
		}

		System.out.print("counts");
		for (QueryContent qc : QueryContent.values()) {
			int count = counts.get(qc);
			System.out.printf(" & %d", count);
		}
		System.out.println();
	}

}