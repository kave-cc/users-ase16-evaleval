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
package exec.csharp.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import cc.kave.commons.model.naming.types.ITypeName;
import cc.kave.commons.utils.io.Directory;
import cc.kave.commons.utils.io.NestedZipFolders;

public class StorageHelperTest {

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();
	private String tempFileName;

	private StorageHelper sut;

	@Before
	public void setup() {
		tempFileName = tempFolder.getRoot().getAbsolutePath();
		sut = new StorageHelper(tempFileName);
	}

	@Test
	public void correctDirectory() {
		assertDirectory(StorageCase.USAGES, "Usages");
		assertDirectory(StorageCase.MICRO_COMMITS, "MicroCommits");
		assertDirectory(StorageCase.NETWORKS, "Networks");
	}

	@Test
	public void correctDirectoryWithModifier() {
		sut.setModifier("xyz");
		assertDirectory(StorageCase.USAGES, "Usages-xyz");
		assertDirectory(StorageCase.MICRO_COMMITS, "MicroCommits-xyz");
		assertDirectory(StorageCase.NETWORKS, "Networks-xyz");
	}

	@Test
	public void correctNestedZip() {
		assertNestedZip(StorageCase.USAGES, "Usages");
		assertNestedZip(StorageCase.MICRO_COMMITS, "MicroCommits");
		assertNestedZip(StorageCase.NETWORKS, "Networks");
	}

	@Test
	public void modifierCanBeCleared() {
		sut.setModifier("xyz");
		sut.clearModifier();
		assertDirectory(StorageCase.USAGES, "Usages");
	}

	private void assertDirectory(StorageCase storageCase, String folder) {
		try {
			Directory d = sut.getDirectory(storageCase);
			URL actual = d.getUrl();
			Path p = Paths.get(tempFileName, folder);
			URL expected = new Directory(p.toFile().getAbsolutePath()).getUrl();
			assertEquals(expected, actual);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			fail();
		}

	}

	private void assertNestedZip(StorageCase storageCase, String folder) {
		try {
			NestedZipFolders<ITypeName> f = sut.getNestedZipFolder(storageCase);
			URL actual = f.getUrl();
			Path p = Paths.get(tempFileName, folder);
			URL expected = new Directory(p.toFile().getAbsolutePath()).getUrl();
			assertEquals(expected, actual);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
}