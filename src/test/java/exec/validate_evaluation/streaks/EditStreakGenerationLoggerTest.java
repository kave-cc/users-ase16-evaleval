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

import static org.mockito.Mockito.mock;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Sets;

import cc.kave.commons.model.events.completionevents.ICompletionEvent;
import exec.validate_evaluation.streaks.EditStreakGenerationRunner.IRemovalFilter;

public class EditStreakGenerationLoggerTest {

	private EditStreakGenerationLogger sut;

	@Before
	public void setup() {
		sut = new EditStreakGenerationLogger();
	}

	@Test
	public void integration() {
		Set<IRemovalFilter> filters = Sets.newHashSet();
		filters.add(mock(IRemovalFilter.class));
		filters.add(mock(IRemovalFilter.class));
		sut.starting(filters);

		sut.foundZips(Sets.newHashSet("a.zip", "b/c.zip"));

		processZip();
		processZip();

		sut.finish();
	}

	private void processZip() {
		sut.startingZip("x.zip");

		Set<ICompletionEvent> events = Sets.newHashSet(mock(ICompletionEvent.class), mock(ICompletionEvent.class),
				mock(ICompletionEvent.class));
		sut.foundEvents(events);

		sut.processingEvent(mock(ICompletionEvent.class));
		sut.processingEvent(mock(ICompletionEvent.class));

		sut.startingRemovalFiltering();
		sut.removedEditStreak();
		sut.removedEditStreak();
		sut.removedEditStreak();

		Set<EditStreak> streaks = Sets.newLinkedHashSet();
		streaks.add(mock(EditStreak.class));
		streaks.add(mock(EditStreak.class));
		streaks.add(mock(EditStreak.class));

		sut.endZip(streaks);
	}
}