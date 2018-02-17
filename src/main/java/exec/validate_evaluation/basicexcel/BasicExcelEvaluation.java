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
package exec.validate_evaluation.basicexcel;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

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
import exec.csharp.queries.IQueryBuilder;
import exec.csharp.queries.QueryBuilderFactory;
import exec.csharp.queries.QueryMode;
import exec.csharp.utils.ModelHelper;
import exec.csharp.utils.QueryUtils;
import exec.csharp.utils.StorageCase;
import exec.csharp.utils.StorageHelper;
import exec.validate_evaluation.microcommits.MicroCommit;
import exec.validate_evaluation.microcommits.MicroCommitIo;

public class BasicExcelEvaluation {

	private MicroCommitIo mcIo;
	private ModelHelper mh;
	private QueryBuilderFactory qbf;
	private NestedZipFolders<ITypeName> usages;
	private IQueryBuilder<Usage, Query> qb;
	private ICallsRecommender<Query> rec;

	@Inject
	public BasicExcelEvaluation(StorageHelper storageHelper, MicroCommitIo mcIo, ModelHelper mh,
			QueryBuilderFactory qb) {
		this.mcIo = mcIo;
		this.mh = mh;
		this.qbf = qb;
		this.usages = storageHelper.getNestedZipFolder(StorageCase.USAGES);
	}

	public void run() {

		// String = QueryMode\tDiffString
		Map<String, BoxplotData> res2 = Maps.newHashMap();
		Map<QueryMode, BoxplotData> res = Maps.newHashMap();
		Map<Tuple<QueryMode, ITypeName>, BoxplotData> res3 = Maps.newHashMap();
		Map<ITypeName, Integer> typeUsageCounts = Maps.newHashMap();

		for (QueryMode mode : QueryMode.values()) {
			res.put(mode, new BoxplotData());
		}

		System.out.printf("\ninitializing nested .zip folder for types/usages... (%s)\n", new Date());
		Set<ITypeName> types = usages.findKeys();
		// Set<ICoReTypeName> types =
		// Sets.newHashSet(CoReTypeName.get("LSystem/IO/MemoryStream"));
		for (ITypeName type : types) {
			List<Usage> us = usages.readAllZips(type, Usage.class);
			if (us.size() < 1) {
				continue;
			}
			typeUsageCounts.put(type, us.size());
			rec = mh.get(type);

			for (String zip : mcIo.findZips()) {

				// System.out.println("mode\tcategory\tf1");

				List<MicroCommit> readCommits = readCommits(zip, type);
				if (readCommits.isEmpty()) {
					continue;
				}

				System.out.printf("\n### found %d commits ###\nfor %s (%d usages)\nin %s\n\n", readCommits.size(), type,
						us.size(), zip);

				// QueryMode[] modes = QueryMode.values();
				QueryMode[] modes = new QueryMode[] { QueryMode.REAL };
				for (QueryMode mode : modes) {
					System.out.println(mode);

					Tuple<QueryMode, ITypeName> typeKey = Tuple.newTuple(mode, type);
					BoxplotData bpd3 = res3.get(typeKey);
					if (bpd3 == null) {
						bpd3 = new BoxplotData();
						res3.put(typeKey, bpd3);
					}

					qb = qbf.get(mode);

					for (MicroCommit mc : readCommits) {

						// System.out.println(mc.getStart());
						// System.out.println("---------");
						// System.out.println(mc.getEnd());

						Usage start = mc.getStart();
						Usage end = mc.getEnd();

						if (shouldSkip(start, end)) {
							// System.out.println("skipped");
							// System.out.println("########################");
							continue;
						}

						String diff = QueryUtils.toDiffString(mc);
						double f1 = measurePredictionQuality(start, end);

						// System.out.println();
						// System.out.printf("diff: %s\n", diff);
						// System.out.printf("--> %.3f\n", f1);
						// System.out.println("########################");

						res.get(mode).add(f1);
						bpd3.add(f1);

						String k2 = mode + "\t" + diff;
						BoxplotData bp = res2.get(k2);
						if (bp == null) {
							bp = new BoxplotData();
							res2.put(k2, bp);
						}
						bp.add(f1);

						// StringBuilder sb = new StringBuilder();
						//
						// sb.append(mode);
						// sb.append('\t');
						// sb.append(diff);
						// sb.append('\t');
						// sb.append(f1);
						// sb.append('\n');
						//
						// System.out.printf(sb.toString());
						System.out.printf(".");
					}
					System.out.println();
				}
			}
		}

		System.out.println("done at " + new Date());

		System.out.println();
		System.out.println();
		System.out.println("### RES1 -- grouped by (mode) ###");
		System.out.println("mode -> f1 (boxplot)");
		for (QueryMode m : res.keySet()) {
			BoxplotData bpd = res.get(m);
			if (bpd.hasData()) {
				Boxplot bp = bpd.getBoxplot();
				System.out.printf("%s -> %.3f (%s)\n", m, bp.getMean(), bp);
			}
		}

		System.out.println();
		System.out.println();
		System.out.println("### RES2 -- grouped by (mode+diff) ###");
		System.out.println("mode\tdiff\tcount\tf1\tbp");
		for (String k : res2.keySet()) {
			BoxplotData bpd = res2.get(k);
			if (bpd.hasData()) {
				Boxplot bp = bpd.getBoxplot();
				System.out.printf("%s\t%d\t%.3f\t%s\n", k, bp.getNumValues(), bp.getMean(), bp);
			}
		}

		System.out.println();
		System.out.println();
		System.out.println("### RES3 -- grouped by type ###");
		System.out.println("mode\ttype\tnumUsages\tcount\tf1\tbp");
		for (Tuple<QueryMode, ITypeName> modeAndType : res3.keySet()) {
			BoxplotData bpd = res3.get(modeAndType);
			if (bpd.hasData()) {
				Boxplot bp = res3.get(modeAndType).getBoxplot();

				QueryMode mode = modeAndType.getFirst();
				ITypeName type = modeAndType.getSecond();

				System.out.printf("%s\t%s\t%d\t%d\t%.3f\t%s\n", mode, type, typeUsageCounts.get(type),
						bp.getNumValues(), bp.getMean(), bp);
			}
		}
	}

	private Map<String, List<MicroCommit>> allCommits = Maps.newHashMap();

	private List<MicroCommit> readCommits(String zip, ITypeName type) {
		List<MicroCommit> commits = allCommits.get(zip);
		if (commits == null) {
			commits = mcIo.read(zip);
			allCommits.put(zip, commits);
		}

		List<MicroCommit> byType = Lists.newLinkedList();
		for (MicroCommit c : commits) {
			if (type.equals(c.getType())) {
				byType.add(c);
			}
		}
		// return commits.stream().filter(mc ->
		// type.equals(mc.getType())).collect(Collectors.toList());
		return byType;
	}

	private boolean shouldSkip(Usage start, Usage end) {
		int numAdditions = QueryUtils.countAdditions(start, end);
		if (0 == numAdditions) {
			return true;
		}
		return false;
	}

	private double measurePredictionQuality(Usage start, Usage end) {
		Usage sstart = safe(start, end);
		Usage send = safe(end, start);
		List<Query> queries = qb.createQueries(sstart, send);
		BoxplotData res = new BoxplotData();
		for (Query q : queries) {
			Set<IMethodName> proposals = getProposals(rec, q);
			Set<IMethodName> expectation = getExpectation(q, end);
			double f1 = Measure.newMeasure(expectation, proposals).getF1();
			res.add(f1);
		}
		return res.getMean();
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