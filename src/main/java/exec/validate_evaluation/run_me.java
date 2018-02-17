/**
 * Copyright 2014 Technische Universität Darmstadt
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
package exec.validate_evaluation;

import java.io.File;

import org.apache.commons.io.FileUtils;

import exec.validate_evaluation.streaks.EditStreakGenerationIo;
import exec.validate_evaluation.streaks.EditStreakGenerationLogger;
import exec.validate_evaluation.streaks.EditStreakGenerationRunner;

public class run_me {

	private static String root = "/Volumes/Data/";
	private static String dirEventsCompletion = root + "Events/OnlyCompletion/";
	private static String dirEditStreaks = root + "EditStreaks/";

	public static void main(String[] args) {
		/* data preparation */
		generateStreaks();

		/* evaluation results */
	}

	private static void generateStreaks() {
		cleanDirs(dirEditStreaks);

		EditStreakGenerationIo io = new EditStreakGenerationIo(dirEventsCompletion, dirEditStreaks);
		EditStreakGenerationLogger logger = new EditStreakGenerationLogger();

		new EditStreakGenerationRunner(io, logger).run();
	}

	private static void cleanDirs(String... dirs) {
		for (String dir : dirs) {
			File f = new File(dir);
			FileUtils.deleteQuietly(f);
			f.mkdirs();
		}
	}
}