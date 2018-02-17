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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.time.ZonedDateTime;

import org.junit.Test;

import com.google.common.collect.Lists;

import cc.kave.commons.model.events.completionevents.Context;

public class EditStreakTest {

	@Test
	public void defaultValues() {
		EditStreak sut = new EditStreak();

		assertTrue(sut.isEmptyOrSingleEdit());
		assertEquals(Lists.newArrayList(), sut.getSnapshots());
	}

	@Test
	public void snapshotsCanBeAdded() {
		EditStreak sut = new EditStreak();
		Snapshot s = Snapshot.create(ZonedDateTime.now(), new Context(), null);
		sut.add(s);

		assertTrue(sut.isEmptyOrSingleEdit());
		assertEquals(Lists.newArrayList(s), sut.getSnapshots());
	}

	@Test
	public void multipleSnapshotsCanBeAdded() {
		EditStreak sut = new EditStreak();
		sut.add(Snapshot.create(ZonedDateTime.now(), new Context(), null));
		sut.add(Snapshot.create(ZonedDateTime.now(), new Context(), null));

		assertFalse(sut.isEmptyOrSingleEdit());
	}

	@Test
	public void equality_default() {
		EditStreak a = new EditStreak();
		EditStreak b = new EditStreak();
		assertEquals(a, b);
		assertEquals(a.hashCode(), b.hashCode());
	}

	@Test
	public void equality_reallyTheSame() {
		Snapshot s = Snapshot.create(ZonedDateTime.now(), new Context(), null);

		EditStreak a = new EditStreak();
		a.add(s);
		EditStreak b = new EditStreak();
		b.add(s);

		assertEquals(a, b);
		assertEquals(a.hashCode(), b.hashCode());
	}

	@Test
	public void equality_different() {
		EditStreak a = new EditStreak();
		a.add(Snapshot.create(ZonedDateTime.now(), new Context(), null));
		EditStreak b = new EditStreak();

		assertNotEquals(a, b);
		assertNotEquals(a.hashCode(), b.hashCode());
	}
}