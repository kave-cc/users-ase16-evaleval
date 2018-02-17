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

import static cc.kave.commons.assertions.Asserts.assertNotNull;

import java.io.File;
import java.util.Map;

import com.google.common.collect.Maps;

import cc.kave.commons.model.naming.types.ITypeName;
import cc.kave.commons.utils.io.Directory;
import cc.kave.commons.utils.io.NestedZipFolders;

public class StorageHelper {

	private final String rootPath;
	private final Map<StorageCase, String> locations;

	private String modifier;

	public StorageHelper(String rootPath) {
		if (!rootPath.endsWith(File.separator)) {
			rootPath = rootPath + File.separator;
		}
		this.rootPath = rootPath;
		locations = Maps.newHashMap();
		locations.put(StorageCase.USAGES, "Usages");
		locations.put(StorageCase.MICRO_COMMITS, "MicroCommits");
		locations.put(StorageCase.NETWORKS, "Networks");
		modifier = "";
	}

	public void setModifier(String modifier) {
		assertNotNull(modifier);
		this.modifier = modifier;
	}

	public void clearModifier() {
		modifier = "";
	}

	public NestedZipFolders<ITypeName> getNestedZipFolder(StorageCase storageCase) {
		return new NestedZipFolders<ITypeName>(getDirectory(storageCase), ITypeName.class);
	}

	public Directory getDirectory(StorageCase storageCase) {
		return new Directory(getPath(storageCase));
	}

	private String getPath(StorageCase storageCase) {
		String mod = modifier.isEmpty() ? "" : "-" + modifier;
		return rootPath + locations.get(storageCase) + mod + "/";
	}
}