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
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import cc.kave.commons.model.events.completionevents.Context;
import cc.kave.commons.model.naming.Names;
import cc.kave.commons.model.naming.codeelements.IMethodName;
import cc.kave.commons.model.naming.types.ITypeName;
import cc.kave.commons.model.ssts.impl.declarations.FieldDeclaration;
import cc.kave.rsse.calls.datastructures.Tuple;
import cc.kave.rsse.calls.usages.CallSites;
import cc.kave.rsse.calls.usages.NoUsage;
import cc.kave.rsse.calls.usages.Query;
import cc.kave.rsse.calls.usages.Usage;
import exec.validate_evaluation.queryhistory.QueryHistoryCollector.QueryHistoryForStreak;
import exec.validate_evaluation.streaks.EditStreak;
import exec.validate_evaluation.streaks.EditStreakGenerationIo;
import exec.validate_evaluation.streaks.Snapshot;

public class QueryHistoryGenerationRunnerTest {

	@Captor
	private ArgumentCaptor<Set<List<Usage>>> usageCaptor;

	private Map<String, Set<EditStreak>> in;
	private Map<String, Set<List<Usage>>> out;

	private QueryHistoryGenerationRunner sut;

	private QueryHistoryCollector histCollector;
	private Set<List<Usage>> collectedUsages;

	private IUsageExtractor usageExtractor;

	private List<Usage> curUsages;

	private QueryHistoryForStreak qhfs;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		in = Maps.newLinkedHashMap();
		out = Maps.newLinkedHashMap();

		EditStreakGenerationIo esIo = mockEditStreakIo();
		QueryHistoryIo io = mockQueryHistoryIo();

