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
package exec.csharp.evaluation.impl;

import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

import cc.kave.rsse.calls.usages.Query;
import exec.csharp.evaluation.IEvaluation;
import exec.csharp.queries.QueryMode;

public class AnalysisOfNoiseTest extends AbstractEvalTest {

	private AnalysisOfNoise sut;

	@Before
	public void setup() {
		sut = new AnalysisOfNoise(mock(IEvaluation.class));
		sut.run();
	}

	@Test
	public void shouldWorkWithoutValues() {
		sut.finish();
	}

	@Test
	public void addingOneValue() {
		Query start = createQuery(1);
		Query end = createQuery(1, 2);
		sut.addResult(start, end, QueryMode.LINEAR, 0.123);
		sut.finish();
	}

	@Test
	public void addingDifferentValues() {
		Query s1 = createQuery(1);
		Query e1 = createQuery(1, 2);
		sut.addResult(s1, e1, QueryMode.LINEAR, 0.123);

		Query s2 = createQuery(1, 2);
		Query e2 = createQuery(1, 3);
		sut.addResult(s2, e2, QueryMode.LINEAR, 0.234);

		sut.finish();
	}

	@Test
	public void countingWorks() {
		Query s1 = createQuery(1);
		Query e1 = createQuery(1, 2);
		sut.addResult(s1, e1, QueryMode.LINEAR, 0.1);

		Query s2 = createQuery(1);
		Query e2 = createQuery(1, 3);
		sut.addResult(s2, e2, QueryMode.LINEAR, 0.3);

		Query s3 = createQuery(1, 2);
		Query e3 = createQuery(1, 3);
		sut.addResult(s3, e3, QueryMode.LINEAR, 0.234);

		sut.finish();
	}

	@Test
	public void pureRemovalsAreTracked() {
		sut.skipCommit_NoAddition(QueryMode.LINEAR);
		sut.finish();
	}
}