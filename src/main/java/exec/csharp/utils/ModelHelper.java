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
package exec.csharp.utils;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

import cc.kave.commons.model.naming.types.ITypeName;
import cc.kave.commons.utils.SublistSelector;
import cc.kave.commons.utils.io.NestedZipFolders;
import cc.kave.rsse.calls.ICallsRecommender;
import cc.kave.rsse.calls.mining.NoCallRecommender;
import cc.kave.rsse.calls.pbn.PBNMiner;
import cc.kave.rsse.calls.usages.Query;
import cc.kave.rsse.calls.usages.Usage;

public class ModelHelper {

	public static final int MAX_NUM_USAGES = 30000;

	private PBNMiner miner;
	private NestedZipFolders<ITypeName> zipsUsages;

	@Inject
	public ModelHelper(PBNMiner miner, StorageHelper storageHelper) {
		this.miner = miner;
		zipsUsages = storageHelper.getNestedZipFolder(StorageCase.USAGES);
	}

	public ICallsRecommender<Query> get(ITypeName type) {

		if (!zipsUsages.hasZips(type)) {
			return new NoCallRecommender();
		}

		List<Usage> usages = readTrainingData(type, zipsUsages);
		if (usages.isEmpty()) {
			return new NoCallRecommender();
		}

		if (usages.size() > MAX_NUM_USAGES) {
			usages = SublistSelector.pickRandomSublist(usages, MAX_NUM_USAGES);
		}

		ICallsRecommender<Query> recommender = miner.createRecommender(usages);
		return recommender;
	}

	private List<Usage> readTrainingData(ITypeName type, NestedZipFolders<ITypeName> zipsUsages) {
		List<Query> qs = zipsUsages.readAllZips(type, Query.class);
		return Lists.newLinkedList(qs);
	}
}