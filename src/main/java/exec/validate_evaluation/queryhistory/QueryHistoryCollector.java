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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import cc.kave.commons.assertions.Asserts;
import cc.kave.commons.model.naming.codeelements.IMethodName;
import cc.kave.commons.model.naming.types.ITypeName;
import cc.kave.rsse.calls.datastructures.Tuple;
import cc.kave.rsse.calls.usages.NoUsage;
import cc.kave.rsse.calls.usages.Usage;

public class QueryHistoryCollector {

	private final QueryHistoryGenerationLogger log;

	public QueryHistoryCollector(QueryHistoryGenerationLogger log) {
		this.log = log;
	}

	public QueryHistoryForStreak startEditStreak(Set<Tuple<ITypeName, IMethodName>> keys) {
		return new QueryHistoryForStreak(keys);
	}

	public class QueryHistoryForStreak {

		private Set<Tuple<ITypeName, IMethodName>> keysInEditStreak;
		private Set<Tuple<ITypeName, IMethodName>> keysInThisSnapshot = Sets.newHashSet();

		private Map<Tuple<ITypeName, IMethodName>, List<Usage>> queryHistories = Maps.newLinkedHashMap();

		private Usage selectionResult;

		public QueryHistoryForStreak(Set<Tuple<ITypeName, IMethodName>> keys) {
			this.keysInEditStreak = keys;
		}

		public void startSnapshot() {
		}

		public void noSnapshots() {
		}

		public void endSnapshot() {

			if (selectionResult != null) {
				registerByKey(selectionResult, getKey(selectionResult));
				selectionResult = null;
			}

			for (Tuple<ITypeName, IMethodName> key : keysInEditStreak) {
				if (!keysInThisSnapshot.contains(key)) {
					registerByKey(new NoUsage(), key);
				}
			}

			keysInThisSnapshot.clear();
		}

		public void register(Usage u) {
			Tuple<ITypeName, IMethodName> key = getKey(u);

			if (keysInThisSnapshot.contains(key)) {
				// generics currently blow our concept... as do instance-aware
				// points-to analyses
				return;
			}

			registerByKey(u, key);
		}

		public void registerSelectionResult(Usage u2) {
			Asserts.assertNull(selectionResult);
			selectionResult = u2;
		}

		private void registerByKey(Usage u, Tuple<ITypeName, IMethodName> key) {
			Asserts.assertTrue(keysInEditStreak.contains(key));

			keysInThisSnapshot.add(key);

			List<Usage> qh = queryHistories.get(key);
			if (qh == null) {
				qh = Lists.newLinkedList();
				queryHistories.put(key, qh);
			}
			qh.add(u);
		}

		private Tuple<ITypeName, IMethodName> getKey(Usage u) {
			return Tuple.newTuple(u.getType(), u.getMethodContext());
		}

		public Set<List<Usage>> getHistories() {
			removeRepeatingUsages();
			removeSingleHistories();
			return Sets.newLinkedHashSet(queryHistories.values());
		}

		private void removeRepeatingUsages() {
			log.startFixingHistories();
			for (List<Usage> qh : queryHistories.values()) {
				removeRepeatingUsages(qh);
			}
		}

		private void removeRepeatingUsages(List<Usage> qh) {
			Usage last = null;
			int diff = 0;

			for (Iterator<Usage> it = qh.iterator(); it.hasNext();) {
				Usage u = it.next();

				if (u.equals(last)) {
					it.remove();
					diff++;
				}

				last = u;
			}

			if (diff > 0) {
				log.fixedQueryHistory(-diff);
			}
		}

		private void removeSingleHistories() {
			log.startingRemoveEmptyHistories();
			queryHistories.entrySet().removeIf(e -> {
				if (e.getValue().size() < 2) {
					log.removedEmptyHistory();
					return true;
				}
				return false;
			});

			// TODO: remove histories that start and end with NoUsage
		}
	}
}