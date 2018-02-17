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
package exec.csharp;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.google.inject.Guice;
import com.google.inject.Injector;

import cc.kave.commons.assertions.Asserts;
import cc.kave.commons.model.naming.types.ITypeName;
import cc.kave.commons.utils.io.Directory;
import cc.kave.commons.utils.io.Logger;
import cc.kave.commons.utils.io.NestedZipFolders;
import exec.csharp.queries.QueryBuilderFactory;
import exec.csharp.utils.ModelHelper;
import exec.csharp.utils.StorageCase;
import exec.csharp.utils.StorageHelper;
import exec.demographics.Demographics;
import exec.demographics.DemographicsCollector;
import exec.demographics.DemographicsIO;
import exec.validate_evaluation.categorized.MicroCommitIoExtension;
import exec.validate_evaluation.categorized.NoiseCategorizedEvaluation;
import exec.validate_evaluation.categorized.ScenarioCategorizedEvaluation;
import exec.validate_evaluation.greedy_and_endgoal.GreedyAndEndGoalEval;
import exec.validate_evaluation.microcommits.FinalStateMicroCommitGenerationRunner;
import exec.validate_evaluation.microcommits.MicroCommitGenerationLogger;
import exec.validate_evaluation.microcommits.MicroCommitIo;
import exec.validate_evaluation.queryhistory.QueryHistoryCollector;
import exec.validate_evaluation.queryhistory.QueryHistoryGenerationLogger;
import exec.validate_evaluation.queryhistory.QueryHistoryGenerationRunner;
import exec.validate_evaluation.queryhistory.QueryHistoryIo;
import exec.validate_evaluation.queryhistory.UsageExtractor;
import exec.validate_evaluation.streaks.EditStreakGenerationIo;
import exec.validate_evaluation.streaks.EditStreakGenerationLogger;
import exec.validate_evaluation.streaks.EditStreakGenerationRunner;
import exec.validate_evaluation.streaks.EditStreakGenerationRunner.EmptyOrSingleEditStreakRemovalFilter;

public class run {

	private static Injector injector = Guice.createInjector(new Module());
	private static StorageHelper storageHelper;
	private static String dirRoot = "/Volumes/Data/";
	private static String dirCE = dirRoot + "Events/OnlyCompletion";
	private static String dirES = dirRoot + "EditStreaks";
	private static String dirQH = dirRoot + "QueryHistories";
	private static String dirMC = dirRoot + "MicroCommits";

	private static void init() {
		storageHelper = load(StorageHelper.class);

		Logger.setPrinting(true);
		Logger.setDebugging(false);
		Logger.setCapturing(false);

		long maxMem = Runtime.getRuntime().maxMemory();
		float maxMemInMb = Math.round(maxMem * 1.0d / (1024 * 1024 * 1.0f));
		Logger.log("maximum memory (-Xmx): %.0f MB", maxMemInMb);
	}

	public static void main(String[] args) throws IOException {
		init();

		/* data preparation */
		// runBatchPBNSmileMiner();
		// storageHelper.setModifier("inlined");
		// runBatchPBNSmileMiner();
		// storageHelper.clearModifier();

		/* evaluations */
		// load(UsageToMicroCommitRatioCalculator.class).run();
		// load(MicroCommitStatisticsRunner.class).run();
		// slow: load(UsagesStatisticsRunner.class).run(); // takes *very* long

		/* evaluations */
		// storageHelper.setModifier("inlined"); // TODO remove faked call in
		// evaluator
		// load(F1ByQueryMode.class).run();
		// load(F1ByQueryType.class).run();
		// load(F1ByCategory.class).run();
		// load(F1Details.class).run();
		// load(AnalysisOfNoise.class).run();

		/* new evals */
		// load(BasicExcelEvaluation.class).run();
		runMicroCommitTransformation();
		// runCategorizedEvaluation();
		runStats();
		// runGreedyVsGoalEval();
	}

