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

public class F1DetailsTest extends AbstractEvalTest {

	private F1Details sut;

	@Before
	public void setup() {
		sut = new F1Details(mock(IEvaluation.class));
		sut.run();
	}

	@Test
	public void noCrashEvenIFDataIsMissing() {
		sut.finish();
	}

	@Test
	public void printingWorksIfDataIsThere() {

		Query start = createQuery(1, 2, 3);
		Query end = createQuery(1, 2, 3, 4);

		sut.addResult(start, end, QueryMode.LINEAR, 0.123);
		sut.finish();
	}

	@Test
	public void biggerExample() {

		Query start = createQuery();
		Query end = createQuery(1);

		sut.addResult(start, end, QueryMode.LINEAR, 0.123);
		sut.addResult(start, end, QueryMode.LINEAR, 0.234);

		sut.finish();
	}

	@Test
	public void resultsAreOnlyCounterForLinear() {

		Query start = createQuery();
		Query end = createQuery(1);

		sut.addResult(start, end, QueryMode.LINEAR, 0.123);
		sut.addResult(start, end, QueryMode.RANDOM, 0.234);

		sut.finish();
	}
}