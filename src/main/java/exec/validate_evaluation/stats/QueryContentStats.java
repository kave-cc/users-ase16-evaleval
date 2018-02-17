/**
 * Copyright 2016 Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package exec.validate_evaluation.stats;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;

import cc.kave.commons.model.naming.types.ITypeName;
import cc.kave.commons.utils.io.NestedZipFolders;
import cc.kave.rsse.calls.datastructures.Tuple;
import exec.csharp.utils.NoiseMode;
import exec.csharp.utils.QueryJudge;
import exec.validate_evaluation.categorized.MicroCommitIoExtension;
import exec.validate_evaluation.microcommits.MicroCommit;

public class QueryContentStats {

	private static final int MAX_M = 6;
	private static final int MAX_N = 5;

	private NestedZipFolders<ITypeName> usages;
	private MicroCommitIoExtension mcIo;

	int numSkipped = 0;
	int numPureRemoval = 0;

	public Map<Object, Integer> counts = Maps.newHashMap();

	public QueryContentStats(NestedZipFolders<ITypeName> usages, MicroCommitIoExtension mcIo) {
		this.usages = usages;
		this.mcIo = mcIo;
	}

	public void run() {

		for (int n = 0; n <= MAX_N; n++) {
			for (int m = 1; m <= MAX_M; m++) {
				counts.put(key(n, m), 0);
			}
		}

		System.out.println();
		System.out.println("finding keys");
		Set<ITypeName> keys = usages.findKeys();
		for (ITypeName type : keys) {
			for (String user : mcIo.findZipsWith(type)) {
				System.out.println(type);
				for (List<MicroCommit> mcs : mcIo.readZipAndSortByLocation(user, type)) {
					for (MicroCommit mc : mcs) {
						System.out.printf(".");
						count(mc);
					}
				}
				System.out.println();
			}
		}

		System.out.println();
		System.out.println();
		System.out.println("Statistics of query contents:");

		for (int m = 1; m <= MAX_M; m++) {
			System.out.printf("\t%d", m);
		}
		System.out.println();

		int numTotal = 0;

		for (int n = 0; n <= MAX_N; n++) {
			System.out.printf("%d", n);
			for (int m = 1; m <= MAX_M; m++) {
				int count = counts.get(key(n, m));
				numTotal += count;
				System.out.printf("\t%d", count);
			}
			System.out.println();
		}

		System.out.println();
		System.out.printf("numRegular: %s\n", numTotal);
		System.out.printf("numPureRemovals: %s\n", numPureRemoval);
		System.out.printf("numSkipped: %s\n", numSkipped);
		System.out.println("---");
		System.out.printf("numTotal: %d", numTotal + numSkipped + numPureRemoval);
	}

	private void count(MicroCommit mc) {

		NoiseMode mode = new QueryJudge(mc).getNoiseMode();
		if (mode == NoiseMode.PURE_REMOVAL) {
			numPureRemoval++;
			return;
		}
		if (mode == NoiseMode.SKIPPED) {
			numSkipped++;
			return;
		}

		int m = mc.getEnd().getReceiverCallsites().size();
		int n = m - new QueryJudge(mc).getNumAdditions();
		m = Math.min(MAX_M, m);
		n = Math.min(MAX_N, n);
		Tuple<Integer, Integer> key = key(n, m);
		int count = counts.get(key);
		counts.put(key, count + 1);

	}

	private Tuple<Integer, Integer> key(int m, int n) {
		return Tuple.newTuple(m, n);
	}
}