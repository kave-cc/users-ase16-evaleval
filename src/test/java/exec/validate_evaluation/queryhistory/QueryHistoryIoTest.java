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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import cc.kave.rsse.calls.usages.CallSites;
import cc.kave.rsse.calls.usages.Query;
import cc.kave.rsse.calls.usages.Usage;

public class QueryHistoryIoTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();
	private File dir;
	private QueryHistoryIo sut;

	@Before
	public void setup() throws IOException {
		dir = tmp.newFolder("data");
		sut = new QueryHistoryIo(dir.getAbsolutePath());
	}

	@Test
	public void filesAreFound() throws IOException {
		create(file(dir, "a.txt"));
		create(file(dir, "a.json"));
		create(file(dir, "a.zip"));
		create(file(dir, "a", "b", "c.zip"));

		Set<String> actuals = sut.findQueryHistoryZips();
		Set<String> expecteds = Sets.newHashSet("a.zip", Paths.get("a", "b", "c.zip").toString());
		assertEquals(expecteds, actuals);
	}

	@Test
	public void correctFileIsCreated() {

		Collection<List<Usage>> qhs = Lists.newLinkedList();
		qhs.add(history(query(1)));

		sut.storeQueryHistories(qhs, Paths.get("a", "b.zip").toString());

		File expectedFile = Paths.get(dir.getAbsolutePath(), "a", "b.zip").toFile();
		assertTrue(expectedFile.exists());
	}

	@Test
	public void correctFileIsNotCreatedWhenNoHistoriesAreProvided() {

		Collection<List<Usage>> qhs = Lists.newLinkedList();

		sut.storeQueryHistories(qhs, Paths.get("a", "b.zip").toString());

		File expectedFile = Paths.get(dir.getAbsolutePath(), "a", "b.zip").toFile();
		assertFalse(expectedFile.exists());
	}

	@Test
	public void storeAndReadHistories() {
		String zip = Paths.get("a", "b.zip").toString();

		Collection<List<Usage>> expecteds = Lists.newLinkedList();
		expecteds.add(history(query(1)));
		expecteds.add(history(query(2), query(3)));
		expecteds.add(history(query(4), query(5), query(6)));

		sut.storeQueryHistories(expecteds, zip);
		Collection<List<Usage>> actuals = sut.readQueryHistories(zip);

		assertEquals(expecteds, actuals);
	}

	@Test
	public void storeAndReadHistories_emptiesAreRemoved() {
		String zip = Paths.get("a", "b.zip").toString();

		Collection<List<Usage>> expecteds = Lists.newLinkedList();
		expecteds.add(history(query(1)));
		expecteds.add(history(query(2), query(3)));
		expecteds.add(history(query(4), query(5), query(6)));

		Collection<List<Usage>> input = Lists.newLinkedList();
		input.add(history());
		input.addAll(expecteds);

		sut.storeQueryHistories(input, zip);
		Collection<List<Usage>> actuals = sut.readQueryHistories(zip);

		assertEquals(expecteds, actuals);
	}

	private List<Usage> history(Usage... qs) {
		return Lists.newArrayList(qs);
	}

	private Usage query(int i) {
		Query q = new Query();
		q.addCallSite(CallSites.createReceiverCallSite("LT.m" + i + "()V"));
		return q;
	}

	private void create(String fileName) throws IOException {
		File f = new File(fileName);
		f.getParentFile().mkdirs();
		f.createNewFile();
	}

	private String file(File dir, String... parts) throws IOException {
		return Paths.get(dir.getAbsolutePath(), parts).toFile().getAbsolutePath();
	}
}