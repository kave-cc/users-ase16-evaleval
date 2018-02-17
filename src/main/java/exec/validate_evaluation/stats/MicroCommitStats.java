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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import cc.kave.commons.assertions.Asserts;
import cc.kave.commons.model.naming.codeelements.IMethodName;
import cc.kave.commons.model.naming.types.ITypeName;
import cc.kave.commons.utils.io.NestedZipFolders;
import cc.kave.rsse.calls.datastructures.Tuple;
import cc.kave.rsse.calls.usages.NoUsage;
import cc.kave.rsse.calls.usages.Usage;
import exec.csharp.evaluation.impl.QueryContent;
import exec.csharp.utils.NoiseMode;
import exec.csharp.utils.QueryJudge;
import exec.csharp.utils.QueryUtils;
import exec.validate_evaluation.microcommits.MicroCommit;
import exec.validate_evaluation.microcommits.MicroCommitIo;

public class MicroCommitStats {

	int numCommitsWithNoUsages;
	private MicroCommitIo io;
	private Map<ITypeName, Map<IMethodName, List<MicroCommit>>> allCommits = Maps.newHashMap();
	private NestedZipFolders<ITypeName> usages;

	public MicroCommitStats(MicroCommitIo io, NestedZipFolders<ITypeName> usages) {
		this.io = io;
		this.usages = usages;
	}

	public void run() {
		for (String zip : io.findZips()) {

			System.out.println();
			System.out.printf("#####\n");
			System.out.printf("##### %s\n", zip);
			System.out.printf("#####\n");
			System.out.println();

			List<MicroCommit> mcs = io.read(zip);
			System.out.printf("%d commits\n", mcs.size());

			for (MicroCommit mc : mcs) {
				sort(mc);
			}
		}

		print();

		System.out.println();
		System.out.println();

		System.out.printf("--- overall stats ---\n");
		System.out.printf("numTypes: %d\n", usedTypes.size());
		System.out.printf("numLocations: %d\n", usedLocations.size());
		System.out.printf("numCommits: %d\n", numCommits);
		System.out.printf("numCommitsWithNoUsages: %d\n", numCommitsWithNoUsages);

		printCounting("diffStats", diffCounts);
		printCounting("noiseStats", noiseCounts);
		printCounting("contentStats", contentCounts);
	}

	private void printCounting(String title, Map<?, Integer> counts) {
		System.out.println();
		System.out.println();
		System.out.printf("#### %s ####\n", title);
		int numDiffs = 0;
		for (Object key : counts.keySet()) {
			int diffCount = counts.get(key);
			numDiffs += diffCount;
			System.out.printf("%s\t%d\n", key, diffCount);
		}
		System.out.printf("---\ntotal\t%d\n", numDiffs);
	}

	private void sort(MicroCommit mc) {

		validate(mc);

		ITypeName type = mc.getType();
		IMethodName context = mc.getMethodContext();

		if (!usages.hasZips(type)) {
			numCommitsWithNoUsages++;
			return;
		}

		Map<IMethodName, List<MicroCommit>> ctxs = allCommits.get(type);
		if (ctxs == null) {
			ctxs = Maps.newHashMap();
			allCommits.put(type, ctxs);
		}

		List<MicroCommit> mcs = ctxs.get(context);
		if (mcs == null) {
			mcs = Lists.newLinkedList();
			ctxs.put(context, mcs);
		}

		Asserts.assertTrue(mcs.add(mc));
	}

	private void validate(MicroCommit mc) {
		Usage start = mc.getStart();
		Usage end = mc.getEnd();
		if (!(start instanceof NoUsage || end instanceof NoUsage)) {
			Asserts.assertEquals(start.getType(), end.getType(), "expected equal type");
			Asserts.assertEquals(start.getMethodContext(), end.getMethodContext(), "expected equal ctx");
		}
	}

	private Set<ITypeName> usedTypes = Sets.newHashSet();
	private Set<Tuple<ITypeName, IMethodName>> usedLocations = Sets.newHashSet();
	private int numCommits = 0;
	private Map<String, Integer> diffCounts = Maps.newHashMap();
	private Map<NoiseMode, Integer> noiseCounts = Maps.newHashMap();
	private Map<QueryContent, Integer> contentCounts = Maps.newHashMap();

	private void print() {

		for (ITypeName t : allCommits.keySet()) {
			Map<IMethodName, List<MicroCommit>> ctxs = allCommits.get(t);

			usedTypes.add(t);
			System.out.println();
			System.out.printf("#### commits of '%s' ####\n", t);

			for (IMethodName m : ctxs.keySet()) {
				List<MicroCommit> mcs = ctxs.get(m);

				Tuple<ITypeName, IMethodName> loc = Tuple.newTuple(t, m);
				usedLocations.add(loc);

				System.out.println();
				System.out.printf("in: %s\n", m);

				numCommits += mcs.size();

				System.out.println();
				System.out.println();
				System.out.println("### Diff String ###");
				for (MicroCommit mc : mcs) {
					String diff = QueryUtils.toDiffString(mc);
					System.out.printf("%s, ", diff);

					Integer i = diffCounts.get(diff);
					if (i == null) {
						diffCounts.put(diff, 1);
					} else {
						diffCounts.put(diff, i + 1);
					}
				}

				System.out.println();
				System.out.println();
				System.out.println("### Noise Mode / Query Content ###");
				for (MicroCommit mc : mcs) {
					QueryJudge judge = new QueryJudge(mc);

					NoiseMode noiseMode = judge.getNoiseMode();
					QueryContent queryContentCategorization = judge.getQueryContentCategorization();

					System.out.printf("%s/%s, ", noiseMode, queryContentCategorization);

					Integer i = noiseCounts.get(noiseMode);
					if (i == null) {
						noiseCounts.put(noiseMode, 1);
					} else {
						noiseCounts.put(noiseMode, i + 1);
					}

					Integer i2 = contentCounts.get(queryContentCategorization);
					if (i2 == null) {
						contentCounts.put(queryContentCategorization, 1);
					} else {
						contentCounts.put(queryContentCategorization, i2 + 1);
					}
				}

				System.out.println();
			}
		}
	}
}