	private static void runGreedyVsGoalEval() {
		NestedZipFolders<ITypeName> usages = storageHelper.getNestedZipFolder(StorageCase.USAGES);
		ModelHelper mh = load(ModelHelper.class);
		QueryHistoryIo qhIo = new QueryHistoryIo(dirQH);
		new GreedyAndEndGoalEval(usages, mh, qhIo).run();
	}

	private static void runCategorizedEvaluation() {
		NestedZipFolders<ITypeName> usages = storageHelper.getNestedZipFolder(StorageCase.USAGES);
		ModelHelper mh = load(ModelHelper.class);
		MicroCommitIo mcIo = new MicroCommitIo(dirMC);
		MicroCommitIoExtension mcIoExt = new MicroCommitIoExtension(mcIo);
		QueryBuilderFactory qbf = load(QueryBuilderFactory.class);
		new NoiseCategorizedEvaluation(usages, mh, mcIoExt, qbf).run();
		new ScenarioCategorizedEvaluation(usages, mh, mcIoExt, qbf).run();
		// new DebuggingEvaluation(usages, mh, mcIoExt, qbf).run();
	}

	private static void runStats() throws IOException {
		MicroCommitIo mcIo = new MicroCommitIo(dirMC);
		QueryHistoryIo qhIo = new QueryHistoryIo(dirQH);
		EditStreakGenerationIo esIo = new EditStreakGenerationIo(dirCE, dirES);
		StorageHelper sh = new StorageHelper(dirRoot);

		NestedZipFolders<ITypeName> usages = storageHelper.getNestedZipFolder(StorageCase.USAGES);
		// new QueryContentStats(usages, new
		// MicroCommitIoExtension(mcIo)).run();
		// new MicroCommitStats(mcIo, usages).run();
		// new QueryHistoryStats(qhIo, usages).run();
		// new UsageToMicroCommitRatioCalculator(sh, mcIo).run();

		DemographicsIO demIo = new DemographicsIO(mcIo, esIo, usages);
		Demographics demographics = new DemographicsCollector(demIo).collect();
		System.out.println(demographics.toCSV());
		demographics.printRatios();
	}

	private static void runMicroCommitTransformation() {

		EditStreakGenerationIo esIo = new EditStreakGenerationIo(dirCE, dirES);
		QueryHistoryIo qhIo = new QueryHistoryIo(dirQH);
		MicroCommitIo mcIo = new MicroCommitIo(dirMC);

		EditStreakGenerationLogger esLog = new EditStreakGenerationLogger();
		QueryHistoryGenerationLogger qhLog = new QueryHistoryGenerationLogger();
		MicroCommitGenerationLogger mcLog = new MicroCommitGenerationLogger();

		EditStreakGenerationRunner esGen = new EditStreakGenerationRunner(esIo, esLog);
		esGen.add(new EmptyOrSingleEditStreakRemovalFilter());
		QueryHistoryGenerationRunner qhGen = new QueryHistoryGenerationRunner(esIo, qhIo, qhLog,
				new QueryHistoryCollector(qhLog), new UsageExtractor());
		// MicroCommitGenerationRunner mcGen = new
		// MicroCommitGenerationRunner(qhIo, mcIo, mcLog);
		FinalStateMicroCommitGenerationRunner mcGen = new FinalStateMicroCommitGenerationRunner(qhIo, mcIo, mcLog);

		clean(dirES);
		esGen.run();

		clean(dirQH);
		qhGen.run();

		clean(dirMC);
		mcGen.run();
	}

	private static void clean(String dir) {
		try {
			File f = new File(dir);
			FileUtils.deleteDirectory(f);
			Asserts.assertFalse(f.exists());
			f.mkdirs();
			Asserts.assertTrue(f.exists());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static void runBatchPBNSmileMiner() {
		NestedZipFolders<ITypeName> zipsUsages = storageHelper.getNestedZipFolder(StorageCase.USAGES);
		Directory dirNetworks = storageHelper.getDirectory(StorageCase.NETWORKS);
		// load(BatchPBNSmileMiner.class).run(zipsUsages, dirNetworks);
	}

	private static <T> T load(Class<T> c) {
		return injector.getInstance(c);
	}
}