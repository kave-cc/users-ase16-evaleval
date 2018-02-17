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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import cc.kave.commons.assertions.Asserts;
import cc.kave.commons.evaluation.Boxplot;
import cc.kave.commons.evaluation.BoxplotData;
import cc.kave.commons.model.naming.codeelements.IMethodName;
import cc.kave.commons.model.naming.types.ITypeName;
import cc.kave.commons.utils.io.NestedZipFolders;
import cc.kave.rsse.calls.usages.NoUsage;
import cc.kave.rsse.calls.usages.Usage;
import exec.validate_evaluation.queryhistory.QueryHistoryIo;

public class QueryHistoryStats {

	private QueryHistoryIo io;
	private Set<ITypeName> usedTypes;
	private int numUsagesForTypes = 0;

	private Map<ITypeName, Map<IMethodName, List<Usage>>> uhByUser;
	private NestedZipFolders<ITypeName> usages;

	public QueryHistoryStats(QueryHistoryIo io, NestedZipFolders<ITypeName> usages) {
		this.io = io;
		this.usages = usages;
	}

	public void run() {

		int numHistoriesWithoutReferenceData = 0;
		BoxplotData usageHistoryLengths = new BoxplotData();

		usedTypes = Sets.newHashSet();

		for (String zip : io.findQueryHistoryZips()) {

			uhByUser = Maps.newHashMap();

			System.out.println();
			System.out.printf("#####\n");
			System.out.printf("##### %s\n", zip);
			System.out.printf("#####\n");
			System.out.println();

			Collection<List<Usage>> hists = io.readQueryHistories(zip);
			System.out.printf("%d histories:\n", hists.size());

			for (List<Usage> hist : hists) {

				ITypeName type = findFirstRealUsage(hist).getType();
				if (usages.hasZips(type)) {
					if (!usedTypes.contains(type)) {
						usedTypes.add(type);
						List<Usage> us = usages.readAllZips(type, Usage.class);
						numUsagesForTypes += us.size();
					}
					usageHistoryLengths.add((double) hist.size());
					sort(hist);
				} else {
					numHistoriesWithoutReferenceData++;
				}
			}

			print();
		}

		System.out.println();
		System.out.println();

		System.out.printf("--- overall stats ---\n");
		System.out.printf("numUsedTypes: %d\n", usedTypes.size());
		System.out.printf("numUsagesForTypes: %d\n", numUsagesForTypes);
		System.out.printf("numUHWithoutData: %d\n", numHistoriesWithoutReferenceData);
		System.out.printf("uhLen stats: %s\n", usageHistoryLengths.getBoxplot());
		System.out.println("percentiles for len(uh):");
		for (int percentile = 80; percentile < 101; percentile += 1) {
			double len = usageHistoryLengths.getPercentil(percentile);
			System.out.printf("\t%d covered with a uh len of %.1f\n", percentile, len);
		}
		System.out.println();
		System.out.printf("numHistories: %d (%d collisions)\n", numLocations, collisions);
		Boxplot bp = avgHistLength.getBoxplot();
		System.out.printf("avgLength: %.1f usages %s\n", bp.getMean(), bp);
	}

	private int numLocations = 0;
	private BoxplotData avgHistLength = new BoxplotData();

	private void print() {

		for (ITypeName t : uhByUser.keySet()) {
			System.out.println();
			System.out.printf("#### usages of '%s' ####\n", t);
			Map<IMethodName, List<Usage>> hists = uhByUser.get(t);
			for (IMethodName m : hists.keySet()) {
				System.out.println();
				System.out.printf("in: %s\n", m);

				List<Usage> hist = hists.get(m);

				numLocations++;
				avgHistLength.add((double) hist.size());

				for (Usage usage : hist) {
					if (usage instanceof NoUsage) {
						System.out.printf("_, ");
					} else {
						System.out.printf("%d, ", usage.getReceiverCallsites().size());
					}
				}

				System.out.println();
			}
		}
	}

	private int collisions = 0;

	private void sort(List<Usage> usageHistory) {

		Usage first = findFirstRealUsage(usageHistory);
		ITypeName type = first.getType();
		IMethodName context = first.getMethodContext();

		List<Usage> uhByTypeAndContext = get(type, context);

		for (Usage u : usageHistory) {
			if (!(u instanceof NoUsage)) {
				Asserts.assertTrue(type.equals(u.getType()));
				Asserts.assertTrue(context.equals(u.getMethodContext()));
			}
			uhByTypeAndContext.add(u);
		}
	}

	private List<Usage> get(ITypeName type, IMethodName context) {
		Map<IMethodName, List<Usage>> uhByType = uhByUser.get(type);
		if (uhByType == null) {
			uhByType = Maps.newLinkedHashMap();
			uhByUser.put(type, uhByType);
		}

		List<Usage> uhByTypeAndContext = uhByType.get(context);
		if (uhByTypeAndContext == null) {
			uhByTypeAndContext = Lists.newLinkedList();
			uhByType.put(context, uhByTypeAndContext);
		} else {
			collisions++;
			System.out.flush();
			System.err.printf("collision for context '%s'\n", context);
			System.err.flush();
		}

		return uhByTypeAndContext;
	}

	private Usage findFirstRealUsage(List<Usage> hist) {
		Iterator<Usage> it = hist.iterator();
		while (it.hasNext()) {
			Usage usage = it.next();
			if (!(usage instanceof NoUsage)) {
				return usage;
			}
		}
		throw new RuntimeException("at least one real usage should always exist!");
	}
}