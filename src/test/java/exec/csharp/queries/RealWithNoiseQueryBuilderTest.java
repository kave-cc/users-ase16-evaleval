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
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import cc.kave.commons.model.naming.Names;
import cc.kave.commons.model.naming.codeelements.IMethodName;
import cc.kave.commons.model.naming.types.ITypeName;
import cc.kave.rsse.calls.usages.CallSite;
import cc.kave.rsse.calls.usages.CallSites;
import cc.kave.rsse.calls.usages.DefinitionSite;
import cc.kave.rsse.calls.usages.Query;
import cc.kave.rsse.calls.usages.Usage;

public class RealWithNoiseQueryBuilderTest extends AbstractQueryBuilderTest {

	private IQueryBuilder<Usage, Query> sut;

	private Query start;
	private Query end;

	@Override
	protected IQueryBuilder<Usage, Query> createQueryBuilder() {
		sut = new RealWithNoiseQueryBuilder();
		return sut;
	}

	@Test
	public void copyIsCreated() {
		start = new Query();
		end = new Query();

		Query q = assertSingleQuery();
		assertNotSame(start, q);
		assertNotSame(end, q);
	}

	@Test
	public void basicInfoAreCopiedFromStart() {
		ITypeName type = mock(ITypeName.class);
		ITypeName cctx = mock(ITypeName.class);
		IMethodName mctx = mock(IMethodName.class);
		DefinitionSite defSite = mock(DefinitionSite.class);

		start = new Query();
		start.setType(type);
		start.setClassContext(cctx);
		start.setMethodContext(mctx);
		start.setDefinition(defSite);

		end = new Query();
		end.addCallSite(callSite(1));

		Query q = assertSingleQuery();
		assertSame(type, q.getType());
		assertSame(cctx, q.getClassContext());
		assertSame(mctx, q.getMethodContext());
		assertSame(defSite, q.getDefinitionSite());
	}

	@Test
	public void noNoiseWorks() {
		start = q(1);
		end = q(1, 2);
		assertQuery(1);
	}

	@Test
	public void noiseIsCopiedAsWell() {
		start = q(1, 3);
		end = q(1, 2);
		assertQuery(1, 3);
	}

	private Query q(int... mIds) {
		Query q = new Query();
		for (int mId : mIds) {
			q.addCallSite(callSite(mId));
		}
		return q;
	}

	private CallSite callSite(int mId) {
		IMethodName m = Names.newMethod("LT.m" + mId + "()V");
		return CallSites.createReceiverCallSite(m);
	}

	private void assertQuery(int... mIds) {
		Query q = assertSingleQuery();
		assertEquals(mIds.length, q.getAllCallsites().size());
		for (int mId : mIds) {
			CallSite cs = callSite(mId);
			assertTrue(q.getAllCallsites().contains(cs));
		}
	}

	private Query assertSingleQuery() {
		List<Query> qs = sut.createQueries(start, end);
		Assert.assertEquals(1, qs.size());
		return qs.get(0);
	}
}