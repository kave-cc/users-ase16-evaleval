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
package exec.validate_evaluation.streaks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import cc.kave.commons.model.events.completionevents.CompletionEvent;
import cc.kave.commons.model.events.completionevents.Context;
import cc.kave.commons.model.events.completionevents.ICompletionEvent;
import cc.kave.commons.model.events.completionevents.Proposal;
import cc.kave.commons.model.events.completionevents.ProposalSelection;
import cc.kave.commons.model.events.completionevents.TerminationState;
import cc.kave.commons.model.naming.IName;
import cc.kave.commons.model.naming.Names;
import cc.kave.commons.model.naming.codeelements.IMethodName;
import cc.kave.commons.model.naming.types.ITypeName;
import cc.kave.commons.model.ssts.impl.SST;
import exec.validate_evaluation.streaks.EditStreakGenerationRunner.EmptyOrSingleEditStreakRemovalFilter;
import exec.validate_evaluation.streaks.EditStreakGenerationRunner.IRemovalFilter;

public class EditStreakGenerationRunnerTest {

	private static final IName anyName = Names.newGeneral("abc");

	private EditStreakGenerationIo io;
	private EditStreakGenerationLogger log;
	private EditStreakGenerationRunner sut;

	private Map<String, Set<ICompletionEvent>> input;
	private Map<String, Set<EditStreak>> output;

	@Before
	public void setup() {
		input = Maps.newLinkedHashMap();
		output = Maps.newLinkedHashMap();

		io = mockIo();

		log = mock(EditStreakGenerationLogger.class);

		sut = new EditStreakGenerationRunner(io, log);
	}

	private EditStreakGenerationIo mockIo() {
		EditStreakGenerationIo io = mock(EditStreakGenerationIo.class);

		when(io.findCompletionEventZips()).then(new Answer<Set<String>>() {
			@Override
			public Set<String> answer(InvocationOnMock invocation) throws Throwable {
				return Sets.newLinkedHashSet(input.keySet());
			}
		});

		when(io.readCompletionEvents(anyString())).then(new Answer<Set<ICompletionEvent>>() {
			@Override
			public Set<ICompletionEvent> answer(InvocationOnMock invocation) throws Throwable {
				String zip = (String) invocation.getArguments()[0];
				return input.get(zip);
			}
		});

		doAnswer(new Answer<Void>() {
			@Override
			@SuppressWarnings("unchecked")
			public Void answer(InvocationOnMock invocation) throws Throwable {
				Set<EditStreak> streaks = (Set<EditStreak>) invocation.getArguments()[0];
				String zip = (String) invocation.getArguments()[1];
				output.put(zip, streaks);
				return null;
			}
		}).when(io).storeEditStreaks(anySetOf(EditStreak.class), anyString());

		when(io.readEditStreaks(anyString())).then(new Answer<Set<EditStreak>>() {
			@Override
			public Set<EditStreak> answer(InvocationOnMock invocation) throws Throwable {
				String zip = (String) invocation.getArguments()[0];
				return Sets.newLinkedHashSet(output.get(zip));
			}
		});

		return io;
	}

	@Test
	public void logging_happyPath() {
		Context ctx1a = context(type(1));
		Context ctx1b = context(type(1));

		ICompletionEvent e1 = apply(date(1), ctx1a, anyMethod(1));
		ICompletionEvent e2 = apply(date(2), ctx1b, anyMethod(2));
		addInput("a.zip", e1, e2);

		sut.run();

		EditStreak es = new EditStreak();
		es.add(Snapshot.create(date(1), ctx1a, anyMethod(1)));
		es.add(Snapshot.create(date(2), ctx1b, anyMethod(2)));

		Set<EditStreak> expectedStreaks = Sets.newHashSet();
		expectedStreaks.add(es);

		verify(log).starting(anySetOf(IRemovalFilter.class));
		verify(log).foundZips(Sets.newHashSet("a.zip"));
		verify(log).startingZip("a.zip");
		verify(log).foundEvents(Sets.newHashSet(e1, e2));
		verify(log, times(2)).processingEvent(any(ICompletionEvent.class));
		verify(log).startingRemovalFiltering();
		verify(log).endZip(eq(expectedStreaks));
		verify(log).finish();

		verifyNoMoreInteractions(log);
	}

	@Test
	public void logging_filters() {
		IRemovalFilter f1 = mock(IRemovalFilter.class);
		IRemovalFilter f2 = mock(IRemovalFilter.class);
		sut.add(f1);
		sut.add(f2);

		sut.run();

		verify(log).starting(Sets.newHashSet(f1, f2));
	}

	@Test
	public void logging_snapshotIsFiltered() {
		Context ctx1 = context(type(1));
		ICompletionEvent e1 = apply(date(1), ctx1, anyMethod(1));
		addInput("a.zip", e1);

		EditStreak es = new EditStreak();
		es.add(Snapshot.create(date(1), ctx1, anyMethod(1)));

		sut.add(e -> e.equals(es));
		sut.run();

		verify(log).starting(anySetOf(IRemovalFilter.class));
		verify(log).foundZips(Sets.newHashSet("a.zip"));
		verify(log).startingZip("a.zip");
		verify(log).foundEvents(Sets.newHashSet(e1));
		verify(log).processingEvent(any(ICompletionEvent.class));
		verify(log).startingRemovalFiltering();
		verify(log).removedEditStreak();
		verify(log).endZip(eq(Sets.newHashSet()));
		verify(log).finish();

		verifyNoMoreInteractions(log);
	}

