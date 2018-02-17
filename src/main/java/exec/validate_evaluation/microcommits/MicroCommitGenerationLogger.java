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

import static cc.kave.commons.utils.io.Logger.append;
import static cc.kave.commons.utils.io.Logger.log;

import java.util.Set;

public class MicroCommitGenerationLogger {

	public void init(int maxHistoryLength) {
		log("starting (max history length: %d)", maxHistoryLength);
	}

	private int totalZips;
	private int currentZip;

	public void foundZips(Set<String> zips) {
		totalZips = zips.size();
		currentZip = 0;

		log("found %d zips", totalZips);
		for (String zip : zips) {
			log("- %s", zip);
		}
	}

	public void processingZip(String zip) {
		double perc = 100 * currentZip++ / (double) totalZips;
		log("");
		log("### %s (%d/%d - %.1f%%) ###", zip, currentZip, totalZips, perc);
	}

	private boolean isFirstCommit;

	public void foundHistories(int size) {
		log("");
		log("found %d histories, will convert them to micro commits (notation: len(uh) > #mcs):", size);
		log("");
		isFirstCommit = true;
	}

	public void convertedToCommits(int qhLen, int numMCs) {
		if (!isFirstCommit) {
			append(", ");
		}
		append("%d", qhLen);
		append(">");
		append("%d", numMCs);
		isFirstCommit = false;
	}

	public void done() {
		log("");
		log("### done (100%%) ###");
	}
}