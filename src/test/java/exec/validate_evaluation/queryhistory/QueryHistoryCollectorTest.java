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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import cc.kave.commons.exceptions.AssertionException;
import cc.kave.commons.model.naming.Names;
import cc.kave.commons.model.naming.codeelements.IMethodName;
import cc.kave.commons.model.naming.types.ITypeName;
import cc.kave.rsse.calls.datastructures.Tuple;
import cc.kave.rsse.calls.usages.CallSites;
import cc.kave.rsse.calls.usages.NoUsage;
import cc.kave.rsse.calls.usages.Query;
import cc.kave.rsse.calls.usages.Usage;
import exec.validate_evaluation.queryhistory.QueryHistoryCollector.QueryHistoryForStreak;

public class QueryHistoryCollectorTest {

	private QueryHistoryCollector qhc;
	private QueryHistoryForStreak sut;
	private QueryHistoryGenerationLogger log;

	@Before
	public void setup() {
		log = mock(QueryHistoryGenerationLogger.class);
		qhc = new QueryHistoryCollector(log);
	}

	@Test(expected = AssertionException.class)
	public void cannotRegisterUsagesForUnknownKeys() {
		startStreak();
		sut.startSnapshot();
		sut.register(u(1, 2));
	}

	@Test
	public void canRegisterUsages() {
		startStreak(k(1, 1));

		snapshot(() -> {
			sut.register(u(1, 1));
		});
		snapshot(() -> {
			sut.register(u(1, 1, 1));
		});

		List<Usage> actuals = assertSingleHistory();
		List<Usage> expecteds = Lists.newArrayList(u(1, 1), u(1, 1, 1));
		assertEquals(expecteds, actuals);
	}

	@Test
	public void secondRegistrationOfTheSameKeyPerSnapshotIsIgnored() {
		startStreak(k(1, 1), k(1, 2));
		snapshot(() -> {
			sut.register(u(1, 1, 1));
			sut.register(u(1, 1, 2));
			sut.register(u(1, 2, 1));
		});
		snapshot(() -> {
			sut.register(u(1, 1, 1, 2));
			sut.register(u(1, 2, 1, 2));
		});
		Set<List<Usage>> actual = sut.getHistories();
		Set<List<Usage>> expected = Sets.newHashSet();
		expected.add(Lists.newArrayList(u(1, 1, 1), u(1, 1, 1, 2)));
		expected.add(Lists.newArrayList(u(1, 2, 1), u(1, 2, 1, 2)));
		assertEquals(expected, actual);
	}

	@Test
	public void missingUsagesAreAdded() {
		startStreak(k(1, 1), k(2, 2));
		snapshot(() -> {
			sut.register(u(1, 1));
		});
		snapshot(() -> {
			sut.register(u(1, 1, 1));
			sut.register(u(2, 2, 1));
		});

		Set<List<Usage>> actuals = sut.getHistories();
		Set<List<Usage>> expecteds = Sets.newLinkedHashSet();
		expecteds.add(Lists.newArrayList(u(1, 1), u(1, 1, 1)));
		expecteds.add(Lists.newArrayList(none(), u(2, 2, 1)));
		assertEquals(expecteds, actuals);
	}

	@Test
	public void directRepetitionsAreFiltered() {
		startStreak(k(1, 1));
		snapshot(() -> {
			sut.register(u(1, 1, 1));
		});
		snapshot(() -> {
			sut.register(u(1, 1, 2));
		});
		snapshot(() -> {
			sut.register(u(1, 1, 2));
		});
		snapshot(() -> {
			sut.register(u(1, 1, 3));
		});

		List<Usage> actuals = assertSingleHistory();
		List<Usage> expecteds = Lists.newArrayList(u(1, 1, 1), u(1, 1, 2), u(1, 1, 3));
		assertEquals(expecteds, actuals);

		verify(log).startFixingHistories();
		verify(log).fixedQueryHistory(-1);
	}

