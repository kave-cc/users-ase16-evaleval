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
package exec.csharp.queries;

import java.util.List;
import java.util.Set;

import org.apache.commons.math.util.MathUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import cc.kave.commons.utils.SublistSelector;
import cc.kave.rsse.calls.usages.CallSite;
import cc.kave.rsse.calls.usages.Query;
import cc.kave.rsse.calls.usages.Usage;
import exec.csharp.utils.QueryUtils;

public class RandomQueryBuilder extends AbstractQueryBuilder {

	private int maxNumQueries;

	public RandomQueryBuilder(int maxNumQueries) {
		this.maxNumQueries = maxNumQueries;
	}

	@Override
	public List<Query> createQueries(Usage start, Usage end) {

		int numAfter = end.getReceiverCallsites().size();
		int numAdditions = QueryUtils.countAdditions(start, end);
		int numBefore = numAfter - numAdditions;

		// int maxNumPossible = getMaxNumPossible(numBefore, numAfter);
		// if (maxNumPossible <= maxNumQueries) {
		// return createAllPossibleQueries(end, numBefore);
		// } else {
		return createRandomQueries(end, numAdditions);
		// }

	}

	public int getMaxNumPossible(int numBefore, int numAfter) {
		int maxPossible = (int) MathUtils.binomialCoefficient(numAfter, numBefore);
		return Math.min(maxNumQueries, maxPossible);
	}

	// private List<Query> createAllPossibleQueries(Usage end, int numBefore) {
	// // TODO Auto-generated method stub
	// return null;
	// }

	private List<Query> createRandomQueries(Usage end, int numToRemove) {
		Set<CallSite> allSites = end.getReceiverCallsites();
		Set<Set<CallSite>> randomSelections = Sets.newLinkedHashSet();

		int iterations = 0;
		while (randomSelections.size() < maxNumQueries && iterations++ < 100) {
			Set<CallSite> rndSelection = rndSelect(numToRemove, allSites);
			randomSelections.add(rndSelection);
		}

		List<Query> queries = Lists.newLinkedList();
		for (Set<CallSite> sites : randomSelections) {
			Query q = Query.createAsCopyFrom(end);
			q.setAllCallsites(sites);
			queries.add(q);
		}

		return queries;
	}

	private Set<CallSite> rndSelect(int numToRemove, Set<CallSite> allSites) {

		if (numToRemove == 0) {
			return allSites;
		}

		Set<CallSite> shuffled = SublistSelector.shuffle(allSites);
		for (int i = 0; i < numToRemove; i++) {
			CallSite cs = shuffled.iterator().next();
			shuffled.remove(cs);
		}

		return shuffled;
	}
}