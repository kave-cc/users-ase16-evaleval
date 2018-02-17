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

import static cc.kave.commons.assertions.Asserts.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Iterator;
import java.util.Map;

import org.junit.Test;

import com.google.common.collect.Maps;

public class MapSorterTest {

	@Test
	public void sort() {
		Map<String, String> in = Maps.newLinkedHashMap();
		in.put("c", "x");
		in.put("b", "w");
		in.put("d", "y");
		in.put("a", "z");

		Map<String, String> actual = MapSorter.sort(in);

		Map<String, String> expected = Maps.newLinkedHashMap();
		expected.put("b", "w");
		expected.put("c", "x");
		expected.put("d", "y");
		expected.put("a", "z");

		assertEqualOrder(expected, actual);
	}

	@Test
	public void sortByCount() {
		Map<String, Integer> in = Maps.newLinkedHashMap();
		in.put("c", 2);
		in.put("b", 5);
		in.put("d", 2);
		in.put("a", 10);

		Map<String, Integer> actual = MapSorter.sortByCount(in);

		Map<String, Integer> expected = Maps.newLinkedHashMap();
		expected.put("a", 10);
		expected.put("b", 5);
		expected.put("c", 2);
		expected.put("d", 2);

		assertEqualOrder(expected, actual);
	}

	private <K, V> void assertEqualOrder(Map<K, V> expected, Map<K, V> actual) {
		Iterator<K> itE = expected.keySet().iterator();
		Iterator<K> itA = actual.keySet().iterator();

		while (itE.hasNext()) {
			assertTrue(itA.hasNext());
			K eKey = itE.next();
			K aKey = itA.next();
			assertEquals(eKey, aKey);

			V eVal = expected.get(eKey);
			V aVal = actual.get(aKey);
			assertEquals(eVal, aVal);
		}
		assertFalse(itA.hasNext());
	}
}