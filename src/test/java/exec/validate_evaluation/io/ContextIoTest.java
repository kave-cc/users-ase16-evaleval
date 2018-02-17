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
package exec.validate_evaluation.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.collect.Sets;

import cc.kave.commons.model.events.completionevents.Context;
import cc.kave.commons.model.naming.Names;
import cc.kave.commons.model.ssts.impl.SST;

public class ContextIoTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();
	private File dir;
	private ContextIo sut;

	@Before
	public void setup() throws IOException {
		dir = tmp.newFolder("data");
		sut = new ContextIo(dir.getAbsolutePath());
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
	public void writeReadCycleWorks() {
		Set<Context> expecteds = Sets.newHashSet(context(1), context(2), context(3));
		String zip = relFile("a", "b.zip");
		sut.write(expecteds, zip);
		Set<Context> actuals = sut.read(zip);
		assertEquals(expecteds, actuals);
	}

	@Test
	public void correctFileIsCreated() throws IOException {
		Set<Context> expecteds = Sets.newHashSet(context(1), context(2), context(3));
		sut.write(expecteds, relFile("a", "b.zip"));
		assertTrue(new File(file(dir, "a", "b.zip")).exists());
	}

	private static Context context(int ctxNum) {
		SST sst = new SST();
		sst.setEnclosingType(Names.newType("T%s,P", ctxNum));
		Context ctx = new Context();
		ctx.setSST(sst);
		return ctx;
	}

	private void create(String fileName) throws IOException {
		File f = new File(fileName);
		f.getParentFile().mkdirs();
		f.createNewFile();
	}

	private String file(File dir, String... parts) throws IOException {
		return Paths.get(dir.getAbsolutePath(), parts).toFile().getAbsolutePath();
	}

	private String relFile(String... tokens) {
		return String.join(File.separator, tokens);
	}
}