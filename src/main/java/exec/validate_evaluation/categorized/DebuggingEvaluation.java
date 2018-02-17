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
import java.util.Set;

import com.google.common.collect.Sets;

import cc.kave.commons.evaluation.Boxplot;
import cc.kave.commons.evaluation.BoxplotData;
import cc.kave.commons.evaluation.Measure;
import cc.kave.commons.model.naming.codeelements.IMethodName;
import cc.kave.commons.model.naming.types.ITypeName;
import cc.kave.commons.utils.io.NestedZipFolders;
import cc.kave.rsse.calls.ICallsRecommender;
import cc.kave.rsse.calls.datastructures.Tuple;
import cc.kave.rsse.calls.usages.CallSite;
import cc.kave.rsse.calls.usages.DefinitionSites;
import cc.kave.rsse.calls.usages.NoUsage;
import cc.kave.rsse.calls.usages.Query;
import cc.kave.rsse.calls.usages.Usage;
import exec.csharp.evaluation.impl.QueryContent;
import exec.csharp.queries.IQueryBuilder;
import exec.csharp.queries.QueryBuilderFactory;
import exec.csharp.queries.QueryMode;
import exec.csharp.utils.ModelHelper;
import exec.csharp.utils.NoiseMode;
import exec.csharp.utils.QueryJudge;
import exec.csharp.utils.QueryUtils;
import exec.validate_evaluation.microcommits.MicroCommit;

public class DebuggingEvaluation {
	private NestedZipFolders<ITypeName> usages;
	private MicroCommitIoExtension mcIo;
	private ModelHelper mh;
	private QueryBuilderFactory qbf;
	private IQueryBuilder<Usage, Query> qb;
	private ICallsRecommender<Query> rec;

	public DebuggingEvaluation(NestedZipFolders<ITypeName> usages, ModelHelper mh, MicroCommitIoExtension mcIo,
			QueryBuilderFactory qbf) {
		this.usages = usages;
		this.mh = mh;
		this.mcIo = mcIo;
		this.qbf = qbf;
	}

	public void run() {

		Set<ITypeName> keys = usages.findKeys();
		// ICoReTypeName[] keys = new ICoReTypeName[] {
		// CoReTypeName.get("LSystem/Enum"),
		// CoReTypeName.get("LSystem/Text/StringBuilder") };
		for (ITypeName type : keys) {
			rec = mh.get(type);

			for (String user : mcIo.findZipsWith(type)) {
				for (List<MicroCommit> mcs : mcIo.readZipAndSortByLocation(user, type)) {
					QueryMode[] queryModes = QueryMode.values();
					// QueryMode[] queryModes = new QueryMode[] { QueryMode.REAL
					// };
					for (MicroCommit mc : mcs) {
						Usage start = mc.getStart();
						Usage end = mc.getEnd();

						if (shouldSkip(start, end)) {

							// TODO: add pure removal!
							continue;
						}
						eval(start, end);
					}
				}
			}
		}
	}

	private boolean shouldSkip(Usage start, Usage end) {
		int numAdditions = QueryUtils.countAdditions(start, end);
		if (0 == numAdditions) {
			return true;
		}
		return false;
	}

	private void eval(Usage start, Usage end) {
		printMicroCommit();

		System.out.println("start:");
		System.out.println(start);
		System.out.println("end:");
		System.out.println(end);

		QueryJudge judge = new QueryJudge(start, end);
		NoiseMode noiseMode = judge.getNoiseMode();
		System.out.printf("noise: %s\n", noiseMode);
		QueryContent queryContentCategorization = judge.getQueryContentCategorization();
		System.out.printf("queryType: %s\n", queryContentCategorization);
		System.out.printf("diff: %s\n", QueryUtils.toDiffString(start, end));

		double f1Real = measurePredictionQuality(start, end, QueryMode.REAL);
		double f1Realish = measurePredictionQuality(start, end, QueryMode.REAL_ISH);
		double f1Linear = measurePredictionQuality(start, end, QueryMode.LINEAR);
		double f1Random = measurePredictionQuality(start, end, QueryMode.RANDOM);

		if (noiseMode == NoiseMode.NO_NOISE) {
			if (Math.abs(f1Real - f1Realish) > 0.05) {
				System.out.println();
			}
		}
	}

	private double measurePredictionQuality(Usage start, Usage end, QueryMode mode) {
		printMode(mode);
		qb = qbf.get(mode);
		Usage sstart = safe(start, end);
		Usage send = safe(end, start);
		List<Query> queries = qb.createQueries(sstart, send);
		BoxplotData res = new BoxplotData();
		boolean isFirst = true;
		for (Query q : queries) {
			printQuery(q, isFirst);
			isFirst = false;
			Set<IMethodName> proposals = getProposals(rec, q);
			Set<IMethodName> expectation = getExpectation(q, end);
			double f1 = Measure.newMeasure(expectation, proposals).getF1();
			printIntermediateF1(f1);
			res.add(f1);
		}
		double f1 = res.getMean();
		printF1(res.getBoxplot());
		return f1;
	}

	private void printIntermediateF1(double f1) {
		System.out.printf("f1: %.3f\n", f1);
	}

	private void printF1(Boxplot bp) {
		System.out.println();
		System.out.printf("--> f1: %.3f %s\n", bp.getMean(), bp);
	}

	private void printMicroCommit() {
		System.out.println();
		System.out.println("##########################################################");
	}

	private void printMode(QueryMode mode) {
		System.out.println();
		System.out.println("### " + mode + " ###");
		System.out.println();
	}

	private void printQuery(Usage u, boolean skipSep) {
		if (!skipSep) {
			System.out.println("---------------");
		}
		System.out.println(u);
	}

	private Usage safe(Usage u, Usage other) {
		if (u instanceof NoUsage) {
			Query q = new Query();
			q.setType(other.getType());
			q.setDefinition(DefinitionSites.createUnknownDefinitionSite());
			q.setClassContext(other.getClassContext());
			q.setMethodContext(other.getMethodContext());
			q.setAllCallsites(Sets.newHashSet());
			return q;
		} else {
			return u;
		}
	}

	private Set<IMethodName> getExpectation(Usage q, Usage end) {

		Set<IMethodName> expectation = Sets.newLinkedHashSet();
		for (CallSite cs : end.getReceiverCallsites()) {
			if (!q.getAllCallsites().contains(cs)) {
				expectation.add(cs.getMethod());
			}
		}
		return expectation;
	}

	private Set<IMethodName> getProposals(ICallsRecommender<Query> rec, Query query) {
		Set<IMethodName> proposals = Sets.newHashSet();
		for (Tuple<IMethodName, Double> p : rec.query(query)) {
			proposals.add(p.getFirst());
		}
		return proposals;
	}
}