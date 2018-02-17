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
import static org.mockito.Mockito.mock;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import cc.kave.commons.exceptions.AssertionException;
import cc.kave.commons.model.events.completionevents.Context;
import cc.kave.commons.model.naming.codeelements.IMethodName;
import exec.validate_evaluation.utils.DateUtils;

public class SnapshotTest {

	private ZonedDateTime d;
	private Context q;
	private IMethodName m;

	@Before
	public void setup() {
		d = ZonedDateTime.now();
		q = mock(Context.class);
		m = mock(IMethodName.class);
	}

	@Test
	public void customInit() {
		Snapshot sut = Snapshot.create(d, q, m);
		assertEquals(d, sut.getDate());
		assertEquals(q, sut.getContext());
		assertEquals(m, sut.getSelection());
	}

	@Test(expected = AssertionException.class)
	public void customInit_nullDate() {
		d = null;
		Snapshot.create(d, q, m);
	}

	@Test(expected = AssertionException.class)
	public void customInit_nullQuery() {
		q = null;
		Snapshot.create(d, q, m);
	}

	@Test
	public void hasSelection() {
		Snapshot sut = Snapshot.create(d, q, m);
		assertTrue(sut.hasSelection());
	}

	@Test
	public void hasNoSelection() {
		Snapshot sut = Snapshot.create(d, q, null);
		assertFalse(sut.hasSelection());
	}

	@Test
	public void customInit_nullSelectionIsOk() {
		m = null;
		Snapshot.create(d, q, m);
	}

	@Test
	public void equality() {
		Snapshot a = Snapshot.create(d, q, m);
		Snapshot b = Snapshot.create(d, q, m);

		assertEquals(a, b);
		assertEquals(a.hashCode(), b.hashCode());
	}

	@Test
	public void equality_diffDate() {
		ZonedDateTime d2 = d.plusSeconds(1);

		Snapshot a = Snapshot.create(d, q, m);
		Snapshot b = Snapshot.create(d2, q, m);

		assertNotEquals(a, b);
		assertNotEquals(a.hashCode(), b.hashCode());
	}

	@Test
	public void equality_diffQuery() {
		Context q2 = mock(Context.class);

		Snapshot a = Snapshot.create(d, q, m);
		Snapshot b = Snapshot.create(d, q2, m);

		assertNotEquals(a, b);
		assertNotEquals(a.hashCode(), b.hashCode());
	}

	@Test
	public void equality_diffSelection() {
		IMethodName m2 = mock(IMethodName.class);

		Snapshot a = Snapshot.create(d, q, m);
		Snapshot b = Snapshot.create(d, q, m2);

		assertNotEquals(a, b);
		assertNotEquals(a.hashCode(), b.hashCode());
	}

	private static Date date(int deltaSeconds) {
		LocalDateTime ldt = LocalDateTime.now().plusSeconds(deltaSeconds);
		return DateUtils.fromLDT(ldt);
	}
}