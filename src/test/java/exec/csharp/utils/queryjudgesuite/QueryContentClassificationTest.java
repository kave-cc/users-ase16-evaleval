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

import exec.csharp.evaluation.impl.QueryContent;

public class QueryContentClassificationTest extends BaseTest {

	protected void assertQueryContent(QueryContent expected) {
		QueryContent actual = judge().getQueryContentCategorization();
		assertEquals(expected, actual);
	}

	/* ****** QQ ****** */

	// 0+1[-y][~D] -> [~]0|1

	@Test
	public void qq1() {
		setA('a');
		setB('a', 1);
		assertQueryContent(QueryContent.ZERO_ONE);
	}

	@Test
	public void qq1_minusY() {
		setA('a', 1);
		setB('a', 2);
		assertQueryContent(QueryContent.ZERO_ONE_DEF);
	}

	@Test
	public void qq1_deltaDef() {
		setA('a');
		setB('b', 1);
		assertQueryContent(QueryContent.ZERO_ONE_DEF);
	}

	// 1+1[-y][~D] -> [~]1|2

	@Test
	public void qq2() {
		setA('a', 1);
		setB('a', 1, 2);
		assertQueryContent(QueryContent.ONE_TWO);
	}

	@Test
	public void qq2_minusY() {
		setA('a', 1, 9);
		setB('a', 1, 2);
		assertQueryContent(QueryContent.ONE_TWO_DEF);
	}

	@Test
	public void qq2_deltaDef() {
		setA('a', 1);
		setB('b', 1, 2);
		assertQueryContent(QueryContent.ONE_TWO_DEF);
	}

	// 0+x[-y][~D] -> [~]0|M

	@Test
	public void qq3() {
		setA('a');
		setB('a', 1, 2);
		assertQueryContent(QueryContent.ZERO);
	}

	@Test
	public void qq3_minusY() {
		setA('a', 9);
		setB('a', 1, 2);
		assertQueryContent(QueryContent.ZERO_DEF);
	}

	@Test
	public void qq3_deltaDef() {
		setA('a');
		setB('b', 1, 2);
		assertQueryContent(QueryContent.ZERO_DEF);
	}

	// 0[~D] -> SKIPPED

	@Test
	public void qq4_deltaDef() {
		setA('a');
		setB('b');
		assertQueryContent(QueryContent.SKIPPED);
	}

	// N+1[-y][~D] -> [~]M-1|M

	@Test
	public void qq5() {
		setA('a', 1, 2);
		setB('a', 1, 2, 3);
		assertQueryContent(QueryContent.MINUS1);
	}

	@Test
	public void qq5_minusY() {
		setA('a', 1, 2, 9);
		setB('a', 1, 2, 3);
		assertQueryContent(QueryContent.MINUS1_DEF);
	}

	@Test
	public void qq5_deltaDef() {
		setA('a', 1, 2);
		setB('b', 1, 2, 3);
		assertQueryContent(QueryContent.MINUS1_DEF);
	}

	// N+x[-y][~D] -> [~]N|M

	@Test
	public void qq6() {
		setA('a', 1);
		setB('a', 1, 2, 3);
		assertQueryContent(QueryContent.NM);
	}

	@Test
	public void qq6_minusY() {
		setA('a', 1, 9);
		setB('a', 1, 2, 3);
		assertQueryContent(QueryContent.NM_DEF);
	}

	@Test
	public void qq6_deltaDef() {
		setA('a', 1);
		setB('b', 1, 2, 3);
		assertQueryContent(QueryContent.NM_DEF);
	}

	// N-y[~D] -> PURE_REMOVAL

	@Test
	public void qq7() {
		setA('a', 1, 9);
		setB('a', 1);
		assertQueryContent(QueryContent.PURE_REMOVAL);
	}

	@Test
	public void qq7_deltaDef() {
		setA('a', 1, 9);
		setB('b', 1);
		assertQueryContent(QueryContent.PURE_REMOVAL);
	}

	// N[~D] -> SKIPPED

	@Test
	public void qq8() {
		setA('a', 1);
		setB('a', 1);
		assertQueryContent(QueryContent.SKIPPED);
	}

	@Test
	public void qq8_deltaDef() {
		setA('a', 1);
		setB('b', 1);
		assertQueryContent(QueryContent.SKIPPED);
	}

	/* ****** NQ (~D is implied, -x is impossible) ****** */

	// ~[+x] -> FROM_SCRATCH

	@Test
	public void nq1() {
		nnA();
		setB('a', 1);
		assertQueryContent(QueryContent.FROM_SCRATCH);
	}

	// ~[0] -> SKIPPED

	@Test
	public void nq2() {
		nnA();
		setB('a');
		assertQueryContent(QueryContent.SKIPPED);
	}

	/* ****** QN (~D is implied, +x is impossible) ****** */

	// ~[-x] -> PURE_REMOVAL

	@Test
	public void qn1() {
		setA('a', 1);
		nnB();
		assertQueryContent(QueryContent.PURE_REMOVAL);
	}

	// ~[0] -> SKIPPED

	@Test
	public void qn2() {
		setA('a');
		nnB();
		assertQueryContent(QueryContent.SKIPPED);
	}

	/* ****** NN (this should always be SKIPPED) ****** */

	@Test
	public void nn() {
		nnA();
		nnB();
		assertQueryContent(QueryContent.SKIPPED);
	}

}