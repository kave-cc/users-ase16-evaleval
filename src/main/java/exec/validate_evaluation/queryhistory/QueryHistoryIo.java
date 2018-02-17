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

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.gson.reflect.TypeToken;

import cc.kave.commons.utils.io.Directory;
import cc.kave.commons.utils.io.IReadingArchive;
import cc.kave.commons.utils.io.WritingArchive;
import cc.kave.rsse.calls.usages.Usage;

public class QueryHistoryIo {

	private String dir;

	public QueryHistoryIo(String dir) {
		this.dir = dir;
	}

	public Set<String> findQueryHistoryZips() {
		Directory dir = new Directory(this.dir);
		return dir.findFiles(s -> s.endsWith(".zip"));
	}

	public void storeQueryHistories(Collection<List<Usage>> collection, String zip) {

		if (collection.isEmpty()) {
			return;
		}

		Directory dir = new Directory(this.dir);
		try (WritingArchive wa = dir.getWritingArchive(zip)) {
			for (List<Usage> us : collection) {
				if (!us.isEmpty()) {
					wa.add(us);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public Collection<List<Usage>> readQueryHistories(String zip) {

		Directory dir = new Directory(this.dir);
		try (IReadingArchive ra = dir.getReadingArchive(zip)) {

			Type type = new TypeToken<List<Usage>>() {
			}.getType();

			List<List<Usage>> us = Lists.newLinkedList();
			while (ra.hasNext()) {
				us.add(ra.getNext(type));
			}
			return us;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}