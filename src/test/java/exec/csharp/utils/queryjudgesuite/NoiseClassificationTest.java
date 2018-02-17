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
package exec.csharp.utils.queryjudgesuite;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import exec.csharp.utils.NoiseMode;

public class NoiseClassificationTest extends BaseTest {

	private void assertNoiseMode(NoiseMode expected) {
		NoiseMode actual = judge().getNoiseMode();
		assertEquals(expected, actual);
	}

	/* ****** QQ ****** */

	// n+x[~D]

	@Test
	public void qq1() {
		setA('a', 1);
		setB('a', 1, 2);
		assertNoiseMode(NoiseMode.NO_NOISE);
	}

	@Test
	public void qq1_deltaD() {
		setA('a', 1);
		setB('b', 1, 2);
		assertNoiseMode(NoiseMode.DEF);
	}

	// n+x-y[~D]

	@Test
	public void qq2() {
		setA('a', 1, 9);
		setB('a', 1, 2);
		assertNoiseMode(NoiseMode.REMOVAL);
	}

	@Test
	public void qq2_deltaD() {
		setA('a', 1, 9);
		setB('b', 1, 2);
		assertNoiseMode(NoiseMode.DEF_AND_REMOVAL);
	}

	// n-y[~D]

	@Test
	public void qq3() {
		setA('a', 1, 9);
		setB('a', 1);
		assertNoiseMode(NoiseMode.PURE_REMOVAL);
	}

	@Test
	public void qq3_deltaD() {
		setA('a', 1, 9);
		setB('b', 1);
		assertNoiseMode(NoiseMode.PURE_REMOVAL);
	}

	// n[~d] -> SKIPPED

	@Test
	public void qq4() {
		setA('a', 1);
		setB('a', 1);
		assertNoiseMode(NoiseMode.SKIPPED);
	}

	@Test
	public void qq4_deltaD() {
		setA('a', 1);
		setB('b', 1);
		assertNoiseMode(NoiseMode.SKIPPED);
	}

	/* ****** NQ (~D is implied, -y is impossible) ****** */

	// ~[+x] -> FROM_SRATCH

	@Test
	public void nq1() {
		nnA();
		setB('a', 1);
		assertNoiseMode(NoiseMode.FROM_SCRATCH);
	}

	// ~[0] -> SKIPPED

	@Test
	public void nq2() {
		nnA();
		setB('a');
		assertNoiseMode(NoiseMode.SKIPPED);
	}

	/* ****** QN (~D is implied, +x is impossible) ****** */

	// ~[-X] -> PURE_REMOVAL

	@Test
	public void qn1() {
		setA('a', 1);
		nnB();
		assertNoiseMode(NoiseMode.PURE_REMOVAL);
	}

	// ~[0] -> SKIPPED

	@Test
	public void qn2() {
		setA('a');
		nnB();
		assertNoiseMode(NoiseMode.SKIPPED);
	}

	/* ****** NN (this should always be SKIPPED) ****** */

	@Test
	public void nn() {
		nnA();
		nnB();
		assertNoiseMode(NoiseMode.SKIPPED);
	}
}