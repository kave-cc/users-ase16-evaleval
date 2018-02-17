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
import cc.kave.rsse.calls.usages.Usage;
import exec.csharp.evaluation.AbstractEvaluationConsumer;
import exec.csharp.evaluation.IEvaluation;
import exec.csharp.queries.QueryMode;
import exec.csharp.utils.NoiseMode;
import exec.csharp.utils.QueryJudge;

public class AnalysisOfNoise extends AbstractEvaluationConsumer {

	private final IEvaluation eval;

	private Map<QueryMode, Map<NoiseMode, BoxplotData>> results;

	@Inject
	public AnalysisOfNoise(IEvaluation eval) {
		this.eval = eval;

	}

	@Override
	public void run() {
		results = Maps.newLinkedHashMap();
		for (QueryMode qm : QueryMode.values()) {
			results.put(qm, Maps.newLinkedHashMap());
			for (NoiseMode nm : NoiseMode.values()) {
				results.get(qm).put(nm, new BoxplotData());
			}
		}
		eval.run(this);
	}

	@Override
	public void addResult(Usage start, Usage end, QueryMode queryMode, double f1) {
		NoiseMode noiseMode = new QueryJudge(start, end).getNoiseMode();
		results.get(queryMode).get(noiseMode).add(f1);
	}

	@Override
	public void skipCommit_NoAddition(QueryMode mode) {
		results.get(mode).get(NoiseMode.PURE_REMOVAL).add(0.0);
	}

	@Override
	public void finish() {

		System.out.println();
		System.out.println();

		// header
		for (NoiseMode nm : NoiseMode.values()) {
			System.out.print("\t" + nm);
		}
		System.out.println();

		// values
		for (QueryMode qm : QueryMode.values()) {
			System.out.print(qm);
			for (NoiseMode nm : NoiseMode.values()) {
				double f1 = results.get(qm).get(nm).getMean();
				if (Double.isNaN(f1)) {
					System.out.printf("\t-");
				} else {
					System.out.printf("\t%.4f", f1);
				}
			}
			System.out.println();
		}

		// counts
		System.out.printf("count");
		for (NoiseMode nm : NoiseMode.values()) {
			BoxplotData data = results.get(QueryMode.LINEAR).get(nm);
			int numValues = data.getRawValues().length;
			System.out.print("\t" + numValues);
		}
		System.out.println();

	}
}