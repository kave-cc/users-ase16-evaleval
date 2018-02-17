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

import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

public class CombinationIteratorTest {

	@Test
	public void happyPath() {
		List<List<Integer>> actuals = get(2, 1, 2, 3);
		assertEquals(3, actuals.size());
		assertTrue(actuals.contains(Lists.newArrayList(1, 2)));
		assertTrue(actuals.contains(Lists.newArrayList(2, 3)));
		assertTrue(actuals.contains(Lists.newArrayList(1, 3)));

	}

	public List<List<Integer>> get(int choose, int... ins) {
		List<Integer> items = Lists.newLinkedList();
		for (int i : ins) {
			items.add(i);
		}
		CombinationIterator<Integer> iterator = new CombinationIterator<Integer>(items, choose);
		List<List<Integer>> actuals = Lists.newLinkedList();
		for (List<Integer> is : iterator) {
			actuals.add(is);
		}
		return actuals;
	}
}