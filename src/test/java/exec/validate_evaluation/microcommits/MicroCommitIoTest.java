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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
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

public class MicroCommitIoTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();
	private File dir;
	private MicroCommitIo sut;

	@Before
	public void setup() throws IOException {
		dir = tmp.newFolder("data");
		sut = new MicroCommitIo(dir.getAbsolutePath());
	}

	@Test
	public void zipsAreFound() throws IOException {
		create(file(dir, "a.txt"));
		create(file(dir, "a.zip"));
		create(file(dir, "a", "b", "c.zip"));

		Set<String> actuals = sut.findZips();
		Set<String> expecteds = Sets.newHashSet("a.zip", Paths.get("a", "b", "c.zip").toString());
		assertEquals(expecteds, actuals);
	}

	@Test
	public void storeReadCycleWorks() {
		List<MicroCommit> expecteds = commits(commit(1), commit(2));
		sut.store(expecteds, relFile("a", "b.zip"));
		List<MicroCommit> actuals = sut.read(relFile("a", "b.zip"));
		assertEquals(expecteds, actuals);
	}

	@Test
	public void correctFilesAreCreated() {
		storeReadCycleWorks();

		File expectedFile = Paths.get(dir.getAbsolutePath(), "a", "b.zip").toFile();
		assertTrue(expectedFile.exists());
	}

	private String relFile(String... tokens) {
		return String.join(File.separator, tokens);
	}

	private List<MicroCommit> commits(MicroCommit... commitArr) {
		return Lists.newArrayList(commitArr);
	}

	private MicroCommit commit(int i) {
		int j = 2 * i;
		MicroCommit mc = MicroCommit.create(usage(j), usage(j + 1));
		return mc;
	}

	private Usage usage(int num) {
		Query q = new Query();
		q.addCallSite(CallSites.createReceiverCallSite("LT.m" + num + "()V"));
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