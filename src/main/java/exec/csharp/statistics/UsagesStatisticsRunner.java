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
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.inject.Inject;

import cc.kave.commons.model.naming.types.ITypeName;
import cc.kave.commons.utils.io.Directory;
import cc.kave.commons.utils.io.IReadingArchive;
import cc.kave.rsse.calls.usages.Query;
import cc.kave.rsse.calls.usages.Usage;
import exec.csharp.utils.MapSorter;
import exec.csharp.utils.StorageCase;
import exec.csharp.utils.StorageHelper;

public class UsagesStatisticsRunner {

	private Directory dirUsages;
	private Map<ITypeName, Integer> counts = Maps.newLinkedHashMap();

	@Inject
	public UsagesStatisticsRunner(StorageHelper storageHelper) {
		dirUsages = storageHelper.getDirectory(StorageCase.USAGES);
	}

	public void run() throws IOException {
		Set<String> files = dirUsages.findFiles(f -> f.endsWith(".zip"));

		for (String fileName : files) {
			System.out.printf("found: %s\n", fileName);

			IReadingArchive ra = dirUsages.getReadingArchive(fileName);

			while (ra.hasNext()) {
				Usage u = ra.getNext(Query.class);

				ITypeName type = u.getType();

				Integer i = counts.get(type);
				if (i == null) {
					counts.put(type, 1);
				} else {
					counts.put(type, ++i);
				}
			}
		}

		Map<ITypeName, Integer> sortedCounts = MapSorter.sortByCount(counts);

		for (ITypeName type : sortedCounts.keySet()) {
			int count = counts.get(type);

			System.out.printf("%10dx %s\n", count, type);
		}

		System.out.printf("we found %d different types, out of which:\n", counts.size());

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
}