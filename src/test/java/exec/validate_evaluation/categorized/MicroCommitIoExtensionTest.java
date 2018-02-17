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
package exec.validate_evaluation.categorized;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import cc.kave.commons.model.naming.Names;
import cc.kave.commons.model.naming.types.ITypeName;
import cc.kave.rsse.calls.usages.CallSites;
import cc.kave.rsse.calls.usages.NoUsage;
import cc.kave.rsse.calls.usages.Query;
import cc.kave.rsse.calls.usages.Usage;
import exec.validate_evaluation.microcommits.MicroCommit;
import exec.validate_evaluation.microcommits.MicroCommitIo;

public class MicroCommitIoExtensionTest {

	private MicroCommitIo io;
	private MicroCommitIoExtension sut;

	@Before
	public void setup() {
		io = mock(MicroCommitIo.class);
		when(io.findZips()).thenReturn(Sets.newHashSet("a", "b", "c"));
		when(io.read("a")).thenReturn(Lists.newArrayList(mc(1), mc(2)));
		when(io.read("b")).thenReturn(Lists.newArrayList(mc(1), mc(3)));
		when(io.read("c")).thenReturn(Lists.newArrayList(mc(2), mc(3)));

		sut = new MicroCommitIoExtension(io);
	}

	@Test
	public void findZipsWithType() {
		assertEquals(Lists.newArrayList("a", "b"), sut.findZipsWith(type(1)));
		assertEquals(Lists.newArrayList("a", "c"), sut.findZipsWith(type(2)));
		assertEquals(Lists.newArrayList("b", "c"), sut.findZipsWith(type(3)));
	}

	@Test
	public void findZipsWith() {
		List<MicroCommit> mcs = Lists.newArrayList(mcCtx(1, 1, 1), mcCtx(1, 1, 2), mcCtx(1, 2, 3), mcCtx(2, 3, 4));
		io = mock(MicroCommitIo.class);
		when(io.findZips()).thenReturn(Sets.newHashSet("a"));
		when(io.read("a")).thenReturn(mcs);

		sut = new MicroCommitIoExtension(io);

		Set<List<MicroCommit>> actual = sut.readZipAndSortByLocation("a", type(1));
		Set<List<MicroCommit>> expected = Sets.newHashSet();
		expected.add(Lists.newArrayList(mcCtx(1, 1, 1), mcCtx(1, 1, 2)));
		expected.add(Lists.newArrayList(mcCtx(1, 2, 3)));
		assertEquals(expected, actual);
	}

	private MicroCommit mcCtx(int type, int ctx, int i) {
		Usage start = new NoUsage();
		Query end = new Query();
		end.setType(type(type));
		end.setMethodContext(Names.newMethod("LT.ctx" + ctx + "()V"));
		end.addCallSite(CallSites.createReceiverCallSite("LT.m" + i + "()V"));
		return MicroCommit.create(start, end);
	}

	private MicroCommit mc(int i) {
		Usage start = new NoUsage();
		Query end = new Query();
		end.setType(type(i));
		return MicroCommit.create(start, end);
	}

	private ITypeName type(int i) {
		return Names.newType("LT" + i);
	}
}