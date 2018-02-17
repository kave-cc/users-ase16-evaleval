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
package exec.csharp.evaluation;

import java.util.List;

import cc.kave.commons.model.naming.types.ITypeName;
import cc.kave.commons.utils.io.Logger;
import cc.kave.rsse.calls.usages.DefinitionSiteKind;
import cc.kave.rsse.calls.usages.Usage;
import exec.csharp.queries.QueryMode;
import exec.validate_evaluation.microcommits.MicroCommit;

public abstract class AbstractEvaluationConsumer implements IEvaluationConsumer {

	@Override
	public void skippingType(ITypeName type, List<Usage> us, List<MicroCommit> histories) {
		Logger.log("");
		Logger.log("--- skipping %s (%d usage, %d micro commits)", type, us.size(), histories.size());
	}

	@Override
	public void startingType(ITypeName type, List<Usage> usages, List<MicroCommit> histories) {
		Logger.log("");
		Logger.log("###");
		Logger.log("### %s (%d usages, %d histories)", type, usages.size(), histories.size());
		Logger.log("###");
		Logger.log("");
	}

	@Override
	public void startingQueryMode(QueryMode mode) {
		Logger.log("  %s", mode);
		Logger.log("     ");
	}

	@Override
	public void skipCommit_NoChange(QueryMode mode) {
	}

	@Override
	public void skipCommit_NoAddition(QueryMode mode) {
	}

	@Override
	public void registerQuery(DefinitionSiteKind def, int before, int add, int after) {
	}
}