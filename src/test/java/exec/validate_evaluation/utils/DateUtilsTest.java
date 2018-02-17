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
package exec.validate_evaluation.utils;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

public class DateUtilsTest {

	@Test
	public void toLDT() {
		Date d = date(1987, 6, 5, 12, 34, 56);
		LocalDateTime actual = DateUtils.toLDT(d);
		LocalDateTime expected = localDateTime(1987, 6, 5, 12, 34, 56);
		assertEquals(expected, actual);
	}

	@Test
	public void fromLDT() {
		LocalDateTime d = localDateTime(1987, 6, 5, 12, 34, 56);
		Date actual = DateUtils.fromLDT(d);
		Date expected = date(1987, 6, 5, 12, 34, 56);
		assertEquals(expected, actual);
	}

	private LocalDateTime localDateTime(int year, int month, int dayOfMonth, int hour, int minute, int second) {
		return LocalDateTime.of(year, month, dayOfMonth, hour, minute, second);
	}

	private Date date(int year, int month, int dayOfMonth, int hour, int minute, int second) {
		Calendar c = Calendar.getInstance();
		c.clear(); // necessary to reset ms to 0
		c.set(year, month - 1, dayOfMonth, hour, minute, second);
		return c.getTime();
	}
}