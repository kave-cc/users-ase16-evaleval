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
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import cc.kave.commons.model.naming.codeelements.IMethodName;
import cc.kave.commons.model.naming.types.ITypeName;
import exec.validate_evaluation.microcommits.MicroCommit;
import exec.validate_evaluation.microcommits.MicroCommitIo;

public class MicroCommitIoExtension {

	private Map<String, List<MicroCommit>> contents = Maps.newHashMap();

	public MicroCommitIoExtension(MicroCommitIo io) {
		Set<String> zips = io.findZips();
		System.out.printf("initializing MicroCommitIoExtension, reading %d zips: ", zips.size());
		for (String zip : zips) {
			contents.put(zip, io.read(zip));
			System.out.printf(".");
		}
		System.out.println();
	}

	public List<String> findZipsWith(ITypeName type) {
		List<String> zips = Lists.newLinkedList();
		zipLoop: for (String zip : contents.keySet()) {
			for (MicroCommit mc : contents.get(zip)) {
				if (type.equals(mc.getType())) {
					zips.add(zip);
					continue zipLoop;
				}
			}
		}
		return zips;
	}

	public Set<List<MicroCommit>> readZipAndSortByLocation(String zip, ITypeName type) {
		Map<IMethodName, List<MicroCommit>> locations = Maps.newHashMap();
		for (MicroCommit mc : contents.get(zip)) {
			if (type.equals(mc.getType())) {
				IMethodName ctx = mc.getMethodContext();
				List<MicroCommit> mcs = locations.get(ctx);
				if (mcs == null) {
					mcs = Lists.newLinkedList();
					locations.put(ctx, mcs);
				}
				mcs.add(mc);
			}
		}
		return Sets.newHashSet(locations.values());
	}
}