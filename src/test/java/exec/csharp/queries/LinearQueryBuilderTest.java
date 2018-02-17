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

import org.junit.Test;

import cc.kave.rsse.calls.usages.Query;
import cc.kave.rsse.calls.usages.Usage;

public class LinearQueryBuilderTest extends AbstractQueryBuilderTest {

	@Override
	protected IQueryBuilder<Usage, Query> createQueryBuilder() {
		return new LinearQueryBuilder();
	}

	@Test
	public void simpleCase() {
		Usage start = new UsageThatShouldBeIgnored("a");
		Usage end = createUsage("a", "b");
		Query q = assertSingleQuery(start, end);
		assertMethods(q, "a");
	}

	@Test
	public void differentOrder() {
		Usage start = new UsageThatShouldBeIgnored("a");
		Usage end = createUsage("b", "a");
		Query q = assertSingleQuery(start, end);
		assertMethods(q, "b");
	}

	@Test
	public void differentNumber() {
		Usage start = new UsageThatShouldBeIgnored("a", "b");
		Usage end = createUsage("a", "b", "c", "d", "e");
		Query q = assertSingleQuery(start, end);
		assertMethods(q, "a", "b");
	}

	@Test
	public void withNoise() {
		Usage start = new UsageThatShouldBeIgnored("a", "b");
		Usage end = createUsage("a", "c");
		Query q = assertSingleQuery(start, end);
		assertMethods(q, "a");
	}
}