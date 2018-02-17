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
package exec.validate_evaluation.queryhistory;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Sets;

import exec.validate_evaluation.streaks.EditStreak;

public class QueryHistoryGenerationLoggerTest {

	private QueryHistoryGenerationLogger sut;

	@Before
	public void setup() {
		sut = new QueryHistoryGenerationLogger();
	}

	@Test
	public void integration() {
		sut.foundZips(Sets.newHashSet("a.zip", "b.zip"));
		file("a.zip");
		file("b.zip");
		sut.finish();

	}

	private void file(String file) {
		sut.processingFile(file);
		sut.foundEditStreaks(4);

		for (int i = 0; i < 4; i++) {
			streak();
		}

		sut.startFixingHistories();
		sut.fixedQueryHistory(-1);
		sut.fixedQueryHistory(-3);
		sut.fixedQueryHistory(-2);

		sut.startingRemoveEmptyHistories();
		sut.removedEmptyHistory();
		sut.removedEmptyHistory();
	}

	private void streak() {
		sut.processingEditStreak(new EditStreak());

		for (int i = 0; i < 3; i++) {
			sut.usage();
			sut.usage();
			sut.usage();
			sut.usage();
			sut.usage();
			sut.usage();
		}
	}
}