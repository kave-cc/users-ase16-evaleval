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
package exec.validate_evaluation.microcommits;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Sets;

public class MicroCommitGenerationLoggerTest {

	private MicroCommitGenerationLogger sut;

	@Before
	public void setup() {
		sut = new MicroCommitGenerationLogger();
	}

	@Test
	public void integration() {
		Set<String> zips = Sets.newHashSet("a", "b");
		sut.foundZips(zips);
		for (String zip : zips) {
			file(zip);
		}
		sut.done();
	}

	private void file(String zip) {
		sut.processingZip(zip);
		sut.foundHistories(3);

		for (int i = 0; i < 3; i++) {
			sut.convertedToCommits(i, 2 * i);
		}
	}
}