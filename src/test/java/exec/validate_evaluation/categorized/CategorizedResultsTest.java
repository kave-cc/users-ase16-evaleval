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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Sets;

import cc.kave.commons.evaluation.Boxplot;
import cc.kave.commons.exceptions.AssertionException;
import cc.kave.commons.utils.LocaleUtils;

public class CategorizedResultsTest {
	private CategorizedResults<String> sut;

	@Before
	public void setup() {
		LocaleUtils.setDefaultLocale();
		sut = CategorizedResults.create();
	}

	@Test
	public void defaultValues() {
		Set<String> expected = sut.getCategories();
		Set<String> actual = Sets.newHashSet();
		assertEquals(expected, actual);
	}

	@Test(expected = AssertionException.class)
	public void accessingNonExistingValues() {
		sut.get("x");
	}

	@Test
	public void valuesCanBeAdded() {
		sut.add("a", 0.1);

		Set<String> expected = sut.getCategories();
		Set<String> actual = Sets.newHashSet("a");
		assertEquals(expected, actual);

		Boxplot actualBp = sut.get("a");
		Boxplot expectedBp = new Boxplot(1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1);
		assertEquals(expectedBp, actualBp);
	}

	@Test
	public void multipleValuesCanBeAdded() {
		sut.add("a", 0.1);
		sut.add("b", 0.2);
		sut.add("c", 0.3);
		sut.add("c", 0.5);

		Set<String> expected = sut.getCategories();
		Set<String> actual = Sets.newHashSet("a", "b", "c");
		assertEquals(expected, actual);

		Boxplot actualBp1 = sut.get("a");
		Boxplot expectedBp1 = new Boxplot(1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1);
		assertEquals(expectedBp1, actualBp1);

		Boxplot actualBp2 = sut.get("b");
		Boxplot expectedBp2 = new Boxplot(1, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2);
		assertEquals(expectedBp2, actualBp2);

		Boxplot actualBp3 = sut.get("c");
		Boxplot expectedBp3 = new Boxplot(2, 0.4, 0.3, 0.3, 0.4, 0.5, 0.5);
		assertEquals(expectedBp3, actualBp3);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void merging() {
		CategorizedResults<String> a = CategorizedResults.create();
		a.add("1", 0.1);
		CategorizedResults<String> b = CategorizedResults.create();
		b.add("1", 0.2);
		b.add("1", 0.4);
		b.add("2", 0.5);
		CategorizedResults<String> actual = CategorizedResults.merge(Sets.newHashSet(a, b));
		CategorizedResults<String> expected = CategorizedResults.create();
		expected.add("1", 0.1);
		expected.add("1", 0.3);
		expected.add("2", 0.5);
		assertEquals(expected, actual);
	}

	@Test
	public void meaningfulToString() {
		sut.add("1", 0.2);
		sut.add("1", 0.4);
		sut.add("2", 0.5);

		String actual = sut.toString();
		assertTrue(actual.contains("1:[2 values (avg: 0.300) - 0.20; 0.20; 0.30; 0.40; 0.40]"));
		assertTrue(actual.contains("2:[1 values (avg: 0.500) - 0.50; 0.50; 0.50; 0.50; 0.50]"));
	}

	@Test
	public void equality_default() {
		CategorizedResults<String> a = new CategorizedResults<String>();
		CategorizedResults<String> b = new CategorizedResults<String>();
		assertEquals(a, b);
		assertEquals(a.hashCode(), b.hashCode());
	}

	@Test
	public void equality_afterAddingValues() {
		CategorizedResults<String> a = new CategorizedResults<String>();
		a.add("a", 0.1);
		CategorizedResults<String> b = new CategorizedResults<String>();
		b.add("a", 0.1);
		assertEquals(a, b);
		assertEquals(a.hashCode(), b.hashCode());
	}

	@Test
	public void equality_differentValues() {
		CategorizedResults<String> a = new CategorizedResults<String>();
		a.add("a", 0.1);
		CategorizedResults<String> b = new CategorizedResults<String>();
		assertNotEquals(a, b);
		assertNotEquals(a.hashCode(), b.hashCode());
	}
}