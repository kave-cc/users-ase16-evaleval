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
package exec.csharp.queries;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

import cc.kave.commons.exceptions.AssertionException;
import cc.kave.rsse.calls.usages.Query;
import cc.kave.rsse.calls.usages.Usage;

public class QueryBuilderFactoryTest {

	private QueryBuilderFactory sut;

	@Before
	public void setup() {
		LinearQueryBuilder linear = mock(LinearQueryBuilder.class);
		RandomQueryBuilder random = mock(RandomQueryBuilder.class);
		RealQueryBuilder real = mock(RealQueryBuilder.class);
		RealWithNoiseQueryBuilder realWithNoise = mock(RealWithNoiseQueryBuilder.class);
		sut = new QueryBuilderFactory(linear, random, real, realWithNoise);
	}

	@Test
	public void linear() {
		IQueryBuilder<Usage, Query> actual = sut.get(QueryMode.LINEAR);
		assertTrue(actual instanceof LinearQueryBuilder);
	}

	@Test
	public void random() {
		IQueryBuilder<Usage, Query> actual = sut.get(QueryMode.RANDOM);
		assertTrue(actual instanceof RandomQueryBuilder);
	}

	@Test
	public void real() {
		IQueryBuilder<Usage, Query> actual = sut.get(QueryMode.REAL_ISH);
		assertTrue(actual instanceof RealQueryBuilder);
	}

	@Test
	public void realWithNoise() {
		IQueryBuilder<Usage, Query> actual = sut.get(QueryMode.REAL);
		assertTrue(actual instanceof RealWithNoiseQueryBuilder);
	}

	@Test(expected = AssertionException.class)
	public void nullCase() {
		sut.get(null);
	}
}