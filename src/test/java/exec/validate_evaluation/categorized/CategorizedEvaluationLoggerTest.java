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

import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import cc.kave.commons.model.naming.Names;
import exec.csharp.queries.QueryMode;

public class CategorizedEvaluationLoggerTest {

	@Test
	@SuppressWarnings("unchecked")
	public void integration() {
		CategorizedEvaluationLogger<String> sut = CategorizedEvaluationLogger.create();

		sut.type(Names.newType("XXX, P"));
		sut.type(Names.newType("T, P"));

		sut.user("u1");
		sut.user("u2");
		sut.history();
		sut.queryMode(QueryMode.LINEAR);
		sut.microCommit();
		sut.microCommit();
		sut.microCommit();
		sut.microCommit();
		sut.finishedMicroCommits();
		sut.queryMode(QueryMode.RANDOM);
		sut.microCommit();
		sut.microCommit();
		sut.microCommit();
		sut.microCommit();
		sut.finishedMicroCommits();

		sut.user("u2");

		CategorizedResults<String> u1a = new CategorizedResults<String>();
		u1a.add("a", 0.2);
		u1a.add("c", 0.0);
		u1a.add("c", 0.1);
		u1a.add("c", 0.2);
		u1a.add("c", 0.3);
		u1a.add("b", 0.9);

		CategorizedResults<String> u1b = new CategorizedResults<String>();
		u1b.add("b", 0.5);
		u1b.add("a", 0.4);

		CategorizedResults<String> u1c = new CategorizedResults<String>();
		u1c.add("a", 0.5);
		u1c.add("a", 0.7);

		// count is the number of histories in which a key appeared, not how
		// often he appeared in total

		Map<QueryMode, List<CategorizedResults<String>>> res = Maps.newLinkedHashMap();
		res.put(QueryMode.LINEAR, Lists.newArrayList(u1a, u1b, u1c));
		res.put(QueryMode.RANDOM, Lists.newArrayList(u1b, u1c, u1a));

		sut.done(res);
	}
}