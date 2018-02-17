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

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import cc.kave.commons.evaluation.Boxplot;
import cc.kave.commons.evaluation.BoxplotData;
import cc.kave.commons.model.naming.types.ITypeName;
import exec.csharp.queries.QueryMode;

public class CategorizedEvaluationLogger<Category> {

	private ITypeName currentType;

	private boolean isFirstUser;

	private int numTypes;
	private int curType = 0;

	public void foundTypes(int numTypes) {
		System.out.printf("found %d types...\n", numTypes);
		this.numTypes = numTypes;

	}

	public void type(ITypeName type) {
		curType++;
		this.currentType = type;
		isFirstUser = true;
	}

	private boolean isFirstHistory;

	public void user(String user) {
		if (isFirstUser) {
			System.out.println();
			double perc = 100 * ((curType - 1) / (double) numTypes);
			System.out.printf("### %s (%d/%d - %.1f%%) #############################\n", currentType, curType, numTypes,
					perc);
			isFirstUser = false;
		}
		System.out.println();
		System.out.printf("[ %s ]\n", user);
		isFirstHistory = true;
	}

	public void history() {
		if (!isFirstHistory) {
			System.out.println("---");
		}
		isFirstHistory = false;
	}

	public void queryMode(QueryMode mode) {
		System.out.printf("%s: ", mode);
	}

	public void microCommit() {
		System.out.printf(".");
	}

	public void finishedMicroCommits() {
		System.out.println();
	}

	public void done(Map<QueryMode, List<CategorizedResults<Category>>> allRes) {
		System.out.println(new Date());
		System.out.println("done (all res merged by usage history)");

		Set<Category> categories = collectCategories(allRes.values());

		System.out.printf("mode");
		for (Category c : categories) {
			System.out.printf("\t%s", c);
		}
		System.out.println();

		Map<Category, Integer> counts = Maps.newHashMap();

		for (QueryMode mode : allRes.keySet()) {
			List<CategorizedResults<Category>> byMode = allRes.get(mode);
			CategorizedResults<Category> mergedRes = CategorizedResults.merge(byMode);

			counts = doneInternally(mode, categories, mergedRes);
		}

		System.out.printf("count");
		for (Category c : categories) {
			System.out.printf("\t%d", counts.get(c));
		}
		System.out.println();
	}

	public void doneAllTogether(Map<QueryMode, CategorizedResults<Category>> res) {
		System.out.println(new Date());
		System.out.println("done (all res unmerged)");
		Map<Category, Integer> counts = null;
		Set<Category> categories = res.values().iterator().next().getCategories();

		System.out.printf("mode");
		for (Category c : categories) {
			System.out.printf("\t%s", c);
		}
		System.out.println();

		for (QueryMode mode : res.keySet()) {
			counts = doneInternally(mode, categories, res.get(mode));
		}

		System.out.printf("count");
		for (Category c : categories) {
			System.out.printf("\t%d", counts.get(c));
		}
		System.out.println();
	}

	private Map<Category, Integer> doneInternally(QueryMode mode, Set<Category> categories,
			CategorizedResults<Category> res) {
		Map<Category, Integer> counts = Maps.newHashMap();

		System.out.printf("%s", mode);
		for (Category c : categories) {
			Boxplot bp = res.get(c);
			System.out.printf("\t%.5f", bp.getMean());

			int num = bp.getNumValues();
			Integer i = counts.get(c);
			if (i == null) {
				counts.put(c, num);
			} else {
				counts.put(c, i + num);
			}
		}
		System.out.println();

		return counts;
	}

	private Set<Category> collectCategories(Collection<List<CategorizedResults<Category>>> values) {
		Set<Category> catgories = Sets.newLinkedHashSet();
		for (List<CategorizedResults<Category>> results : values) {
			for (CategorizedResults<Category> result : results) {
				catgories.addAll(result.getCategories());
			}
		}
		return catgories;
	}

	public static <T> CategorizedEvaluationLogger<T> create() {
		return new CategorizedEvaluationLogger<T>();
	}

	public void doneByMode(Map<QueryMode, BoxplotData> resByMode) {
		System.out.println("averaged over all commits:");
		for (QueryMode mode : resByMode.keySet()) {
			Boxplot bp = resByMode.get(mode).getBoxplot();
			System.out.printf("%s:\t%.1f\t%s\n", mode, bp.getMean(), bp);
		}
	}
}