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

import java.io.IOException;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

import cc.kave.commons.utils.io.Directory;
import cc.kave.commons.utils.io.IReadingArchive;
import cc.kave.commons.utils.io.WritingArchive;

public class MicroCommitIo {
	private String root;

	public MicroCommitIo(String dir) {
		this.root = dir;
	}

	public Set<String> findZips() {
		Directory dir = new Directory(this.root);
		return dir.findFiles(s -> s.endsWith(".zip"));
	}

	public void store(List<MicroCommit> commits, String zip) {
		Directory dir = new Directory(this.root);
		try (WritingArchive wa = dir.getWritingArchive(zip)) {
			for (MicroCommit mc : commits) {
				wa.add(mc);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public List<MicroCommit> read(String zip) {
		List<MicroCommit> commits = Lists.newLinkedList();
		Directory dir = new Directory(this.root);

		try (IReadingArchive ra = dir.getReadingArchive(zip)) {
			while (ra.hasNext()) {
				commits.add(ra.getNext(MicroCommit.class));
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return commits;
	}
}