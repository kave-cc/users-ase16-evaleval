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

import com.google.inject.Inject;

import cc.kave.commons.assertions.Asserts;
import cc.kave.rsse.calls.usages.Query;
import cc.kave.rsse.calls.usages.Usage;

public class QueryBuilderFactory {

	private final LinearQueryBuilder linear;
	private final RandomQueryBuilder random;
	private final RealQueryBuilder real;
	private final RealWithNoiseQueryBuilder realWithNoise;

	@Inject
	public QueryBuilderFactory(LinearQueryBuilder linear, RandomQueryBuilder random, RealQueryBuilder real,
			RealWithNoiseQueryBuilder realWithNoise) {
		this.linear = linear;
		this.random = random;
		this.real = real;
		this.realWithNoise = realWithNoise;
	}

	public IQueryBuilder<Usage, Query> get(QueryMode mode) {
		Asserts.assertNotNull(mode);
		switch (mode) {
		case LINEAR:
			return linear;
		case RANDOM:
			return random;
		case REAL_ISH:
			return real;
		case REAL:
			return realWithNoise;
		}
		throw new RuntimeException("unknown query mode: " + mode);
	}
}