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

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import com.google.common.collect.Sets;

import cc.kave.commons.model.events.IDEEvent;

public class IDEEventIoTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();
	private File dir;
	private IDEEventIo sut;

	@Before
	public void setup() throws IOException {
		dir = tmp.newFolder("data");
		sut = new IDEEventIo(dir.getAbsolutePath());
	}

	// @Test
	public void findsZips() throws IOException {
		write(relFile("a.zip"), e(1));
		write(relFile("b", "c.zip"), e(2));
		new File(dir, "d.txt").createNewFile();

		Set<String> actuals = sut.findZips();
		Set<String> expecteds = Sets.newHashSet("a.zip", relFile("b", "c.zip"));
		// assertEquals(expecteds, actuals);
	}

	private void write(String zip, IDEEvent... e) {
		// new
	}

	private String relFile(String... parts) {
		return String.join(File.separator, parts);
	}

	private IDEEvent e(int i) {
		// TODO Auto-generated method stub
		return null;
	}
}