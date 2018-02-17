/**
 * Copyright (c) 2011-2014 Darmstadt University of Technology. All rights
 * reserved. This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Sebastian Proksch - initial API and implementation
 */
package exec.csharp;

import static cc.kave.rsse.calls.options.MiningOptions.newMiningOptions;
import static cc.kave.rsse.calls.options.QueryOptions.newQueryOptions;

import java.io.File;
import java.nio.file.Paths;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import cc.kave.commons.assertions.Asserts;
import cc.kave.rsse.calls.extraction.features.FeatureExtractor;
import cc.kave.rsse.calls.extraction.features.UsageFeatureExtractor;
import cc.kave.rsse.calls.extraction.features.UsageFeatureWeighter;
import cc.kave.rsse.calls.mining.ModelBuilder;
import cc.kave.rsse.calls.options.MiningOptions;
import cc.kave.rsse.calls.options.OptionsUtils;
import cc.kave.rsse.calls.options.QueryOptions;
import cc.kave.rsse.calls.pbn.PBNModelBuilder;
import cc.kave.rsse.calls.pbn.clustering.FeatureWeighter;
import cc.kave.rsse.calls.pbn.model.BayesianNetwork;
import cc.kave.rsse.calls.usages.Usage;
import cc.kave.rsse.calls.usages.features.UsageFeature;
import exec.csharp.evaluation.Evaluation;
import exec.csharp.evaluation.IEvaluation;
import exec.csharp.queries.RandomQueryBuilder;
import exec.csharp.utils.StorageHelper;
import exec.validate_evaluation.microcommits.MicroCommitIo;

public class Module extends AbstractModule {

	private static final String ROOT_PATH = "/Volumes/Data/";

	@Override
	protected void configure() {
		bindMiningAndQueryOptions();

		
		Asserts.assertTrue(new File(ROOT_PATH).exists(), "\n\n###\n### ROOT_PATH does not exist. Evaluation disc not mounted?\n###\n\n");
		
		// TODO read ROOT_PATH from settings file
		bind(StorageHelper.class).toInstance(new StorageHelper(ROOT_PATH));
		bind(IEvaluation.class).to(Evaluation.class);
	}

	private void bindMiningAndQueryOptions() {
		@SuppressWarnings("deprecation")
		String opts = OptionsUtils.pbn(10).c(false).d(true).p(false).useFloat().ignore(false).dropRareFeatures(false)
				.min(30).get();
		bind(QueryOptions.class).toInstance(newQueryOptions(opts));
		bind(MiningOptions.class).toInstance(newMiningOptions(opts));
	}

	@Provides
	public RandomQueryBuilder provideRandomQueryBuilder() {
		return new RandomQueryBuilder(6);
	}

	@Provides
	public FeatureWeighter<UsageFeature> provideFeatureWeighter(MiningOptions options) {
		return new UsageFeatureWeighter(options);
	}

	@Provides
	public FeatureExtractor<Usage, UsageFeature> provideFeatureExtractor(MiningOptions options) {
		return new UsageFeatureExtractor(options);
	}

	@Provides
	public ModelBuilder<UsageFeature, BayesianNetwork> provideModelBuilder() {
		return new PBNModelBuilder();
	}

	@Provides
	public MicroCommitIo provideMicroCommitIo() {
		return new MicroCommitIo(path("MicroCommits"));
	}

	private String path(String... relSubFolders) {
		return Paths.get(ROOT_PATH, relSubFolders).toString();
	}
}