	@Test
	public void happyPath() {
		Context ctx1a = context(type(1));
		Context ctx1b = context(type(1));

		ICompletionEvent e1 = apply(date(1), ctx1a, anyMethod(1));
		ICompletionEvent e2 = apply(date(2), ctx1b, anyMethod(2));
		addInput("a.zip", e1, e2);

		sut.run();

		EditStreak es = new EditStreak();
		es.add(Snapshot.create(date(1), ctx1a, anyMethod(1)));
		es.add(Snapshot.create(date(2), ctx1b, anyMethod(2)));

		assertNumFiles(1);
		assertFile("a.zip", es);
	}

	@Test
	public void filteringEditStreak() {
		Context ctx1a = context(type(1));
		Context ctx1b = context(type(1));

		ICompletionEvent e1 = apply(date(1), ctx1a, anyMethod(1));
		ICompletionEvent e2 = apply(date(2), ctx1b, anyMethod(2));
		addInput("a.zip", e1, e2);

		EditStreak es = new EditStreak();
		es.add(Snapshot.create(date(1), ctx1a, anyMethod(1)));
		es.add(Snapshot.create(date(2), ctx1b, anyMethod(2)));

		sut.add(e -> false); // filter that never hits
		sut.add(e -> e.equals(es));
		sut.run();

		assertNumFiles(1);
		assertFile("a.zip");
	}

	@Test
	public void applicationOfNonMethod() {

		Context ctx1 = context(type(1));
		ICompletionEvent e1 = apply(date(1), ctx1, anyName);
		addInput("a.zip", e1);

		EditStreak es = new EditStreak();
		es.add(Snapshot.create(date(1), ctx1, null));

		sut.run();

		assertNumFiles(1);
		assertFile("a.zip", es);
	}

	@Test
	public void abortedCompletion() {

		Context ctx1 = context(type(1));
		ICompletionEvent e1 = abort(date(1), ctx1, anyName);
		addInput("a.zip", e1);

		EditStreak es = new EditStreak();
		es.add(Snapshot.create(date(1), ctx1, null));

		sut.run();

		assertNumFiles(1);
		assertFile("a.zip", es);
	}

	@Test
	public void noSnapshotsInUnknownType() {

		Context ctx1 = context(Names.getUnknownType());
		ICompletionEvent e1 = abort(date(1), ctx1, anyName);
		addInput("a.zip", e1);

		sut.run();

		assertNumFiles(1);
		assertFile("a.zip");
	}

	@Test
	public void defaultFilterWorks() {
		EmptyOrSingleEditStreakRemovalFilter sut = new EmptyOrSingleEditStreakRemovalFilter();

		EditStreak es = mock(EditStreak.class);

		when(es.isEmptyOrSingleEdit()).thenReturn(false);
		assertFalse(sut.apply(es));

		when(es.isEmptyOrSingleEdit()).thenReturn(true);
		assertTrue(sut.apply(es));
	}

	private void assertFile(String zip, EditStreak... es) {
		Set<EditStreak> actuals = output.get(zip);
		Set<EditStreak> expecteds = Sets.newHashSet(es);
		assertEquals(expecteds, actuals);
	}

	private void assertNumFiles(int numFiles) {
		assertEquals(numFiles, output.keySet().size());
	}

	private static IMethodName anyMethod(int i) {
		return Names.newMethod("[T,P] [T,P].m" + i + "()");
	}

	private Context context(ITypeName encType) {
		SST sst = new SST();
		sst.setEnclosingType(encType);
		Context ctx = new Context();
		ctx.setSST(sst);
		return ctx;
	}

	private void addInput(String string, ICompletionEvent... eventArr) {
		Set<ICompletionEvent> events = Sets.newLinkedHashSet();
		for (ICompletionEvent event : eventArr) {
			events.add(event);
		}
		input.put(string, events);
	}

	private static ICompletionEvent apply(ZonedDateTime triggeredAt, Context context, IName name) {
		return completionEventWithSelection(triggeredAt, TerminationState.Applied, name, context);
	}

	private static ICompletionEvent abort(ZonedDateTime triggeredAt, Context context, IName name) {
		return completionEventWithSelection(triggeredAt, TerminationState.Cancelled, name, context);
	}

	private static ICompletionEvent completionEventWithSelection(ZonedDateTime triggeredAt, TerminationState state,
			IName name, Context context) {
		CompletionEvent e = completionEvent(context);

		e.TriggeredAt = triggeredAt;
		e.terminatedState = state;

		Proposal p = new Proposal();
		p.Name = name;
		e.proposalCollection.add(p);

		ProposalSelection ps = new ProposalSelection();
		ps.Proposal = p;
		e.selections.add(ps);

		return e;
	}

	private static CompletionEvent completionEvent(Context context) {
		CompletionEvent e = new CompletionEvent();
		e.TriggeredAt = ZonedDateTime.now();

		e.terminatedState = TerminationState.Unknown;
		e.context = context;

		return e;
	}

	private ITypeName type(int i) {
		return Names.newType("T%d, P", i);
	}

	private static ZonedDateTime date(int deltaSecs) {
		LocalDateTime ldt = LocalDateTime.of(2000, 1, 1, 0, 0, 0, 0).plusSeconds(deltaSecs);
		return ZonedDateTime.of(ldt, ZoneId.of("ECT", ZoneId.SHORT_IDS));
	}
}