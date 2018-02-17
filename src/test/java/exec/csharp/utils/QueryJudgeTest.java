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
package exec.csharp.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import cc.kave.commons.exceptions.AssertionException;
import cc.kave.commons.model.naming.Names;
import cc.kave.commons.model.naming.codeelements.IMethodName;
import cc.kave.rsse.calls.usages.CallSite;
import cc.kave.rsse.calls.usages.CallSites;
import cc.kave.rsse.calls.usages.DefinitionSites;
import cc.kave.rsse.calls.usages.NoUsage;
import cc.kave.rsse.calls.usages.Query;
import cc.kave.rsse.calls.usages.Usage;
import exec.validate_evaluation.microcommits.MicroCommit;

public class QueryJudgeTest {

	private Query a;
	private Query b;

	@Test(expected = AssertionException.class)
	public void nullAsFirst() {
		a = createQuery();
		b = null;
		judge();
	}

	@Test(expected = AssertionException.class)
	public void nullAsSecond() {
		a = null;
		b = createQuery();
		judge();
	}

	@Test(expected = AssertionException.class)
	public void nullAsCommit() {
		new QueryJudge(null);
	}

	@Test
	public void bothInitsAreExchangable() {
		a = createQuery(1);
		b = createQuery(1, 2);

		QueryJudge j1 = new QueryJudge(a, b);
		QueryJudge j2 = new QueryJudge(MicroCommit.create(a, b));
		assertEquals(j1.getNumAdditions(), j2.getNumAdditions());
	}

	@Test
	public void numAdditions() {
		a = createQuery(1);
		b = createQuery(1, 2);
		assertEquals(1, judge().getNumAdditions());
	}

	@Test
	public void numAdditions_removal() {
		a = createQuery(1, 9);
		b = createQuery(1, 2);
		assertEquals(1, judge().getNumAdditions());
	}

	@Test
	public void hasAdditions() {
		a = createQuery(1);
		b = createQuery(1);
		assertEquals(false, judge().hasAdditions());

		a = createQuery(1);
		b = createQuery(1, 2);
		assertEquals(true, judge().hasAdditions());
	}

	@Test
	public void numRemovals() {
		a = createQuery(1, 2);
		b = createQuery(1);
		assertEquals(1, judge().getNumRemovals());
	}

	@Test
	public void numRemovals_addition() {
		a = createQuery(1, 2);
		b = createQuery(1, 9);
		assertEquals(1, judge().getNumRemovals());
	}

	@Test
	public void hasRemovals() {
		a = createQuery(1);
		b = createQuery(1);
		assertEquals(false, judge().hasRemovals());

		a = createQuery(1, 2);
		b = createQuery(1);
		assertEquals(true, judge().hasRemovals());
	}

	@Test
	public void hasDefChange() {
		a = createQuery(1);
		b = createQuery(1);
		assertEquals(false, judge().hasDefChange());
		b.setDefinition(DefinitionSites.createUnknownDefinitionSite());
		assertEquals(true, judge().hasDefChange());
	}

	@Test
	public void judging_nq() {
		Usage a = new NoUsage();
		Usage b = createQuery(1, 2);
		QueryJudge sut = new QueryJudge(a, b);
		assertTrue(sut.hasAdditions());
		assertEquals(2, sut.getNumAdditions());
		assertFalse(sut.hasRemovals());
		assertEquals(0, sut.getNumRemovals());
		assertTrue(sut.hasDefChange());
		assertEquals(NoiseMode.FROM_SCRATCH, sut.getNoiseMode());
	}

	@Test
	public void judging_qn() {
		Usage a = createQuery(1, 2);
		Usage b = new NoUsage();
		QueryJudge sut = new QueryJudge(a, b);
		assertFalse(sut.hasAdditions());
		assertEquals(0, sut.getNumAdditions());
		assertTrue(sut.hasRemovals());
		assertEquals(2, sut.getNumRemovals());
		assertTrue(sut.hasDefChange());
		assertEquals(NoiseMode.PURE_REMOVAL, sut.getNoiseMode());
	}

	private QueryJudge judge() {
		return new QueryJudge(MicroCommit.create(a, b));
	}

	private Query createQuery(int... mIds) {
		Query q = new Query();
		q.setType(Names.newType("T, P"));
		q.setClassContext(Names.newType("Ctx, P"));
		q.setMethodContext(method("Ctx", "m"));
		q.setDefinition(DefinitionSites.createDefinitionByThis());
		for (int mID : mIds) {
			IMethodName m = method("T", "m" + mID);
			CallSite cs = CallSites.createReceiverCallSite(m);
			q.addCallSite(cs);
		}
		return q;
	}

	private IMethodName method(String cName, String mName) {
		return Names.newMethod("L%s.%s()V", cName, mName);
	}
}