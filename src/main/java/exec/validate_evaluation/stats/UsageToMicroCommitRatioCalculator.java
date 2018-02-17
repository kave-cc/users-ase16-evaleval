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
package exec.validate_evaluation.stats;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

import cc.kave.commons.model.naming.types.ITypeName;
import cc.kave.commons.utils.io.NestedZipFolders;
import cc.kave.rsse.calls.usages.Usage;
import exec.csharp.utils.MapSorter;
import exec.csharp.utils.StorageCase;
import exec.csharp.utils.StorageHelper;
import exec.validate_evaluation.microcommits.MicroCommit;
import exec.validate_evaluation.microcommits.MicroCommitIo;

public class UsageToMicroCommitRatioCalculator {

	private final Map<ITypeName, List<MicroCommit>> allMicroCommits;
	private final NestedZipFolders<ITypeName> dirUsages;
	private MicroCommitIo io;

	@Inject
	public UsageToMicroCommitRatioCalculator(StorageHelper storageHelper, MicroCommitIo io) {
		this.io = io;
		allMicroCommits = Maps.newHashMap();
		dirUsages = storageHelper.getNestedZipFolder(StorageCase.USAGES);
	}

	public void run() throws IOException {

		System.out.println("\nreading all available MicroCommits...");
		for (String zip : io.findZips()) {
			for (MicroCommit mc : io.read(zip)) {
				List<MicroCommit> mcs = allMicroCommits.get(mc.getType());
				if (mcs == null) {
					mcs = Lists.newLinkedList();
					allMicroCommits.put(mc.getType(), mcs);
				}
				mcs.add(mc);
			}
		}

		Map<String, Double> usageToHistoryRatio = Maps.newLinkedHashMap();

		int numTypesTotal = 0;
		int numCommitsTotal = 0;
		int numUsagesTotal = 0;

		int numTypesDATEV = 0;
		int numCommitsDATEV = 0;
		int numUsagesDATEV = 0;

		int numTypesWith = 0;
		int numCommitsWith = 0;
		int numUsagesWith = 0;
		int numTypesWithout = 0;
		int numCommitsWithout = 0;
		int numUsagesWithout = 0;

		for (ITypeName t : allMicroCommits.keySet()) {

			List<MicroCommit> commits = allMicroCommits.get(t);
			List<Usage> usages = dirUsages.readAllZips(t, Usage.class);

			int numCommits = commits.size();
			int numUsages = usages.size();
			System.out.printf("%s: %d commits, %d usages\n", t, numCommits, numUsages);

			// if (numUsages > 0 && !isDatev(t)) {
			if (!isDatev(t)) {
				double ratio = (0.000001 + numUsages) / (1.0 * numCommits);
				String key = String.format("%s (%d/%d)", t, numUsages, numCommits);
				usageToHistoryRatio.put(key, ratio);
			}

			numTypesTotal++;
			numCommitsTotal += numCommits;
			numUsagesTotal += numUsages;

			if (numCommits > 0 && numUsages > 0) {
				numTypesWith++;
				numCommitsWith += numCommits;
				numUsagesWith += numUsages;
			} else {
				numTypesWithout++;
				numCommitsWithout += numCommits;
				numUsagesWithout += numUsages;

				if (isDatev(t)) {
					numTypesDATEV++;
					numCommitsDATEV += numCommits;
					numUsagesDATEV += numUsages;
				}
			}
		}

		System.out.printf("\n\nsummary:\n");
		System.out.printf("we have a total of %d commits and %d usages for %d different types\n", numCommitsTotal,
				numUsagesTotal, numTypesTotal);
		System.out.printf("currently, we have both commits and usages for %d types (%d commits, %d usages)\n",
				numTypesWith, numCommitsWith, numUsagesWith);
		System.out.printf("we have commits, but no usages for %d types (%d commits, %d usages)\n", numTypesWithout,
				numCommitsWithout, numUsagesWithout);
		System.out.printf("out of these, %d types (%d commits, %d usages) are related to DATEV\n", numTypesDATEV,
				numCommitsDATEV, numUsagesDATEV);

		System.out.printf("\n\nratios (usages/histories):\n");
		Map<String, Double> sortedRatios = MapSorter.sort(usageToHistoryRatio);
		for (String key : sortedRatios.keySet()) {
			double ratio = sortedRatios.get(key);
			System.out.printf("%3.2f - %s\n", ratio, key);
		}

	}

	private static boolean isDatev(ITypeName t) {
		return StringUtils.containsIgnoreCase(t.toString(), "datev");
	}
}