		QueryHistoryGenerationLogger log = mock(QueryHistoryGenerationLogger.class);
		// mockQueryHistoryCollector();
		histCollector = new QueryHistoryCollector(log);
		mockUsageExtractor();
		sut = new QueryHistoryGenerationRunner(esIo, io, log, histCollector, usageExtractor);
	}

	private void mockUsageExtractor() {
		usageExtractor = mock(IUsageExtractor.class);
		for (int num = 1; num <= 10; num++) {
			for (int i = 0; i < num; i++) {
				mockUsageExtraction(num, i);
			}
		}
	}

	private void mockUsageExtraction(int num, int i) {
		when(usageExtractor.analyse(context(num, i))).thenAnswer(new Answer<IAnalysisResult>() {
			@Override
			public IAnalysisResult answer(InvocationOnMock invocation) throws Throwable {
				return analysisResult(num, i);
			}
		});
	}

	private IAnalysisResult analysisResult(int num, int i) {

		Query query = new Query();
		query.setMethodContext(methodContext(num, i));
		query.addCallSite(CallSites.createReceiverCallSite("LT.query()V"));

		IAnalysisResult result = mock(IAnalysisResult.class);
		when(result.getUsages()).thenReturn(usages(num, i));
		when(result.getFirstQuery()).thenReturn(query);

		return result;
	}

	private List<Usage> usages(int num, int i) {
		return Lists.newArrayList(usage(num, i));
	}

	private Query usage(int num, int i) {
		Query u = new Query();
		u.setType(Names.newType("LT-" + num + "-" + i));
		u.setMethodContext(methodContext(num, i));
		u.addCallSite(CallSites.createReceiverCallSite("LT.m()V"));
		return u;
	}

	private IMethodName methodContext(int num, int i) {
		return Names.newMethod("LT.num%di%d()V", num, i);
	}

	private EditStreakGenerationIo mockEditStreakIo() {
		EditStreakGenerationIo esIo = mock(EditStreakGenerationIo.class);
		when(esIo.findEditStreakZips()).thenAnswer(new Answer<Set<String>>() {
			@Override
			public Set<String> answer(InvocationOnMock invocation) throws Throwable {
				return in.keySet();
			}
		});
		when(esIo.readEditStreaks(anyString())).thenAnswer(new Answer<Set<EditStreak>>() {
			@Override
			public Set<EditStreak> answer(InvocationOnMock invocation) throws Throwable {
				String zip = (String) invocation.getArguments()[0];
				return in.get(zip);
			}
		});
		return esIo;
	}

	private QueryHistoryIo mockQueryHistoryIo() {
		QueryHistoryIo io = mock(QueryHistoryIo.class);
		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				String zip = (String) invocation.getArguments()[1];
				out.put(zip, usageCaptor.getValue());
				return null;
			}
		}).when(io).storeQueryHistories(usageCaptor.capture(), anyString());
		return io;
	}

	@Test
	public void historyCollectorOutputIsStored() {
		in.put("a.zip", streaks());
		in.put("b.zip", streaks());

		sut.run();

		assertEquals(Sets.newHashSet(), out.get("a.zip"));
		assertEquals(Sets.newHashSet(), out.get("b.zip"));
	}

	@Test
	public void noProblemWithMultipleZipsAndStreaks() {
		in.put("a.zip", streaks(streak(1), streak(2)));
		in.put("b.zip", streaks(streak(3)));

		sut.run();

		Set<List<Usage>> expectedA = Sets.newLinkedHashSet();
		// streak(1) is filtered -- single item
		expectedA.add(Lists.newArrayList(usage(2, 0), new NoUsage()));
		expectedA.add(Lists.newArrayList(new NoUsage(), usage(2, 1)));

		Set<List<Usage>> expectedB = Sets.newLinkedHashSet();
		expectedB.add(Lists.newArrayList(usage(3, 0), new NoUsage()));
		expectedB.add(Lists.newArrayList(new NoUsage(), usage(3, 1), new NoUsage()));
		expectedB.add(Lists.newArrayList(new NoUsage(), usage(3, 2)));

		Set<List<Usage>> actualA = out.get("a.zip");
		assertEquals(expectedA, actualA);
		Collection<List<Usage>> actualB = out.get("b.zip");
		assertEquals(expectedB, actualB);
	}

	@Test
	public void selectionsAreMerged() {
		Context ctx = mock(Context.class);

		EditStreak e = new EditStreak();
		e.add(Snapshot.create(ZonedDateTime.now(), ctx, Names.newMethod("[T,P] [T,P].sel()")));
		in.put("a.zip", streaks(e));

		Usage usage = new Query();
		Query usageMerged = new Query();
		usageMerged.addCallSite(CallSites.createReceiverCallSite("LT.sel()LT;"));
		IAnalysisResult res = new IAnalysisResult() {
			@Override
			public List<Usage> getUsages() {
				return Lists.newArrayList(usage);
			}

			@Override
			public Usage getFirstQuery() {
				return usage;
			}
		};
		when(usageExtractor.analyse(ctx)).thenReturn(res);

		sut.run();

		Collection<List<Usage>> actuals = out.get("a.zip");

		List<Usage> usages = Lists.newArrayList(usage, usageMerged);
		Set<List<Usage>> expecteds = Sets.newLinkedHashSet();
		expecteds.add(usages);

		assertEquals(expecteds, actuals);
	}

	private Tuple<ITypeName, IMethodName> key(String t, String m) {
		return Tuple.newTuple(Names.newType(t), Names.newMethod(m));
	}

	private static Set<EditStreak> streaks(EditStreak... streakArr) {
		Set<EditStreak> streaks = Sets.newLinkedHashSet();
		for (EditStreak es : streakArr) {
			streaks.add(es);
		}
		return streaks;
	}

	private static EditStreak streak(int num) {
		EditStreak e = new EditStreak();
		for (int i = 0; i < num; i++) {
			ZonedDateTime date = ZonedDateTime.now().plusSeconds(num);
			Context context = context(num, i);
			IMethodName sel = Names.newMethod("[T,P] [T,P] m" + num + "()");
			Snapshot snapshot = Snapshot.create(date, context, sel);
			e.add(snapshot);
		}
		return e;
	}

	private static Context context(int num, int i) {
		FieldDeclaration fd1 = new FieldDeclaration();
		fd1.setName(Names.newField("[T,P] [T,P].num" + num));

		FieldDeclaration fd2 = new FieldDeclaration();
		fd2.setName(Names.newField("[T,P] [T,P].i" + i));

		Context ctx = new Context();
		ctx.getSST().getFields().add(fd1);
		ctx.getSST().getFields().add(fd2);

		return ctx;
	}
}