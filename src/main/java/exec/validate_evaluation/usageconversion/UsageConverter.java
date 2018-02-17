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
package exec.validate_evaluation.usageconversion;

import java.util.List;

import cc.kave.commons.model.events.completionevents.Context;
import cc.kave.commons.model.naming.types.ITypeName;
import cc.kave.commons.utils.io.IWritingArchive;
import cc.kave.commons.utils.io.ZipFolderLRUCache;
import cc.kave.rsse.calls.usages.Usage;
import exec.validate_evaluation.io.ContextIo;
import exec.validate_evaluation.queryhistory.UsageExtractor;

public class UsageConverter {

	private ContextIo cio;
	private ZipFolderLRUCache<ITypeName> uio;
	private UsageExtractor extractor;

	public UsageConverter(ContextIo cio, ZipFolderLRUCache<ITypeName> uio, UsageExtractor extractor) {
		this.cio = cio;
		this.uio = uio;
		this.extractor = extractor;

	}

	public void run() {

		for (String zip : cio.findZips()) {
			for (Context ctx : cio.read(zip)) {
				for (Usage u : analyze(ctx)) {
					IWritingArchive wa = uio.getArchive(u.getType());
					wa.add(u);
				}
			}
			uio.close();
		}
	}

	private List<Usage> analyze(Context ctx) {
		return extractor.analyse(ctx).getUsages();
	}
}