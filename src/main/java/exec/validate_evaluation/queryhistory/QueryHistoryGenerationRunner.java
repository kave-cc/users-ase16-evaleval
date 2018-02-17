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
package exec.validate_evaluation.queryhistory;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import cc.kave.commons.model.naming.codeelements.IMethodName;
import cc.kave.commons.model.naming.types.ITypeName;
import cc.kave.rsse.calls.datastructures.Tuple;
import cc.kave.rsse.calls.usages.CallSite;
import cc.kave.rsse.calls.usages.CallSites;
import cc.kave.rsse.calls.usages.Query;
import cc.kave.rsse.calls.usages.Usage;
import exec.validate_evaluation.queryhistory.QueryHistoryCollector.QueryHistoryForStreak;
import exec.validate_evaluation.streaks.EditStreak;
import exec.validate_evaluation.streaks.EditStreakGenerationIo;
import exec.validate_evaluation.streaks.Snapshot;

public class QueryHistoryGenerationRunner {

	private final QueryHistoryIo io;
	private final QueryHistoryGenerationLogger log;
	private final IUsageExtractor usageExtractor;
	private final QueryHistoryCollector histCollector;
	private final EditStreakGenerationIo esIo;

	private Set<EditStreak> editStreaks;

	public QueryHistoryGenerationRunner(EditStreakGenerationIo esIo, QueryHistoryIo io,
			QueryHistoryGenerationLogger log, QueryHistoryCollector histCollector, IUsageExtractor usageExtractor) {
		this.esIo = esIo;
		this.io = io;
		this.log = log;
		this.histCollector = histCollector;
		this.usageExtractor = usageExtractor;
	}

	public void run() {
		Set<String> zips = esIo.findEditStreakZips();
		log.foundZips(zips);

		for (String zip : zips) {
			log.processingFile(zip);

			Set<List<Usage>> us = Sets.newHashSet();
			editStreaks = esIo.readEditStreaks(zip);

			log.foundEditStreaks(editStreaks.size());

			for (EditStreak es : editStreaks) {
				try {
					Set<List<Usage>> process = process(es);
					us.addAll(process);
				} catch (Exception e) {
					// TODO fix causing issue
					e.printStackTrace();
				}
			}

			io.storeQueryHistories(us, zip);
		}

		log.finish();
	}

	private Set<List<Usage>> process(EditStreak e) {

		log.processingEditStreak(e);

		Map<Snapshot, List<Usage>> allUsages = Maps.newHashMap();
		Map<Snapshot, Usage> allQueries = Maps.newHashMap();

		for (Snapshot s : e.getSnapshots()) {
			IAnalysisResult result = usageExtractor.analyse(s.getContext());
			allUsages.put(s, result.getUsages());
			allQueries.put(s, result.getFirstQuery());
		}

		QueryHistoryForStreak qhc = histCollector.startEditStreak(getKeys(allUsages));

		for (Snapshot s : e.getSnapshots()) {
			List<Usage> usages = allUsages.get(s);
			Usage query = allQueries.get(s);

			log.startSnapshot();
			qhc.startSnapshot();

			for (Usage u : usages) {
				qhc.register(u);
				log.usage();

				boolean isQuery = u.equals(query);
				if (isQuery && s.hasSelection()) {
					Usage u2 = merge(u, s.getSelection());
					qhc.registerSelectionResult(u2);
					log.usageMerged();
				}
			}

			qhc.endSnapshot();
		}

		return qhc.getHistories();
	}

	private Usage merge(Usage u, IMethodName m) {
		CallSite call = CallSites.createReceiverCallSite(m);
		Query q = Query.createAsCopyFrom(u);
		q.addCallSite(call);
		return q;
	}

	private static Set<Tuple<ITypeName, IMethodName>> getKeys(Map<Snapshot, List<Usage>> allUsages) {
		Set<Tuple<ITypeName, IMethodName>> keys = Sets.newHashSet();
		for (List<Usage> us : allUsages.values()) {
			for (Usage u : us) {
				Tuple<ITypeName, IMethodName> key = Tuple.newTuple(u.getType(), u.getMethodContext());
				keys.add(key);
			}
		}
		return keys;
	}
}