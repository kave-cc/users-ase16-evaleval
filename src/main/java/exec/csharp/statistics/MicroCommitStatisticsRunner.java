/**
 * Copyright (c) 2011-2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Sebastian Proksch - initial API and implementation
 */
package exec.csharp.statistics;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

import cc.kave.commons.model.naming.types.ITypeName;
import cc.kave.commons.utils.io.Directory;
import cc.kave.commons.utils.io.IReadingArchive;
import cc.kave.commons.utils.io.NestedZipFolders;
import cc.kave.rsse.calls.usages.CallSite;
import cc.kave.rsse.calls.usages.Query;
import cc.kave.rsse.calls.usages.Usage;
import exec.csharp.utils.MapSorter;
import exec.csharp.utils.StorageCase;
import exec.csharp.utils.StorageHelper;
import exec.validate_evaluation.microcommits.MicroCommit;

public class MicroCommitStatisticsRunner {

	private final Directory dirHistories;

	private int numTotal = 0;
	private int numAdditions = 0;
	private int numRemovals = 0;
	private int numBoth = 0;
	private int numNeither = 0;
	private Map<ITypeName, Integer> counts = Maps.newLinkedHashMap();
	private Map<ITypeName, Integer> allNumCommits = Maps.newLinkedHashMap();

	private NestedZipFolders<ITypeName> zipsUsages;

	@Inject
	public MicroCommitStatisticsRunner(StorageHelper storageHelper) {
		dirHistories = storageHelper.getDirectory(StorageCase.MICRO_COMMITS);
		zipsUsages = storageHelper.getNestedZipFolder(StorageCase.USAGES);
	}

	public void run() throws IOException {

		Set<String> files = dirHistories.findFiles(f -> f.endsWith(".zip"));

		for (String fileName : files) {
			System.out.printf("found: %s\n", fileName);
			IReadingArchive ra = dirHistories.getReadingArchive(fileName);

			int counter = 1;
			while (ra.hasNext()) {
				numTotal++;
				MicroCommit t = ra.getNext(MicroCommit.class);

				Usage a = t.getStart();
				Usage b = t.getEnd();

				ITypeName type = a.getType();

				countCommitFor(type);

				// if (isAddition(a, b)) {
				Integer i = counts.get(type);
				if (i == null) {
					counts.put(type, 1);
				} else {
					counts.put(type, ++i);
				}
				// }

				System.out.printf("%s, ", getDiff(a, b));

				if ((counter++) % 21 == 0) {
					counter = 1;
					System.out.println();
				}
			}
			System.out.println();
		}

		// should be zero
		int numSanity = numTotal - numAdditions - numRemovals - numNeither - numBoth;

		System.out.printf(
				"totals:\n-------\n%d MicroCommits - additions: %d (+both: %d), removals: %d (both: %d, neither: %d, sanity: %d)\n",
				numTotal, numAdditions, (numAdditions + numBoth), numRemovals, numBoth, numNeither, numSanity);

		Map<ITypeName, Integer> sortedCounts = MapSorter.sortByCount(counts);

		int numTypesWithCommitsAndUsages = 0;
		int totalUsages = 0;
		int totalCommitsWithUsages = 0;

		System.out.printf("\nwithout any filtering, we found %d micro commits for %d different types:\n", numTotal,
				counts.size());
		System.out.printf("count - type\n");
		for (ITypeName type : sortedCounts.keySet()) {
			int count = counts.get(type);
			int numUsages = getNumUsages(type);
			totalUsages += numUsages;
			System.out.printf("%3dx - %s", count, type, numUsages);

			int numCommits = allNumCommits.get(type);

			if (numUsages > 0 && numCommits > 0) {
				numTypesWithCommitsAndUsages++;
				totalCommitsWithUsages += numCommits;
				totalUsages += numUsages;
				System.out.printf(" (%d commits, %d usages)", numCommits, numUsages);
			}
			System.out.println();
		}

		System.out.printf("after filtering, we have both commits and usages for %d types\n",
				numTypesWithCommitsAndUsages);
		System.out.printf("for these types combined, we have %d commits and %d usages\n\n", totalCommitsWithUsages,
				totalUsages);

		System.out.printf("\nsummary:\n", counts.size());

		counttEqualOrSmallerThan(sortedCounts, 1, 2);
		counttEqualOrSmallerThan(sortedCounts, 2, 3);
		counttEqualOrSmallerThan(sortedCounts, 3, 5);
		counttEqualOrSmallerThan(sortedCounts, 5, 8);
		counttEqualOrSmallerThan(sortedCounts, 8, 13);
		counttEqualOrSmallerThan(sortedCounts, 13, 21);
		counttEqualOrSmallerThan(sortedCounts, 21, 40);
		counttEqualOrSmallerThan(sortedCounts, 40, 100);
		counttEqualOrSmallerThan(sortedCounts, 100, 1000);
		counttEqualOrSmallerThan(sortedCounts, 1000, 10000);
	}

	private void countCommitFor(ITypeName type) {
		Integer i = allNumCommits.get(type);
		if (i == null) {
			allNumCommits.put(type, 1);
		} else {
			allNumCommits.put(type, i + 1);
		}
	}

	private static boolean isAddition(Query a, Query b) {
		for (CallSite c : b.getReceiverCallsites()) {
			if (!a.getReceiverCallsites().contains(c)) {
				return true;
			}
		}
		return false;
	}

	private String getDiff(Usage a, Usage b) {

		int removed = 0;
		int added = 0;

		boolean isAddition = false;
		boolean isRemoval = false;

		for (CallSite c : a.getReceiverCallsites()) {
			if (!b.getAllCallsites().contains(c)) {
				isRemoval = true;
				removed++;
			}
		}
		for (CallSite c : b.getReceiverCallsites()) {
			if (!a.getAllCallsites().contains(c)) {
				isAddition = true;
				added++;
			}
		}

		if (isAddition && isRemoval) {
			numBoth++;
			return String.format("%s|%s", sign(added), sign(-1 * removed));
		} else if (isAddition) {
			numAdditions++;
			return String.format("%s", sign(added));
		} else if (isRemoval) {
			numRemovals++;
			return String.format("%s", sign(-1 * removed));
		} else {
			numNeither++;
			return String.format("n/a", sign(added), removed);
		}

	}

	private static Object sign(int num) {
		if (num > 0) {
			return "+" + num;
		}
		return "" + num;
	}

	private static void counttEqualOrSmallerThan(Map<ITypeName, Integer> counts, int lowerBound, int upperBound) {
		Map<ITypeName, Integer> matches = Maps.newLinkedHashMap();
		for (ITypeName type : counts.keySet()) {
			int count = counts.get(type);
			if (count >= lowerBound && count < upperBound) {
				matches.put(type, count);
			}
		}

		System.out.printf("%5dx %5d <= N < %d\n", matches.size(), lowerBound, upperBound);
	}

	private int getNumUsages(ITypeName type) {
		List<Usage> usages = readTrainingData(type, zipsUsages);
		return usages.size();
	}

	private List<Usage> readTrainingData(ITypeName type, NestedZipFolders<ITypeName> zipsUsages) {
		List<Query> qs = zipsUsages.readAllZips(type, Query.class);
		return Lists.newLinkedList(qs);
	}
}