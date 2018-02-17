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
package exec.validate_evaluation.microcommits;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import cc.kave.rsse.calls.usages.CallSites;
import cc.kave.rsse.calls.usages.Query;
import cc.kave.rsse.calls.usages.Usage;
import exec.validate_evaluation.queryhistory.QueryHistoryIo;

public class FinalStateMicroCommitGenerationRunnerTest {
	private MicroCommitIo mcIo;
	private QueryHistoryIo qhIo;
	private MicroCommitGenerationLogger log;

	private FinalStateMicroCommitGenerationRunner sut;

	private Map<String, List<List<Usage>>> in;
	private Map<String, List<MicroCommit>> out;

	@Before
	public void setup() {
		in = Maps.newLinkedHashMap();
		out = Maps.newLinkedHashMap();

		mockQueryHistoryIo();
		mockMicroCommitIo();
		log = mock(MicroCommitGenerationLogger.class);

		sut = new FinalStateMicroCommitGenerationRunner(qhIo, mcIo, log);
	}

	private void mockQueryHistoryIo() {
		qhIo = mock(QueryHistoryIo.class);

		when(qhIo.findQueryHistoryZips()).then(new Answer<Set<String>>() {
			@Override
			public Set<String> answer(InvocationOnMock invocation) throws Throwable {
				return in.keySet();
			}
		});

		when(qhIo.readQueryHistories(anyString())).then(new Answer<List<List<Usage>>>() {
			@Override
			public List<List<Usage>> answer(InvocationOnMock invocation) throws Throwable {
				String zip = (String) invocation.getArguments()[0];
				return in.get(zip);
			}
		});
	}

	private void mockMicroCommitIo() {
		mcIo = mock(MicroCommitIo.class);
		doAnswer(new Answer<Void>() {
			@Override
			@SuppressWarnings("unchecked")
			public Void answer(InvocationOnMock inv) throws Throwable {
				List<MicroCommit> commits = (List<MicroCommit>) inv.getArguments()[0];
				String zip = (String) inv.getArguments()[1];
				out.put(zip, commits);
				return null;
			}
		}).when(mcIo).store(anyListOf(MicroCommit.class), anyString());
	}

	@Test
	public void noProblemWithMultipleFilesOrHistories() {

		register("a.zip", history(usage(1), usage(2)));
		register("b.zip", //
				history(usage(3), usage(4)), //
				history(usage(5), usage(6)) //
		);

		sut.run();

		assertNumFiles(2);
		assertCommits("a.zip", commit(usage(1), usage(2)));
		assertCommits("b.zip", //
				commit(usage(3), usage(4)), //
				commit(usage(5), usage(6)));
	}

	@Test
	public void correctCreationOfIntermediateHistories() {

		register("a.zip", history(usage(1), usage(2), usage(3)));

		sut.run();

		assertNumFiles(1);
		assertCommits("a.zip", //
				commit(usage(1), usage(3)), //
				commit(usage(2), usage(3)) //
		);
	}

	@Test
	public void allLoggerMethodsAreCalled() {
		noProblemWithMultipleFilesOrHistories();

		verify(log).foundZips(Sets.newHashSet("a.zip", "b.zip"));

		verify(log).processingZip("a.zip");
		verify(log).foundHistories(1);
		verify(log, times(3)).convertedToCommits(2, 1);

		verify(log).processingZip("b.zip");
		verify(log).foundHistories(2);
		// convertedToCommits?... see above

		verify(log).done();

		verifyNoMoreInteractions(log);
	}

	@Test
	public void largeConversionsCreateAppropriateLog() {
		correctCreationOfIntermediateHistories();
		verify(log).convertedToCommits(3, 2);
	}

	private void assertNumFiles(int expected) {
		assertEquals(expected, out.keySet().size());
	}

	private void assertCommits(String zip, MicroCommit... commit) {
		List<MicroCommit> expecteds = Lists.newArrayList(commit);
		assertEquals(expecteds, out.get(zip));
	}

	private MicroCommit commit(Usage a, Usage b) {
		return MicroCommit.create(a, b);
	}

	@SafeVarargs
	private final void register(String zip, List<Usage>... historyArr) {
		in.put(zip, Lists.newArrayList(historyArr));
	}

	private List<Usage> history(Usage... usageArr) {
		return Lists.newArrayList(usageArr);
	}

	private Usage usage(int num) {
		Query q = new Query();
		q.addCallSite(CallSites.createReceiverCallSite("LT.m" + num + "()V"));
		return q;
	}
}