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

import static cc.kave.commons.utils.io.Logger.append;
import static cc.kave.commons.utils.io.Logger.log;

import java.util.Iterator;
import java.util.Set;

import exec.validate_evaluation.streaks.EditStreak;
import exec.validate_evaluation.streaks.Snapshot;

public class QueryHistoryGenerationLogger {

	private int currentFile = 0;
	private int totalFiles;

	public void foundZips(Set<String> zips) {
		totalFiles = zips.size();
		log("found %d files:", totalFiles);
		for (String zip : zips) {
			log("- %s", zip);
		}
	}

	public void processingFile(String zip) {
		currentFile++;
		double progress = 100 * (currentFile - 1) / (double) totalFiles;
		log("");
		log("### %s (%d/%d - %.1f%%) ###", zip, currentFile, totalFiles, progress);
	}

	private int currentStreak;

	public void foundEditStreaks(int size) {
		currentStreak = 0;
		log("");
		log("found %d edit streaks", size);
	}

	private int currentSnapshot;

	public void processingEditStreak(EditStreak e) {
		currentStreak++;
		log("");
		log("-- edit streak %d: %s ---------------", currentStreak, getEncType(e));
		currentSnapshot = 0;
	}

	private String getEncType(EditStreak e) {
		Iterator<Snapshot> it = e.getSnapshots().iterator();
		if (it.hasNext()) {
			return it.next().getContext().getSST().getEnclosingType().getIdentifier();
		}
		return "--";
	}

	public void startSnapshot() {
		log("s%d: ", currentSnapshot++);
	}

	public void usage() {
		append(".");
	}

	public void usageMerged() {
		append("o");
	}

	public void finish() {
		log("");
		log("### done (100.0%%) ###", totalFiles, totalFiles);
	}

	/* everything from here on is printed in QueryHistoryCollector */

	public void registeredContextKeys(int size) {
		log("registered %d context keys", size);
	}

	private boolean isFirstFix;

	public void startFixingHistories() {
		log("");
		log("fixing histories:");
		log("");
		isFirstFix = true;
	}

	public void fixedQueryHistory(int diff) {
		if (isFirstFix) {
			isFirstFix = false;
			append("" + diff);
		} else {
			append(", " + diff);
		}
	}

	public void startingRemoveEmptyHistories() {
		log("");
		log("removing empty or single histories:");
		log("");
	}

	public void removedEmptyHistory() {
		append(".");
	}
}