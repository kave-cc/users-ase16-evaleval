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

import static exec.csharp.utils.QueryUtils.countAdditions;
import static exec.csharp.utils.QueryUtils.countRemovals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import cc.kave.commons.model.naming.Names;
import cc.kave.rsse.calls.usages.CallSite;
import cc.kave.rsse.calls.usages.CallSites;
import cc.kave.rsse.calls.usages.DefinitionSites;
import cc.kave.rsse.calls.usages.NoUsage;
import cc.kave.rsse.calls.usages.Query;
import cc.kave.rsse.calls.usages.Usage;

public class QueryUtilsTest {

	@Test
	public void countAdditions_noDiff() {
		assertEquals(0, countAdditions(q(1), q(1)));
	}

	@Test
	public void countAdditions_addition_qq() {
		assertEquals(1, countAdditions(q(1), q(1, 2)));
	}

	@Test
	public void countAdditions_addition_nq() {
		assertEquals(1, countAdditions(new NoUsage(), q(1)));
	}

	@Test
	public void countAdditions_addition_qn() {
		assertEquals(0, countAdditions(q(1), new NoUsage()));
	}

	@Test
	public void countAdditions_addition_nn() {
		assertEquals(0, countAdditions(new NoUsage(), new NoUsage()));
	}

	@Test
	public void countAdditions_removal() {
		assertEquals(0, countAdditions(q(1, 2), q(1)));
	}

	@Test
	public void countRemovals_noDiff() {
		assertEquals(0, countRemovals(q(1), q(1)));
	}

	@Test
	public void countRemovals_addition() {
		assertEquals(0, countRemovals(q(1), q(1, 2)));
	}

	@Test
	public void countRemovals_removal_qq() {
		assertEquals(1, countRemovals(q(1, 2), q(1)));
	}

	@Test
	public void countRemovals_removal_nq() {
		assertEquals(0, countRemovals(new NoUsage(), q(1)));
	}

	@Test
	public void countRemovals_removal_qn() {
		assertEquals(2, countRemovals(q(1, 2), new NoUsage()));
	}

	@Test
	public void countRemovals_removal_nn() {
		assertEquals(0, countRemovals(new NoUsage(), new NoUsage()));
	}

	@Test
	public void toDiffString_equalQueries() {
		Query a = q(1);
		Query b = q(1);

		assertDiffToString(a, b, "1");
	}

	@Test
	public void toDiffString_addedCalls() {
		Query a = q(1);
		Query b = q(1, 2);

		assertDiffToString(a, b, "1+1");
	}

	@Test
	public void toDiffString_removedCalls() {
		Query a = q(1, 2);
		Query b = q(1);

		assertDiffToString(a, b, "2-1");
	}

	@Test
	public void toDiffString_addedAndRemovedCalls() {
		Query a = q(1, 2);
		Query b = q(1, 3, 4);

		assertDiffToString(a, b, "2-1+2");
	}

	@Test
	public void toDiffString_differentType() {
		Query a = q(1);
		a.setType(Names.newType("LT2"));
		Query b = q(1);

		assertDiffToString(a, b, "1~T");
	}

	@Test
	public void toDiffString_differentClassContext() {
		Query a = q(1);
		a.setClassContext(Names.newType("LSome2"));
		Query b = q(1);

		assertDiffToString(a, b, "1~C");
	}

	@Test
	public void toDiffString_differentMethodContext() {
		Query a = q(1);
		a.setMethodContext(Names.newMethod("LS2.m()V"));
		Query b = q(1);

		assertDiffToString(a, b, "1~M");
	}

	@Test
	public void toDiffString_differentDefinition() {
		Query a = q(1);
		a.setDefinition(DefinitionSites.createDefinitionByThis());
		Query b = q(1);

		assertDiffToString(a, b, "1~D");
	}

	@Test
	public void toDiffString_integration() {
		Query a = q(1, 2);
		a.setType(Names.newType("LT2"));
		a.setClassContext(Names.newType("LSome2"));
		a.setMethodContext(Names.newMethod("LS2.m()V"));
		a.setDefinition(DefinitionSites.createDefinitionByThis());
		Query b = q(1, 3, 4);

		assertDiffToString(a, b, "2-1+2~TCMD");
	}

	@Test
	public void toDiffString_completeRemoval() {
		assertDiffToString(q(1), new NoUsage(), "~[-1]");
	}

	@Test
	public void toDiffString_completeRemoval2() {
		assertDiffToString(q(1, 2), new NoUsage(), "~[-2]");
	}

	@Test
	public void toDiffString_fromScratch() {
		assertDiffToString(new NoUsage(), q(1), "~[+1]");
	}

	@Test
	public void toDiffString_fromScratch2() {
		assertDiffToString(new NoUsage(), q(1, 2), "~[+2]");
	}

	@Test
	public void toDiffString_NoUsageToNoUsage() {
		assertDiffToString(new NoUsage(), new NoUsage(), "[- -> -]");
	}

	private void assertDiffToString(Usage a, Usage b, String expected) {
		String actual = QueryUtils.toDiffString(a, b);
		assertEquals(expected, actual);
	}

	private Query q(int... mIds) {
		Query q = new Query();
		q.setType(Names.newType("Ln/T"));
		q.setClassContext(Names.newType("LSome"));
		q.setMethodContext(Names.newMethod("LS.m()V"));
		q.setDefinition(DefinitionSites.createDefinitionByConstant());
		for (int mId : mIds) {
			CallSite cs = CallSites.createReceiverCallSite("Ln/T.m" + mId + "()V");
			q.addCallSite(cs);
		}
		return q;
	}
}