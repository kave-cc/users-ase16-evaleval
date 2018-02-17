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

import static cc.kave.commons.assertions.Asserts.assertTrue;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.Sets;

import cc.kave.commons.model.naming.Names;
import cc.kave.rsse.calls.usages.CallSite;
import cc.kave.rsse.calls.usages.CallSites;
import cc.kave.rsse.calls.usages.DefinitionSites;
import cc.kave.rsse.calls.usages.Query;
import cc.kave.rsse.calls.usages.Usage;

public class RandomQueryBuilderTest extends AbstractQueryBuilderTest {

	private RandomQueryBuilder sut;

	@Override
	protected IQueryBuilder<Usage, Query> createQueryBuilder() {
		sut = new RandomQueryBuilder(3);
		return sut;
	}

	@Test
	public void detailsAreCopiedOver() {
		List<Query> qs = sut.createQueries(q(), q(1));
		assertEquals(1, qs.size());
		Query actual = qs.get(0);
		Query expected = q();
		assertEquals(expected, actual);
	}

	@Test
	public void allCombinationsAreBuild() {
		List<Query> out = sut.createQueries(q(1), q(1, 2, 3));

		Set<Query> actuals = assertUniqueQueries(out);
		Set<Query> expecteds = Sets.newHashSet(q(1), q(2), q(3));
		assertEquals(expecteds, actuals);
	}

	@Test
	public void butNotMoreThanConfigured() {
		List<Query> out = sut.createQueries(q(1), q(1, 2, 3, 4));

		Set<Query> actuals = assertUniqueQueries(out);
		assertTrue(actuals.size() == 3);
	}

	@Test
	public void moreFancyCombinations() {
		List<Query> out = sut.createQueries(q(1, 2), q(1, 2, 3));

		Set<Query> actuals = assertUniqueQueries(out);
		Set<Query> expecteds = Sets.newHashSet(q(1, 2), q(1, 3), q(2, 3));
		assertEquals(expecteds, actuals);
	}

	@Test
	public void removalsAreNotCounted() {
		List<Query> out = sut.createQueries(q(1, 999), q(1, 2, 3));

		Set<Query> actuals = assertUniqueQueries(out);
		Set<Query> expecteds = Sets.newHashSet(q(1), q(2), q(3));
		assertEquals(expecteds, actuals);
	}

	private Set<Query> assertUniqueQueries(List<Query> input) {
		Set<Query> output = Sets.newLinkedHashSet();
		output.addAll(input);
		assertEquals(input.size(), output.size());
		return output;
	}

	private Query q(int... mIds) {
		Query q = new Query();
		q.setType(Names.newType("LT"));
		q.setClassContext(Names.newType("LC"));
		q.setMethodContext(Names.newMethod("LC.m()V"));
		q.setDefinition(DefinitionSites.createDefinitionByThis());
		for (int mId : mIds) {
			CallSite cs = CallSites.createReceiverCallSite("LT.m" + mId + "()V");
			q.addCallSite(cs);
		}
		return q;
	}
}