	@Test
	public void directRepetitionsAreFiltered_multiple() {
		startStreak(k(1, 1));
		snapshot(() -> {
			sut.register(u(1, 1, 1));
		});
		snapshot(() -> {
			sut.register(u(1, 1, 2));
		});
		snapshot(() -> {
			sut.register(u(1, 1, 2));
		});
		snapshot(() -> {
			sut.register(u(1, 1, 2));
		});
		snapshot(() -> {
			sut.register(u(1, 1, 3));
		});

		List<Usage> actuals = assertSingleHistory();
		List<Usage> expecteds = Lists.newArrayList(u(1, 1, 1), u(1, 1, 2), u(1, 1, 3));
		assertEquals(expecteds, actuals);

		verify(log).startFixingHistories();
		verify(log).fixedQueryHistory(-2);
	}

	@Test
	public void indirectRepetitionsAreNotFiltered() {
		startStreak(k(1, 1));
		snapshot(() -> {
			sut.register(u(1, 1, 1));
		});
		snapshot(() -> {
			sut.register(u(1, 1, 2));
		});
		snapshot(() -> {
			sut.register(u(1, 1, 3));
		});
		snapshot(() -> {
			sut.register(u(1, 1, 2));
		});

		List<Usage> actuals = assertSingleHistory();
		List<Usage> expecteds = Lists.newArrayList(u(1, 1, 1), u(1, 1, 2), u(1, 1, 3), u(1, 1, 2));
		assertEquals(expecteds, actuals);
	}

	@Test
	public void singleHistoryIsCompletelyRemoved() {
		startStreak(k(1, 1));
		snapshot(() -> {
			sut.register(u(1, 1, 1));
		});

		Set<List<Usage>> actuals = sut.getHistories();
		Set<List<Usage>> expecteds = Sets.newHashSet();
		assertEquals(expecteds, actuals);
	}

	@Test
	public void bothFiltersCombined() {
		startStreak(k(1, 1));
		snapshot(() -> {
			sut.register(u(1, 1, 1));
		});
		snapshot(() -> {
			sut.register(u(1, 1, 1));
		});

		Set<List<Usage>> actuals = sut.getHistories();
		Set<List<Usage>> expecteds = Sets.newHashSet();
		assertEquals(expecteds, actuals);
	}

	@Test
	public void selectionIsAdded() {
		startStreak(k(1, 1));

		snapshot(() -> {
			sut.register(u(1, 1, 1));
			sut.registerSelectionResult(u(1, 1, 1, 2));
		});

		snapshot(() -> {
			sut.register(u(1, 1, 3));
		});

		List<Usage> actuals = assertSingleHistory();
		List<Usage> expecteds = Lists.newArrayList(u(1, 1, 1), u(1, 1, 1, 2), u(1, 1, 3));
		assertEquals(expecteds, actuals);
	}

	@Test(expected = AssertionException.class)
	public void cannotRegisterTwoSelections() {
		startStreak(k(1, 1));

		sut.startSnapshot();
		sut.registerSelectionResult(u(1, 1, 1));
		sut.registerSelectionResult(u(1, 1, 1));
	}

	private void snapshot(InSnapshotAction fun) {
		sut.startSnapshot();
		fun.action();
		sut.endSnapshot();
	}

	private Usage none() {
		return new NoUsage();
	}

	private List<Usage> assertSingleHistory() {
		Collection<List<Usage>> res = sut.getHistories();
		assertEquals(1, res.size());
		return res.iterator().next();
	}

	@SafeVarargs
	private final void startStreak(Tuple<ITypeName, IMethodName>... ks) {
		Set<Tuple<ITypeName, IMethodName>> keys = Sets.newHashSet();
		for (Tuple<ITypeName, IMethodName> k : ks) {
			keys.add(k);
		}
		sut = qhc.startEditStreak(keys);
	}

	private Tuple<ITypeName, IMethodName> k(int typeNum, int methodNum) {
		return Tuple.newTuple(type(typeNum), ctx(methodNum));
	}

	private ITypeName type(int typeNum) {
		return Names.newType("T" + typeNum + ", P");
	}

	private Usage u(int typeNum, int methodNum, int... callNums) {
		Query q = new Query();
		q.setType(type(typeNum));
		q.setMethodContext(ctx(methodNum));
		for (int callNum : callNums) {
			q.addCallSite(CallSites.createReceiverCallSite(m(callNum)));
		}
		return q;
	}

	private IMethodName ctx(int methodNum) {
		return m(100 + methodNum);
	}

	private IMethodName m(int methodNum) {
		return Names.newMethod("LT.m" + methodNum + "()V");
	}

	private interface InSnapshotAction {
		public void action();
	}
}